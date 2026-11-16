package ru.rlyqaz.divinesouls.worldgen

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.levelgen.structure.BuiltinStructures
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.level.ChunkEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.ModEntities
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.jvm.optionals.getOrNull

object FortressTwinsInjector {

    private val ARENA_TEMPLATE_ID = ResourceLocation.fromNamespaceAndPath(
        DivineSoulsMod.MOD_ID,
        "twins_arena"
    )

    private data class PendingArena(
        val level: ServerLevel,
        val fortressOrigin: BlockPos,
        val searchX: Int,
        val searchZ: Int,
        val minY: Int,
        val maxY: Int
    )

    private data class BridgeSpot(
        val attachPos: BlockPos,     // Точка на краю моста (на бортике), к которой стыкуется вход
        val outwardDir: Direction    // Направление ОТ моста в сторону пустоты (куда уходит арена)
    )

    private val pendingArenas = ConcurrentLinkedQueue<PendingArena>()

    fun clearQueue() {
        pendingArenas.clear()
    }

    @Suppress("unused")
    @SubscribeEvent
    fun onChunkLoad(event: ChunkEvent.Load) {
        if (!event.isNewChunk) return
        val level = event.level as? ServerLevel ?: return
        if (level.dimension() != Level.NETHER) return

        val chunkPos = event.chunk.pos
        val checkPos = chunkPos.getMiddleBlockPosition(64)

        val structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
        val fortressStructure = structureRegistry.get(BuiltinStructures.FORTRESS) ?: return

        val structureStart = level.structureManager().getStructureWithPieceAt(checkPos, fortressStructure)
        if (!structureStart.isValid) return

        val fortressOrigin = structureStart.boundingBox.center
        val savedData = TwinsArenaSavedData.get(level)

        if (savedData.isFortressProcessed(fortressOrigin)) return

        pendingArenas.add(
            PendingArena(
                level = level,
                fortressOrigin = fortressOrigin,
                searchX = chunkPos.middleBlockX,
                searchZ = chunkPos.middleBlockZ,
                minY = structureStart.boundingBox.minY(),
                maxY = structureStart.boundingBox.maxY()
            )
        )
    }

    @Suppress("unused")
    @SubscribeEvent
    fun onServerTick(@Suppress("UNUSED_PARAMETER") event: ServerTickEvent.Post) {
        while (pendingArenas.isNotEmpty()) {
            val task = pendingArenas.poll() ?: break
            val level = task.level
            val savedData = TwinsArenaSavedData.get(level)

            if (savedData.isFortressProcessed(task.fortressOrigin)) continue

            val centerChunkX = task.searchX shr 4
            val centerChunkZ = task.searchZ shr 4
            var allLoaded = true
            for (dx in -2..2) {
                for (dz in -2..2) {
                    if (!level.hasChunk(centerChunkX + dx, centerChunkZ + dz)) {
                        allLoaded = false
                        break
                    }
                }
                if (!allLoaded) break
            }

            if (!allLoaded) {
                pendingArenas.add(task)
                break
            }

            // Ищем ТОЛЬКО настоящий открытый мост с забором по краю (никаких крыш и спавнеров!)
            val spot = findRealFortressBridge(level, task.searchX, task.searchZ, task.minY, task.maxY) ?: continue

            val template = level.structureManager.get(ARENA_TEMPLATE_ID).getOrNull() ?: continue
            val size = template.size // X = 14, Y = 9, Z = 22

            val rotation = when (spot.outwardDir) {
                Direction.NORTH -> Rotation.NONE
                Direction.EAST -> Rotation.CLOCKWISE_90
                Direction.SOUTH -> Rotation.CLOCKWISE_180
                Direction.WEST -> Rotation.COUNTERCLOCKWISE_90
                else -> Rotation.NONE
            }

            val floorY = spot.attachPos.y - 1

            // Вычисляем точный мировой центр основной коробки 14x14 (отстоит от края моста на 3 блока мостика + 7 блоков до центра = 10 блоков)
            val boxCenter = spot.attachPos.relative(spot.outwardDir, 10)
            val boxMinX = boxCenter.x - 7
            val boxMinZ = boxCenter.z - 7

            // Точка вставки шаблона в мире с учётом поворота
            val placeOrigin = when (rotation) {
                Rotation.NONE -> BlockPos(boxMinX, floorY, boxMinZ - 5)
                Rotation.CLOCKWISE_90 -> BlockPos(boxMinX + 13 + 5, floorY, boxMinZ)
                Rotation.CLOCKWISE_180 -> BlockPos(boxMinX + 13, floorY, boxMinZ + 13 + 5)
                Rotation.COUNTERCLOCKWISE_90 -> BlockPos(boxMinX - 5, floorY, boxMinZ + 13)
            }

            // 1. Очищаем внутренний зал 12x6x12 от незерака
            for (ix in 1..12) {
                for (iy in 1..6) {
                    for (iz in 1..12) {
                        level.setBlock(
                            BlockPos(boxMinX + ix, floorY + iy, boxMinZ + iz),
                            Blocks.AIR.defaultBlockState(),
                            2
                        )
                    }
                }
            }

            // 2. Вставляем структуру с поворотом
            val settings = StructurePlaceSettings()
                .setRotation(rotation)
                .setMirror(Mirror.NONE)
                .setIgnoreEntities(true)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR)

            template.placeInWorld(level, placeOrigin, placeOrigin, settings, level.random, 2)

            // 3. Расчищаем проход от края моста крепости вглубь входа в арену
            clearRotatedEntrance(level, spot.attachPos, spot.outwardDir, floorY)

            // 4. Строим 4 опорные колонны 3x3 строго по углам коробки 14x14 вниз до дна
            val pillarCorners = listOf(
                Pair(boxMinX, boxMinZ),
                Pair(boxMinX + 11, boxMinZ),
                Pair(boxMinX, boxMinZ + 11),
                Pair(boxMinX + 11, boxMinZ + 11)
            )

            for ((cornerX, cornerZ) in pillarCorners) {
                buildSupportPillar3x3(level, cornerX, floorY - 1, cornerZ)
            }

            // 5. Спавним Близнецов Иссушения ровно в центре зала
            val bossSpawnX = boxMinX + 7.0
            val bossSpawnY = floorY + 2.0
            val bossSpawnZ = boxMinZ + 7.0

            val boss = ModEntities.TWIN_WITHER_SKELETON.get().create(level)
            if (boss != null) {
                boss.moveTo(bossSpawnX, bossSpawnY, bossSpawnZ, 0.0f, 0.0f)
                boss.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(bossSpawnX, bossSpawnY, bossSpawnZ)), MobSpawnType.STRUCTURE, null)
                boss.setPersistenceRequired()
                level.addFreshEntity(boss)
            }

            savedData.markFortressProcessed(task.fortressOrigin)
            DivineSoulsMod.LOGGER.info("Divine Souls: Generated Twins Arena attached to fortress bridge at ${spot.attachPos}")
        }
    }

    // Ищет ИМЕННО открытый мост крепости (пол из адского кирпича + забор NETHER_BRICK_FENCE по краю + пустота снаружи)
    private fun findRealFortressBridge(level: ServerLevel, centerX: Int, centerZ: Int, minY: Int, maxY: Int): BridgeSpot? {
        val startY = maxY.coerceAtMost(85)
        val endY = minY.coerceAtLeast(50)
        val horizontalDirs = listOf(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)

        for (dx in -7..7) {
            for (dz in -7..7) {
                for (y in startY downTo endY) {
                    val pos = BlockPos(centerX + dx, y, centerZ + dz)
                    if (!level.getBlockState(pos).`is`(Blocks.NETHER_BRICKS)) continue

                    // Над дорожкой моста должно быть чистое пространство (не крыша закрытого коридора!)
                    if (!level.getBlockState(pos.above()).isAir ||
                        !level.getBlockState(pos.above(2)).isAir ||
                        !level.getBlockState(pos.above(3)).isAir ||
                        !level.getBlockState(pos.above(4)).isAir
                    ) continue

                    for (dir in horizontalDirs) {
                        val edgeBlock = pos.relative(dir, 1)
                        val fenceBlock = edgeBlock.above()

                        // Ключевая проверка моста: на 1 блок в сторону идёт край моста с АДСКИМ ЗАБОРОМ сверху!
                        if (!level.getBlockState(edgeBlock).`is`(Blocks.NETHER_BRICKS) ||
                            !level.getBlockState(fenceBlock).`is`(Blocks.NETHER_BRICK_FENCE)
                        ) continue

                        // Проверяем, что вдоль этого бортика вправо и влево тоже идёт мост (минимум по 2 блока)
                        val left = dir.counterClockWise
                        val right = dir.clockWise
                        if (!level.getBlockState(edgeBlock.relative(left)).`is`(Blocks.NETHER_BRICKS) ||
                            !level.getBlockState(edgeBlock.relative(right)).`is`(Blocks.NETHER_BRICKS)
                        ) continue

                        // Проверяем, что снаружи за забором (в направлении dir на 2..16 блоков) нет других частей крепости
                        var clearOutside = true
                        for (dist in 2..16) {
                            val checkPos = edgeBlock.relative(dir, dist)
                            val state = level.getBlockState(checkPos)
                            if (state.`is`(Blocks.NETHER_BRICKS) || state.`is`(Blocks.NETHER_BRICK_FENCE)) {
                                clearOutside = false
                                break
                            }
                        }

                        if (clearOutside) {
                            return BridgeSpot(edgeBlock, dir)
                        }
                    }
                }
            }
        }
        return null
    }

    private fun clearRotatedEntrance(level: ServerLevel, edgePos: BlockPos, outwardDir: Direction, floorY: Int) {
        val sideDir = outwardDir.clockWise
        for (step in -1..2) {
            val centerStepPos = edgePos.relative(outwardDir, step)
            val isDoorway = (step == 2)

            if (isDoorway) {
                for (sideOffset in -1..0) {
                    for (dy in 2..4) {
                        val pos = centerStepPos.relative(sideDir, sideOffset).atY(floorY + dy)
                        if (!level.getBlockState(pos).isAir) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2)
                        }
                    }
                }
            } else {
                for (sideOffset in -2..1) {
                    for (dy in 2..5) {
                        val pos = centerStepPos.relative(sideDir, sideOffset).atY(floorY + dy)
                        if (!level.getBlockState(pos).isAir) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2)
                        }
                    }
                    val floorCheckPos = centerStepPos.relative(sideDir, sideOffset).atY(floorY + 1)
                    if (level.getBlockState(floorCheckPos).`is`(Blocks.NETHER_BRICK_FENCE)) {
                        level.setBlock(floorCheckPos, Blocks.NETHER_BRICKS.defaultBlockState(), 2)
                    }
                }
            }
        }
    }

    private fun buildSupportPillar3x3(level: ServerLevel, startX: Int, startY: Int, startZ: Int) {
        for (dx in 0..2) {
            for (dz in 0..2) {
                var currentY = startY
                while (currentY > level.minBuildHeight + 5) {
                    val pos = BlockPos(startX + dx, currentY, startZ + dz)
                    val state = level.getBlockState(pos)

                    if (state.isAir || !state.fluidState.isEmpty || state.canBeReplaced()) {
                        level.setBlock(pos, Blocks.NETHER_BRICKS.defaultBlockState(), 2)
                        currentY--
                    } else {
                        break
                    }
                }
            }
        }
    }
}
