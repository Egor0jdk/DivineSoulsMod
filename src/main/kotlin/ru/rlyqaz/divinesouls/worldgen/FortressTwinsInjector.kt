package ru.rlyqaz.divinesouls.worldgen

import net.minecraft.core.BlockPos
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

            // Находим самый северный край моста/площадки крепости
            val bridgeEdgePos = findNorthBridgeEdge(level, task.searchX, task.searchZ, task.minY, task.maxY) ?: continue

            val template = level.structureManager.get(ARENA_TEMPLATE_ID).getOrNull() ?: continue
            val size = template.size // size.x = 14 (центр между 6 и 7)

            // Опустили на 1 блок ниже (bridgeEdgePos.y - 2) и сдвинули на 1 блок назад на север (bridgeEdgePos.z - size.z)
            val placeOrigin = BlockPos(
                bridgeEdgePos.x - (size.x / 2),
                bridgeEdgePos.y - 1, // Возвращаем высоту ровно в уровень с мостом крепости
                bridgeEdgePos.z - size.z
            )

            val boxStartX = placeOrigin.x
            val boxStartZ = placeOrigin.z + 5

            // 1. Очищаем внутреннее пространство самого зала (12x6x12) от незерака
            for (ix in 1..12) {
                for (iy in 1..6) {
                    for (iz in 1..12) {
                        level.setBlock(
                            BlockPos(boxStartX + ix, placeOrigin.y + iy, boxStartZ + iz),
                            Blocks.AIR.defaultBlockState(),
                            2
                        )
                    }
                }
            }

            // 2. Вставляем структуру (игнорируем воздух шаблона, чтобы не ломать существующий мост)
            val settings = StructurePlaceSettings()
                .setRotation(Rotation.NONE)
                .setMirror(Mirror.NONE)
                .setIgnoreEntities(true)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR)

            template.placeInWorld(level, placeOrigin, placeOrigin, settings, level.random, 2)

            // 3. Разделённая расчистка + замена выпирающих заборов на уровне пола на цельный адский кирпич
            val entranceCenterX = placeOrigin.x + (size.x / 2)
            val bridgeStartZ = boxStartZ + 13
            val bridgeEndZ = placeOrigin.z + size.z

            for (z in bridgeStartZ..bridgeEndZ) {
                if (z == bridgeStartZ) {
                    // Сам дверной проём в стене арены: 2 в ширину (-1..0) и 3 в высоту (2..4)
                    for (dx in -1..0) {
                        for (dy in 2..4) {
                            val clearPos = BlockPos(entranceCenterX + dx, placeOrigin.y + dy, z)
                            if (!level.getBlockState(clearPos).isAir) {
                                level.setBlock(clearPos, Blocks.AIR.defaultBlockState(), 2)
                            }
                        }
                    }
                } else {
                    // Вся площадка моста дальше: квадрат 4 в ширину (-2..1) и 4 в высоту (2..5)
                    for (dx in -2..1) {
                        for (dy in 2..5) {
                            val clearPos = BlockPos(entranceCenterX + dx, placeOrigin.y + dy, z)
                            if (!level.getBlockState(clearPos).isAir) {
                                level.setBlock(clearPos, Blocks.AIR.defaultBlockState(), 2)
                            }
                        }

                        // Если на уровне пола (dy = 1) торчит забор крепости — меняем его на обычный блок адского кирпича!
                        val floorLevelPos = BlockPos(entranceCenterX + dx, placeOrigin.y + 1, z)
                        if (level.getBlockState(floorLevelPos).`is`(Blocks.NETHER_BRICK_FENCE)) {
                            level.setBlock(floorLevelPos, Blocks.NETHER_BRICKS.defaultBlockState(), 2)
                        }
                    }
                }
            }

            // 4. Строим 4 опорные колонны 3x3 по углам основной коробки вниз до полного упора в дно
            val boxEndX = boxStartX + 13
            val boxEndZ = boxStartZ + 13
            val floorY = placeOrigin.y

            val pillarCorners = listOf(
                Pair(boxStartX, boxStartZ),
                Pair(boxEndX - 2, boxStartZ),
                Pair(boxStartX, boxEndZ - 2),
                Pair(boxEndX - 2, boxEndZ - 2)
            )

            for ((cornerX, cornerZ) in pillarCorners) {
                buildSupportPillar3x3(level, cornerX, floorY - 1, cornerZ)
            }

            // 5. Спавним Близнецов Иссушения ровно в центре зала
            val bossSpawnX = boxStartX + 7.0
            val bossSpawnY = floorY + 1.0
            val bossSpawnZ = boxStartZ + 7.0

            val boss = ModEntities.TWIN_WITHER_SKELETON.get().create(level)
            if (boss != null) {
                boss.moveTo(bossSpawnX, bossSpawnY, bossSpawnZ, 0.0f, 0.0f)
                boss.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(bossSpawnX, bossSpawnY, bossSpawnZ)), MobSpawnType.STRUCTURE, null)
                boss.setPersistenceRequired()
                level.addFreshEntity(boss)
            }

            savedData.markFortressProcessed(task.fortressOrigin)
            DivineSoulsMod.LOGGER.info("Divine Souls: Generated Twins Arena attached cleanly to bridge edge at $bridgeEdgePos")
        }
    }

    // Ищет самый северный крайний блок моста/площадки крепости (где дальше на север уже нет кирпича крепости)
    private fun findNorthBridgeEdge(level: ServerLevel, centerX: Int, centerZ: Int, minY: Int, maxY: Int): BlockPos? {
        val startY = maxY.coerceAtMost(95)
        val endY = minY.coerceAtLeast(45)

        for (dx in -8..8) {
            for (dz in -8..8) {
                for (y in startY downTo endY) {
                    val pos = BlockPos(centerX + dx, y, centerZ + dz)
                    if (!level.getBlockState(pos).`is`(Blocks.NETHER_BRICKS)) continue

                    // Над краем моста должно быть минимум 3 блока воздуха
                    if (!level.getBlockState(pos.above()).isAir ||
                        !level.getBlockState(pos.above(2)).isAir ||
                        !level.getBlockState(pos.above(3)).isAir
                    ) continue

                    // Проверяем, что это именно КРАЙ моста с северной стороны:
                    // на 1 блок севернее (pos.north(1)) уже нет пола крепости (там воздух или забор)
                    val oneStepNorth = pos.north(1)
                    if (level.getBlockState(oneStepNorth).`is`(Blocks.NETHER_BRICKS)) continue

                    // А на 2–6 блоков севернее нет других коридоров крепости, в которые мы могли бы врезаться
                    var clearNorth = true
                    for (dist in 2..6) {
                        val checkNorth = pos.north(dist)
                        val state = level.getBlockState(checkNorth)
                        if (state.`is`(Blocks.NETHER_BRICKS) || state.`is`(Blocks.NETHER_BRICK_FENCE)) {
                            clearNorth = false
                            break
                        }
                    }
                    if (clearNorth) {
                        return pos
                    }
                }
            }
        }
        return null
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
