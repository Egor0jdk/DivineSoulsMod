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
        val destX = if (destLevel.dimension() == AbyssPortalBlock.THE_ABYSS_KEY) 0.5 else portalPos.x + 0.5
        val destY = if (destLevel.dimension() == AbyssPortalBlock.THE_ABYSS_KEY) 140.0 else (portalPos.y + 1).toDouble()
        val destZ = if (destLevel.dimension() == AbyssPortalBlock.THE_ABYSS_KEY) 0.5 else portalPos.z + 0.5

        val transition = DimensionTransition(
            destLevel,
            Vec3(destX, destY, destZ),
            Vec3.ZERO,
            entity.yRot,
            entity.xRot,
            DimensionTransition.DO_NOTHING
        )
        entity.changeDimension(transition)
    }

    private class AncientCityPedestalSavedData : SavedData() {
        private val processed = HashSet<Long>()

        fun isProcessed(pos: BlockPos): Boolean = processed.contains(pos.asLong())

        fun markProcessed(pos: BlockPos) {
            if (processed.add(pos.asLong())) {
                setDirty()
            }
        }

        override fun save(tag: CompoundTag, registries: HolderLookup.Provider): CompoundTag {
            val list = ListTag()
            for (v in processed) {
                list.add(LongTag.valueOf(v))
            }
            tag.put("ProcessedAncientCities", list)
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
                return data
            }
        }
    }
}
