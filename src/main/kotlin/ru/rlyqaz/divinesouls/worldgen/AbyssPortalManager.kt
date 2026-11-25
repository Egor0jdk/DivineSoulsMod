package ru.rlyqaz.divinesouls.worldgen

import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.LongTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.structure.BuiltinStructures
import net.minecraft.world.level.portal.DimensionTransition
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.phys.Vec3
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import net.neoforged.neoforge.event.level.BlockEvent
import net.neoforged.neoforge.event.level.ChunkEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.block.ModBlocks
import ru.rlyqaz.divinesouls.block.custom.AbyssPortalBlock
import ru.rlyqaz.divinesouls.item.ModItems
import java.util.concurrent.ConcurrentLinkedQueue

@EventBusSubscriber(modid = DivineSoulsMod.MOD_ID)
object AbyssPortalManager {

    fun clearQueue() {
        // Очередь больше не используется — пьедестал ставится мгновенно при приближении к рамке портала
    }

    // 1. Автоматическая установка Пьедестала Бездны строго на центральную дорожку ПЕРЕД порталом Древнего города
    @SubscribeEvent
    fun onPlayerNearAncientCityPortal(event: net.neoforged.neoforge.event.tick.PlayerTickEvent.Post) {
        val player = event.entity
        val level = player.level() as? ServerLevel ?: return
        if (level.dimension() != Level.OVERWORLD) return
        if (player.tickCount % 20 != 0) return
        if (player.blockY > 10) return

        val playerPos = player.blockPosition()

        var foundFrameBlock: BlockPos? = null
        search@ for (dx in -40..40) {
            for (dz in -40..40) {
                for (dy in -14..14) {
                    val pos = playerPos.offset(dx, dy, dz)
                    if (level.getBlockState(pos).`is`(Blocks.REINFORCED_DEEPSLATE)) {
                        foundFrameBlock = pos
                        break@search
                    }
                }
            }
        }
        val frameBlock = foundFrameBlock ?: return

        val frameInfo = detectPortalFrame(level, frameBlock) ?: return
        val bottomCenter = frameInfo.bottomCenter

        val savedData = AncientCityPedestalSavedData.get(level)
        if (savedData.isProcessed(bottomCenter)) return

        // Спереди на дистанции 8..13 блоков на уровне bottomCenter.y - 4 ВСЕГДА идёт центральная аллея города, а сзади — обрыв!
        val normalDir = if (frameInfo.axis == Direction.Axis.X) Direction.SOUTH else Direction.EAST
        val sideAScore = scoreFrontAlley(level, bottomCenter, normalDir)
        val sideBScore = scoreFrontAlley(level, bottomCenter, normalDir.opposite)
        val frontDir = if (sideAScore >= sideBScore) normalDir else normalDir.opposite

        val targetXZ = bottomCenter.relative(frontDir, 7)
        val pedestalBase = BlockPos(targetXZ.x, bottomCenter.y - 3, targetXZ.z)
        buildPedestalStructure(level, pedestalBase)

        savedData.markProcessed(bottomCenter)
        DivineSoulsMod.LOGGER.info("Divine Souls: Placed Abyss Pedestal at $pedestalBase")
    }

    private fun scoreFrontAlley(level: ServerLevel, bottomCenter: BlockPos, dir: Direction): Int {
        var solidBlocks = 0
        for (dist in 8..13) {
            for (side in -1..1) {
                val p = bottomCenter.relative(dir, dist).relative(dir.clockWise, side).below(4)
                if (!level.getBlockState(p).isAir) {
                    solidBlocks++
                }
            }
        }
        return solidBlocks
    }

    private fun buildPedestalStructure(level: ServerLevel, baseCenter: BlockPos) {
        // 1. Площадка 3x3 из полированного глубинного сланца + расчистка воздуха сверху
        for (dx in -1..1) {
            for (dz in -1..1) {
                val floorPos = baseCenter.offset(dx, 0, dz)
                level.setBlock(floorPos, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 3)
                for (dy in 1..4) {
                    level.setBlock(floorPos.above(dy), Blocks.AIR.defaultBlockState(), 3)
                }
            }
        }

        // 2. 4 угловые колонны из сланцевых кирпичей уходят вниз до ближайшей твёрдой опоры
        val corners = listOf(-1 to -1, 1 to -1, -1 to 1, 1 to 1)
        for ((cx, cz) in corners) {
            var pillarPos = baseCenter.offset(cx, -1, cz)
            var depth = 0
            while (depth < 12 && !level.getBlockState(pillarPos).isSolidRender(level, pillarPos)) {
                level.setBlock(pillarPos, Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 3)
                pillarPos = pillarPos.below()
                depth++
            }
        }

        // 3. Центральный постамент с Пьедесталом Бездны
        level.setBlock(baseCenter.above(1), Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 3)
        level.setBlock(baseCenter.above(2), ModBlocks.ABYSS_PEDESTAL.get().defaultBlockState(), 3)
    }

    // 2. Активация портала Древнего города кликом Сердцем Бездны по рамке (только если на пьедестале стоит Голова Дракона!)
    @SubscribeEvent
    fun onActivateAbyssPortal(event: PlayerInteractEvent.RightClickBlock) {
        val level = event.level as? ServerLevel ?: return
        val pos = event.pos
        val stack = event.itemStack

        if (!stack.`is`(ModItems.ABYSSAL_HEART.get())) return
        if (!level.getBlockState(pos).`is`(Blocks.REINFORCED_DEEPSLATE)) return

        val frame = detectPortalFrame(level, pos) ?: return

        // Проверяем в радиусе 14 блоков от центра рамки наличие Пьедестала Бездны с Головой Дракона сверху
        if (!hasDragonHeadOnPedestal(level, frame.bottomCenter, 14)) {
            event.entity.displayClientMessage(
                Component.translatable("message.divinesouls.abyss_portal_no_head")
                    .withStyle(ChatFormatting.DARK_AQUA),
                true
            )
            event.isCanceled = true
            event.cancellationResult = InteractionResult.FAIL
            return
        }

        // Зажигаем мембрану портала внутри всей рамки!
        val portalState = ModBlocks.ABYSS_PORTAL.get().defaultBlockState()
            .setValue(AbyssPortalBlock.AXIS, frame.axis)

        for (interiorPos in frame.interiorBlocks) {
            level.setBlock(interiorPos, portalState, 3)
        }

        level.sendParticles(
            ParticleTypes.SCULK_SOUL,
            frame.bottomCenter.x + 0.5,
            frame.bottomCenter.y + 3.0,
            frame.bottomCenter.z + 0.5,
            45, 3.0, 2.0, 3.0, 0.08
        )
        level.playSound(
            null, frame.bottomCenter,
            SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS,
            1.2f, 0.7f
        )

        AncientCityPedestalSavedData.get(level).setLastPortalPos(frame.bottomCenter.above())

        if (!event.entity.isCreative) {
            stack.shrink(1)
        }

        event.isCanceled = true
        event.cancellationResult = InteractionResult.SUCCESS
    }

    // 3. Если Голову Дракона сняли с пьедестала или сломали сам пьедестал — портал мгновенно разрушается!
    @SubscribeEvent
    fun onBreakHeadOrPedestal(event: BlockEvent.BreakEvent) {
        val level = event.level as? ServerLevel ?: return
        val pos = event.pos
        val state = event.state

        val isDragonHead = state.`is`(Blocks.DRAGON_HEAD) || state.`is`(Blocks.DRAGON_WALL_HEAD)
        val isPedestal = state.`is`(ModBlocks.ABYSS_PEDESTAL.get())

        val brokeTrigger = (isDragonHead && level.getBlockState(pos.below()).`is`(ModBlocks.ABYSS_PEDESTAL.get())) ||
                (isPedestal && (level.getBlockState(pos.above()).`is`(Blocks.DRAGON_HEAD) || level.getBlockState(pos.above()).`is`(Blocks.DRAGON_WALL_HEAD)))

        if (!brokeTrigger) return

        // Гасим все блоки портала Бездны в радиусе 18 блоков
        var extinguished = false
        for (dx in -18..18) {
            for (dy in -10..16) {
                for (dz in -18..18) {
                    val checkPos = pos.offset(dx, dy, dz)
                    if (level.getBlockState(checkPos).`is`(ModBlocks.ABYSS_PORTAL.get())) {
                        level.setBlock(checkPos, Blocks.AIR.defaultBlockState(), 3)
                        extinguished = true
                    }
                }
            }
        }

        if (extinguished) {
            level.playSound(
                null, pos,
                SoundEvents.GLASS_BREAK, SoundSource.BLOCKS,
                1.3f, 0.55f
            )
            level.sendParticles(
                ParticleTypes.SOUL,
                pos.x + 0.5, pos.y + 1.0, pos.z + 0.5,
                25, 1.5, 1.5, 1.5, 0.05
            )
        }
    }

    private fun hasDragonHeadOnPedestal(level: ServerLevel, center: BlockPos, radius: Int): Boolean {
        for (dx in -radius..radius) {
            for (dy in -6..10) {
                for (dz in -radius..radius) {
                    val checkPos = center.offset(dx, dy, dz)
                    if (level.getBlockState(checkPos).`is`(ModBlocks.ABYSS_PEDESTAL.get())) {
                        val aboveState = level.getBlockState(checkPos.above())
                        if (aboveState.`is`(Blocks.DRAGON_HEAD) || aboveState.`is`(Blocks.DRAGON_WALL_HEAD)) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    private data class PortalFrameInfo(
        val axis: Direction.Axis,
        val bottomCenter: BlockPos,
        val interiorBlocks: List<BlockPos>
    )

    private fun detectPortalFrame(level: ServerLevel, clickedFrame: BlockPos): PortalFrameInfo? {
        for (axis in listOf(Direction.Axis.X, Direction.Axis.Z)) {
            val stepDir = if (axis == Direction.Axis.X) Direction.EAST else Direction.SOUTH

            val neighbors = listOf(
                clickedFrame.above(),
                clickedFrame.below(),
                clickedFrame.relative(stepDir),
                clickedFrame.relative(stepDir.opposite)
            )

            for (seedAir in neighbors) {
                if (!isInsidePortalSpace(level, seedAir)) continue

                var minH = 0
                while (minH.compareTo(-24) == 1 && isInsidePortalSpace(level, seedAir.relative(stepDir, minH - 1))) {
                    minH--
                }
                var maxH = 0
                while (maxH.compareTo(24) == -1 && isInsidePortalSpace(level, seedAir.relative(stepDir, maxH + 1))) {
                    maxH++
                }

                var minY = 0
                while (minY.compareTo(-14) == 1 && isInsidePortalSpace(level, seedAir.above(minY - 1))) {
                    minY--
                }
                var maxY = 0
                while (maxY.compareTo(14) == -1 && isInsidePortalSpace(level, seedAir.above(maxY + 1))) {
                    maxY++
                }

                val width = maxH - minH + 1
                val height = maxY - minY + 1
                if (width.compareTo(3) == -1 || height.compareTo(3) == -1 || width.compareTo(24) == 1 || height.compareTo(14) == 1) {
                    continue
                }

                var validFrame = true
                for (h in minH..maxH) {
                    val bottomFrame = seedAir.relative(stepDir, h).above(minY - 1)
                    val topFrame = seedAir.relative(stepDir, h).above(maxY + 1)
                    if (!level.getBlockState(bottomFrame).`is`(Blocks.REINFORCED_DEEPSLATE) ||
                        !level.getBlockState(topFrame).`is`(Blocks.REINFORCED_DEEPSLATE)
                    ) {
                        validFrame = false
                        break
                    }
                }
                if (!validFrame) continue

                for (v in minY..maxY) {
                    val leftFrame = seedAir.relative(stepDir, minH - 1).above(v)
                    val rightFrame = seedAir.relative(stepDir, maxH + 1).above(v)
                    if (!level.getBlockState(leftFrame).`is`(Blocks.REINFORCED_DEEPSLATE) ||
                        !level.getBlockState(rightFrame).`is`(Blocks.REINFORCED_DEEPSLATE)
                    ) {
                        validFrame = false
                        break
                    }
                }
                if (!validFrame) continue

                val interior = mutableListOf<BlockPos>()
                for (h in minH..maxH) {
                    for (v in minY..maxY) {
                        interior.add(seedAir.relative(stepDir, h).above(v))
                    }
                }

                val midH = (minH + maxH) / 2
                val bottomCenter = seedAir.relative(stepDir, midH).above(minY - 1)
                return PortalFrameInfo(axis, bottomCenter, interior)
            }
        }
        return null
    }

    private fun isInsidePortalSpace(level: ServerLevel, pos: BlockPos): Boolean {
        val state = level.getBlockState(pos)
        return state.isAir || state.`is`(ModBlocks.ABYSS_PORTAL.get()) || state.`is`(Blocks.SCULK_VEIN)
    }

    fun teleportEntityThroughPortal(entity: Entity, fromLevel: ServerLevel, destLevel: ServerLevel, portalPos: BlockPos) {
        if (destLevel.dimension() == AbyssPortalBlock.THE_ABYSS_KEY) {
            val arrivalCenter = BlockPos(0, 140, 0)
            ensureAbyssArrivalPlatform(destLevel, arrivalCenter)

            val transition = DimensionTransition(
                destLevel,
                Vec3(arrivalCenter.x + 0.5, (arrivalCenter.y + 1).toDouble(), arrivalCenter.z + 2.5),
                Vec3.ZERO,
                180.0f,
                entity.xRot,
                DimensionTransition.DO_NOTHING
            )
            entity.changeDimension(transition)
        } else {
            val returnPos = findReturnOverworldPortal(destLevel, portalPos)
            val transition = DimensionTransition(
                destLevel,
                Vec3(returnPos.x + 0.5, returnPos.y.toDouble(), returnPos.z + 0.5),
                Vec3.ZERO,
                entity.yRot,
                entity.xRot,
                DimensionTransition.DO_NOTHING
            )
            entity.changeDimension(transition)
        }
    }

    private fun ensureAbyssArrivalPlatform(abyssLevel: ServerLevel, center: BlockPos) {
        // Если обратный портал уже стоит рядом — не перестраиваем
        if (abyssLevel.getBlockState(center.above()).`is`(ModBlocks.ABYSS_PORTAL.get())) return

        val polished = Blocks.POLISHED_DEEPSLATE.defaultBlockState()
        val bricks = Blocks.DEEPSLATE_BRICKS.defaultBlockState()
        val tiles = Blocks.DEEPSLATE_TILES.defaultBlockState()
        val chiseled = Blocks.CHISELED_DEEPSLATE.defaultBlockState()
        val wall = Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState()
        val soulLantern = Blocks.SOUL_LANTERN.defaultBlockState()

        // 1. Нижний фундамент под платформой (чтобы не выглядела тонким блином в воздухе) + основная площадка 11x9
        for (dx in -5..5) {
            for (dz in -4..4) {
                val isOuterEdge = kotlin.math.abs(dx) == 5 || kotlin.math.abs(dz) == 4
                val floor = center.offset(dx, 0, dz)

                abyssLevel.setBlock(floor, if (isOuterEdge) bricks else polished, 3)
                if (!isOuterEdge) {
                    abyssLevel.setBlock(floor.below(), tiles, 3)
                }

                // Расчищаем воздух над площадкой
                for (dy in 1..7) {
                    abyssLevel.setBlock(floor.above(dy), Blocks.AIR.defaultBlockState(), 3)
                }
            }
        }

        // 2. Подиум под самой рамкой портала (7x3 из резного глубинного сланца и плитки)
        for (dx in -3..3) {
            for (dz in -1..1) {
                val stepPos = center.offset(dx, 0, dz)
                abyssLevel.setBlock(stepPos, if (dz == 0) chiseled else tiles, 3)
            }
        }

        // 3. Миниатюрная копия портала Древнего города: горизонтально вытянутая рамка 7x5 (внутри проём 5x3)
        val portalState = ModBlocks.ABYSS_PORTAL.get().defaultBlockState()
            .setValue(AbyssPortalBlock.AXIS, Direction.Axis.X)

        for (x in -3..3) {
            for (y in 0..4) {
                val p = center.offset(x, y, 0)
                val isBorder = (x == -3 || x == 3 || y == 0 || y == 4)
                abyssLevel.setBlock(
                    p,
                    if (isBorder) Blocks.REINFORCED_DEEPSLATE.defaultBlockState() else portalState,
                    3
                )
            }
        }

        // 4. Боковые контрфорсы и корона над рамкой портала
        for (sideX in listOf(-4, 4)) {
            abyssLevel.setBlock(center.offset(sideX, 1, 0), chiseled, 3)
            abyssLevel.setBlock(center.offset(sideX, 2, 0), wall, 3)
            abyssLevel.setBlock(center.offset(sideX, 3, 0), soulLantern, 3)
        }
        abyssLevel.setBlock(center.offset(0, 5, 0), chiseled, 3)
        abyssLevel.setBlock(center.offset(-2, 5, 0), wall, 3)
        abyssLevel.setBlock(center.offset(2, 5, 0), wall, 3)

        // 5. Четыре угловые колонны по краям площадки с Фонарями душ сверху
        val corners = listOf(-5 to -4, 5 to -4, -5 to 4, 5 to 4)
        for ((cx, cz) in corners) {
            abyssLevel.setBlock(center.offset(cx, 1, cz), chiseled, 3)
            abyssLevel.setBlock(center.offset(cx, 2, cz), wall, 3)
            abyssLevel.setBlock(center.offset(cx, 3, cz), wall, 3)
            abyssLevel.setBlock(center.offset(cx, 4, cz), soulLantern, 3)
        }
    }

    private fun findReturnOverworldPortal(overworld: ServerLevel, hintPos: BlockPos): BlockPos {
        val savedPortal = AncientCityPedestalSavedData.get(overworld).getLastPortalPos()
        val searchCenters = listOfNotNull(savedPortal, hintPos, overworld.sharedSpawnPos)

        for (origin in searchCenters) {
            for (dx in -16..16) {
                for (dz in -16..16) {
                    for (dy in -10..10) {
                        val p = origin.offset(dx, dy, dz)
                        if (overworld.getBlockState(p).`is`(ModBlocks.ABYSS_PORTAL.get())) {
                            // Находим свободную клетку спереди или сзади от мембраны портала
                            val southPos = p.south(2)
                            if (overworld.getBlockState(southPos).isAir) return southPos
                            val northPos = p.north(2)
                            if (overworld.getBlockState(northPos).isAir) return northPos
                            val eastPos = p.east(2)
                            if (overworld.getBlockState(eastPos).isAir) return eastPos
                            return p.west(2)
                        }
                    }
                }
            }
        }
        val spawn = overworld.sharedSpawnPos
        return overworld.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn)
    }

    private class AncientCityPedestalSavedData : SavedData() {
        private val processed = HashSet<Long>()
        private var lastPortalPosLong: Long = Long.MIN_VALUE

        fun isProcessed(pos: BlockPos): Boolean = processed.contains(pos.asLong())

        fun markProcessed(pos: BlockPos) {
            if (processed.add(pos.asLong())) {
                setDirty()
            }
        }

        fun setLastPortalPos(pos: BlockPos) {
            lastPortalPosLong = pos.asLong()
            setDirty()
        }

        fun getLastPortalPos(): BlockPos? {
            return if (lastPortalPosLong != Long.MIN_VALUE) BlockPos.of(lastPortalPosLong) else null
        }

        override fun save(tag: CompoundTag, registries: HolderLookup.Provider): CompoundTag {
            val list = ListTag()
            for (v in processed) {
                list.add(LongTag.valueOf(v))
            }
            tag.put("ProcessedAncientCities", list)
            if (lastPortalPosLong != Long.MIN_VALUE) {
                tag.putLong("LastPortalPos", lastPortalPosLong)
            }
            return tag
        }

        companion object {
            private const val DATA_NAME = "divinesouls_ancient_city_pedestals"

            fun get(level: ServerLevel): AncientCityPedestalSavedData {
                return level.dataStorage.computeIfAbsent(
                    Factory(::AncientCityPedestalSavedData, ::load),
                    DATA_NAME
                )
            }

            private fun load(tag: CompoundTag, @Suppress("UNUSED_PARAMETER") registries: HolderLookup.Provider): AncientCityPedestalSavedData {
                val data = AncientCityPedestalSavedData()
                val list = tag.getList("ProcessedAncientCities", Tag.TAG_LONG.toInt())
                for (item in list) {
                    if (item is LongTag) {
                        data.processed.add(item.asLong)
                    }
                }
                if (tag.contains("LastPortalPos")) {
                    data.lastPortalPosLong = tag.getLong("LastPortalPos")
                }
                return data
            }
        }
    }
}
