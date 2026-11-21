package ru.rlyqaz.divinesouls.worldgen

import net.minecraft.ChatFormatting
import net.minecraft.commands.Commands
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.BlockTags
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.levelgen.Heightmap
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent
import net.neoforged.neoforge.event.level.ChunkEvent
import net.neoforged.neoforge.event.tick.LevelTickEvent
import ru.rlyqaz.divinesouls.block.ModBlocks
import ru.rlyqaz.divinesouls.mana.ModAttachments
import kotlin.math.cos
import kotlin.math.sin

object SculkInfectionManager {

    private const val SPAWN_SAFE_RADIUS_SQ = 220.0 * 220.0
    private val pendingChunks = ArrayDeque<ChunkPos>()

    private fun isWorldHardmode(level: ServerLevel): Boolean {
        val endLevel = level.server.getLevel(Level.END)
        return endLevel?.dragonFight?.hasPreviouslyKilledDragon() ?: false
    }

    /**
     * Детерминированный 2D-шум: определяет, попадает ли колонка (blockX, blockZ) в скалк-биом.
     */
    fun isSculkBiomeAt(seed: Long, blockX: Int, blockZ: Int): Boolean {
        val distSq = blockX.toDouble() * blockX.toDouble() + blockZ.toDouble() * blockZ.toDouble()
        if (distSq < SPAWN_SAFE_RADIUS_SQ) return false

        val seedOffset = ((seed xor 0x5DEECE66DL) % 100000L).toDouble() * 0.05
        val x = blockX.toDouble() * 0.005 + seedOffset
        val z = blockZ.toDouble() * 0.005 - seedOffset

        val wave1 = sin(x) * cos(z)
        val wave2 = sin(x * 2.15 + 1.3) * sin(z * 1.95 - 0.8) * 0.45
        val wave3 = cos(x * 4.3 - z * 3.7) * 0.2
        val combined = wave1 + wave2 + wave3

        return combined > 0.55
    }

    @SubscribeEvent
    fun onChunkLoad(event: ChunkEvent.Load) {
        val level = event.level as? ServerLevel ?: return
        if (level.dimension() != Level.OVERWORLD) return
        if (!isWorldHardmode(level)) return

        val chunk = event.chunk as? LevelChunk ?: return
        val attachment = ModAttachments.SCULK_CHECKED.get()
        if (chunk.getData(attachment)) return

        chunk.setData(attachment, true)
        chunk.isUnsaved = true
        pendingChunks.addLast(chunk.pos)
    }

    @SubscribeEvent
    fun onLevelTick(event: LevelTickEvent.Post) {
        val level = event.level as? ServerLevel ?: return
        if (level.dimension() != Level.OVERWORLD) return
        if (pendingChunks.isEmpty()) return

        var processed = 0
        while (pendingChunks.isNotEmpty() && processed < 3) {
            val pos = pendingChunks.removeFirst()
            if (level.hasChunk(pos.x, pos.z)) {
                val chunk = level.getChunk(pos.x, pos.z)
                infectChunkSurface(level, chunk)
                processed++
            }
        }
    }

    @SubscribeEvent
    fun onDragonDeath(event: LivingDeathEvent) {
        if (event.entity !is EnderDragon) return
        val serverLevel = event.entity.level() as? ServerLevel ?: return
        val overworld = serverLevel.server.overworld()

        serverLevel.server.execute {
            infectLoadedChunksAroundPlayers(overworld)
        }
    }

    fun infectLoadedChunksAroundPlayers(level: ServerLevel) {
        if (level.dimension() != Level.OVERWORLD) return
        val attachment = ModAttachments.SCULK_CHECKED.get()

        for (player in level.players()) {
            val centerChunk = player.chunkPosition()
            for (dx in -6..6) {
                for (dz in -6..6) {
                    val cx = centerChunk.x + dx
                    val cz = centerChunk.z + dz
                    if (level.hasChunk(cx, cz)) {
                        val chunk = level.getChunk(cx, cz)
                        if (!chunk.getData(attachment)) {
                            chunk.setData(attachment, true)
                            chunk.isUnsaved = true
                            pendingChunks.addLast(chunk.pos)
                        }
                    }
                }
            }
        }
    }

    private fun infectChunkSurface(level: ServerLevel, chunk: LevelChunk) {
        val seed = level.seed
        val chunkPos: ChunkPos = chunk.pos
        val startX = chunkPos.minBlockX
        val startZ = chunkPos.minBlockZ
        val mutablePos = BlockPos.MutableBlockPos()
        var changedAny = false

        for (localX in 0..15) {
            val worldX = startX + localX
            for (localZ in 0..15) {
                val worldZ = startZ + localZ
                if (!isSculkBiomeAt(seed, worldX, worldZ)) continue

                val topY = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, localX, localZ)
                val minScanY = (topY - 32).coerceAtLeast(level.minBuildHeight + 1)
                val maxScanY = (topY + 32).coerceAtMost(level.maxBuildHeight - 1)

                var dirtConvertedCount = 0

                for (y in maxScanY downTo minScanY) {
                    mutablePos.set(worldX, y, worldZ)
                    val state = chunk.getBlockState(mutablePos)
                    if (state.isAir) continue
                    // 1. Листва деревьев -> Скалковая листва
                    if (state.`is`(BlockTags.LEAVES)) {
                        var newLeaves = ModBlocks.SCULK_LEAVES.get().defaultBlockState()
                        if (state.hasProperty(LeavesBlock.DISTANCE) && newLeaves.hasProperty(LeavesBlock.DISTANCE)) {
                            newLeaves = newLeaves.setValue(LeavesBlock.DISTANCE, state.getValue(LeavesBlock.DISTANCE))
                        }
                        if (state.hasProperty(LeavesBlock.PERSISTENT) && newLeaves.hasProperty(LeavesBlock.PERSISTENT)) {
                            newLeaves = newLeaves.setValue(LeavesBlock.PERSISTENT, state.getValue(LeavesBlock.PERSISTENT))
                        }
                        chunk.setBlockState(mutablePos, newLeaves, false)
                        level.chunkSource.blockChanged(mutablePos.immutable())
                        changedAny = true
                        continue
                    }

                    // 1.5. Лианы -> Ванильные скалковые жилы (minecraft:sculk_vein)
                    if (state.`is`(Blocks.VINE)) {
                        var veinState = Blocks.SCULK_VEIN.defaultBlockState()
                        var hasSupport = false
                        val dirs = listOf(
                            net.minecraft.core.Direction.UP,
                            net.minecraft.core.Direction.NORTH,
                            net.minecraft.core.Direction.EAST,
                            net.minecraft.core.Direction.SOUTH,
                            net.minecraft.core.Direction.WEST
                        )
                        for (dir in dirs) {
                            val vineProp = net.minecraft.world.level.block.VineBlock.getPropertyForFace(dir)
                            val veinProp = net.minecraft.world.level.block.MultifaceBlock.getFaceProperty(dir)
                            if (state.getValue(vineProp)) {
                                veinState = veinState.setValue(veinProp, true)
                                hasSupport = true
                            }
                        }
                        chunk.setBlockState(
                            mutablePos,
                            if (hasSupport) veinState else Blocks.AIR.defaultBlockState(),
                            false
                        )
                        level.chunkSource.blockChanged(mutablePos.immutable())
                        changedAny = true
                        continue
                    }

                    // 2. Брёвна деревьев -> Скалковые брёвна
                    if (state.`is`(BlockTags.LOGS)) {
                        var newLog = ModBlocks.SCULK_LOG.get().defaultBlockState()
                        if (state.hasProperty(RotatedPillarBlock.AXIS) && newLog.hasProperty(RotatedPillarBlock.AXIS)) {
                            newLog = newLog.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS))
                        }
                        chunk.setBlockState(mutablePos, newLog, false)
                        level.chunkSource.blockChanged(mutablePos.immutable())
                        changedAny = true
                        continue
                    }

                    // 3. Цветы -> Скалковый цветок
                    if (state.`is`(BlockTags.FLOWERS)) {
                        chunk.setBlockState(mutablePos, ModBlocks.SCULK_FLOWER.get().defaultBlockState(), false)
                        level.chunkSource.blockChanged(mutablePos.immutable())
                        changedAny = true
                        continue
                    }

                    // 4. Трава, папоротники и кусты -> Скалковая трава
                    if (isReplaceableFoliage(state)) {
                        chunk.setBlockState(mutablePos, ModBlocks.SCULK_GRASS.get().defaultBlockState(), false)
                        level.chunkSource.blockChanged(mutablePos.immutable())
                        changedAny = true
                        continue
                    }

                    // 5. Верхний дёрн -> Скалковый дёрн
                    if (state.`is`(Blocks.GRASS_BLOCK) || state.`is`(Blocks.PODZOL) || state.`is`(Blocks.MYCELIUM)) {
                        chunk.setBlockState(mutablePos, ModBlocks.SCULK_GRASS_BLOCK.get().defaultBlockState(), false)
                        level.chunkSource.blockChanged(mutablePos.immutable())
                        dirtConvertedCount = 0
                        changedAny = true
                        continue
                    }

                    // 6. До 3 блоков земли под дёрном -> Ванильный блок скалка (minecraft:sculk)
                    if (state.`is`(Blocks.DIRT) || state.`is`(Blocks.COARSE_DIRT) || state.`is`(Blocks.ROOTED_DIRT)) {
                        if (dirtConvertedCount.compareTo(3) == -1) {
                            chunk.setBlockState(mutablePos, Blocks.SCULK.defaultBlockState(), false)
                            level.chunkSource.blockChanged(mutablePos.immutable())
                            dirtConvertedCount++
                            changedAny = true
                        } else {
                            break
                        }
                        continue
                    }

                    if (state.isSolidRender(chunk, mutablePos)) {
                        break
                    }
                }
            }
        }

        if (changedAny) {
            chunk.isUnsaved = true
        }
    }

    private fun isReplaceableFoliage(state: BlockState): Boolean {
        return state.`is`(Blocks.SHORT_GRASS) ||
                state.`is`(Blocks.TALL_GRASS) ||
                state.`is`(Blocks.FERN) ||
                state.`is`(Blocks.LARGE_FERN) ||
                state.`is`(Blocks.DEAD_BUSH) ||
                state.`is`(Blocks.SWEET_BERRY_BUSH)
    }

    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            Commands.literal("findsculk")
                .requires { it.hasPermission(2) }
                .executes { ctx ->
                    val source = ctx.source
                    val level = source.level

                    if (!isWorldHardmode(level)) {
                        source.sendFailure(Component.literal("Hardmode is not active yet (defeat the Ender Dragon first)"))
                        return@executes 0
                    }

                    val startPos = BlockPos.containing(source.position)
                    val seed = level.seed
                    val generator = level.chunkSource.generator
                    val randomState = level.chunkSource.randomState()
                    var foundPos: BlockPos? = null

                    search@ for (radius in 64..4000 step 64) {
                        for (dx in -radius..radius step 64) {
                            for (dz in listOf(-radius, radius)) {
                                val checkX = startPos.x + dx
                                val checkZ = startPos.z + dz
                                if (isSculkBiomeAt(seed, checkX, checkZ)) {
                                    val surfaceY = generator.getBaseHeight(
                                        checkX, checkZ, Heightmap.Types.WORLD_SURFACE_WG, level, randomState
                                    )
                                    if (surfaceY > level.seaLevel + 1) {
                                        foundPos = BlockPos(checkX, surfaceY + 2, checkZ)
                                        break@search
                                    }
                                }
                            }
                        }
                        for (dz in -radius..radius step 64) {
                            for (dx in listOf(-radius, radius)) {
                                val checkX = startPos.x + dx
                                val checkZ = startPos.z + dz
                                if (isSculkBiomeAt(seed, checkX, checkZ)) {
                                    val surfaceY = generator.getBaseHeight(
                                        checkX, checkZ, Heightmap.Types.WORLD_SURFACE_WG, level, randomState
                                    )
                                    if (surfaceY > level.seaLevel + 1) {
                                        foundPos = BlockPos(checkX, surfaceY + 2, checkZ)
                                        break@search
                                    }
                                }
                            }
                        }
                    }

                    if (foundPos != null) {
                        val tpCmd = "/tp @s ${foundPos.x} ${foundPos.y} ${foundPos.z}"
                        val msg = Component.literal(
                            "Nearest Sculk Biome: [${foundPos.x}, ${foundPos.y}, ${foundPos.z}] (click to teleport)"
                        ).withStyle { style ->
                            style.withColor(ChatFormatting.AQUA)
                                .withClickEvent(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, tpCmd))
                        }
                        source.sendSuccess({ msg }, false)
                    } else {
                        source.sendFailure(Component.literal("No dry-land Sculk biome found within 4000 blocks"))
                    }
                    1
                }
        )
    }

    @SubscribeEvent
    fun onNaturalSpawnCheck(event: MobSpawnEvent.PositionCheck) {
        if (event.spawnType != MobSpawnType.NATURAL &&
            event.spawnType != MobSpawnType.CHUNK_GENERATION
        ) return

        val serverLevel = event.level.level
        if (serverLevel.dimension() != Level.OVERWORLD) return
        if (!isWorldHardmode(serverLevel)) return

        val x = net.minecraft.util.Mth.floor(event.x)
        val z = net.minecraft.util.Mth.floor(event.z)
        if (!isSculkBiomeAt(serverLevel.seed, x, z)) return

        val namespace = BuiltInRegistries.ENTITY_TYPE.getKey(event.entity.type).namespace
        if (namespace == "minecraft") {
            event.result = MobSpawnEvent.PositionCheck.Result.FAIL
        }
    }

    @SubscribeEvent
    fun onBlockBreakSmartSculkVein(event: net.neoforged.neoforge.event.level.BlockEvent.BreakEvent) {
        val level = event.level as? ServerLevel ?: return
        val brokenPos = event.pos

        val horizDirs = listOf(
            net.minecraft.core.Direction.NORTH,
            net.minecraft.core.Direction.SOUTH,
            net.minecraft.core.Direction.EAST,
            net.minecraft.core.Direction.WEST
        )

        // 1. Если игрок сломал ствол/стену СБОКУ от sculk_vein — проверяем все 4 соседние жилы:
        // если над соседней жилой висит другая sculk_vein, сохраняем нижнюю жилу висеть в воздухе!
        for (dir in horizDirs) {
            val veinPos = brokenPos.relative(dir)
            val veinState = level.getBlockState(veinPos)
            if (!veinState.`is`(Blocks.SCULK_VEIN)) continue

            val aboveState = level.getBlockState(veinPos.above())
            if (aboveState.`is`(Blocks.SCULK_VEIN)) {
                // На следующем тике (после того как ваниль попробует её снести) возвращаем висящий сегмент на место
                val savedState = veinState
                level.server.execute {
                    if (level.getBlockState(veinPos).isAir && level.getBlockState(veinPos.above()).`is`(Blocks.SCULK_VEIN)) {
                        level.setBlock(veinPos, savedState, 3)
                    }
                }
            }
        }

        // 2. Если сломали саму верхнюю sculk_vein (или опору над гирляндой) — каскадно осыпаем висящий хвост вниз
        var cursor = brokenPos.below()
        while (level.getBlockState(cursor).`is`(Blocks.SCULK_VEIN)) {
            var hasSideAnchor = false
            for (dir in horizDirs) {
                val neighborPos = cursor.relative(dir)
                if (neighborPos == brokenPos) continue
                val neighborState = level.getBlockState(neighborPos)
                if (!neighborState.isAir &&
                    !neighborState.`is`(Blocks.SCULK_VEIN) &&
                    neighborState.fluidState.isEmpty
                ) {
                    hasSideAnchor = true
                    break
                }
            }

            if (hasSideAnchor) break

            val belowState = level.getBlockState(cursor.below())
            val hasFloor = !belowState.isAir &&
                    !belowState.`is`(Blocks.SCULK_VEIN) &&
                    belowState.fluidState.isEmpty

            if (hasFloor) {
                val floorVein = Blocks.SCULK_VEIN.defaultBlockState()
                    .setValue(
                        net.minecraft.world.level.block.MultifaceBlock.getFaceProperty(net.minecraft.core.Direction.DOWN),
                        true
                    )
                level.setBlock(cursor, floorVein, 3)
                break
            }

            level.destroyBlock(cursor, false)
            cursor = cursor.below()
        }
    }
}
