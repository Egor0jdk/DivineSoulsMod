package ru.rlyqaz.divinesouls.worldgen

import com.mojang.serialization.Codec
import net.minecraft.core.BlockPos
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import ru.rlyqaz.divinesouls.block.ModBlocks

class AbyssDecorationFeature(codec: Codec<NoneFeatureConfiguration>) : Feature<NoneFeatureConfiguration>(codec) {

    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level: WorldGenLevel = context.level()
        val random = context.random()
        val origin = context.origin()

        val chunkStartX = origin.x and -16
        val chunkStartZ = origin.z and -16

        val stoneState = ModBlocks.ABYSSAL_STONE.get().defaultBlockState()
        val tuffState = ModBlocks.ABYSSAL_TUFF.get().defaultBlockState()
        val myceliumState = ModBlocks.ABYSSAL_MYCELIUM.get().defaultBlockState()
        val vineState = ModBlocks.ABYSSAL_VINE.get().defaultBlockState()
        val smallMushroomState = ModBlocks.ABYSSAL_MUSHROOM.get().defaultBlockState()

        // 1. Вкрапления Туфа Бездны (abyssal_tuff) внутри массива камня Бездны (4 жилы на чанк)
        repeat(4) {
            val vx = chunkStartX + 2 + random.nextInt(12)
            val vy = 20 + random.nextInt(215)
            val vz = chunkStartZ + 2 + random.nextInt(12)
            val rx = 2 + random.nextInt(3)
            val ry = 2 + random.nextInt(2)
            val rz = 2 + random.nextInt(3)

            for (dx in -rx..rx) {
                for (dy in -ry..ry) {
                    for (dz in -rz..rz) {
                        val norm = (dx.toDouble() / rx) * (dx.toDouble() / rx) +
                                (dy.toDouble() / ry) * (dy.toDouble() / ry) +
                                (dz.toDouble() / rz) * (dz.toDouble() / rz)
                        if (norm.compareTo(1.0) != 1) {
                            val p = BlockPos(vx + dx, vy + dy, vz + dz)
                            if (level.getBlockState(p).`is`(ModBlocks.ABYSSAL_STONE.get())) {
                                level.setBlock(p, tuffState, 2)
                            }
                        }
                    }
                }
            }
        }

        // 2. Блочные сталактиты (свисают с потолка и нижних граней парящих островов)
        repeat(6) {
            val sx = chunkStartX + 3 + random.nextInt(10)
            val sz = chunkStartZ + 3 + random.nextInt(10)
            val startY = 70 + random.nextInt(175)

            // Ищем нижнюю грань камня Бездны
            for (y in startY downTo 35) {
                val ceilingPos = BlockPos(sx, y, sz)
                if (level.getBlockState(ceilingPos).`is`(ModBlocks.ABYSSAL_STONE.get()) &&
                    level.isEmptyBlock(ceilingPos.below())
                ) {
                    val length = 8 + random.nextInt(15)
                    for (dy in 1..length) {
                        val tipPos = ceilingPos.below(dy)
                        if (!level.isEmptyBlock(tipPos)) break
                        val progress = dy.toDouble() / length.toDouble()
                        val blockToUse = if (random.nextFloat().compareTo(0.25f) == -1) tuffState else stoneState
                        level.setBlock(tipPos, blockToUse, 2)

                        // Утолщение у основания сталактита
                        if (progress.compareTo(0.45) == -1) {
                            val arms = listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)
                            for ((ox, oz) in arms) {
                                val sidePos = tipPos.offset(ox, 0, oz)
                                if (level.isEmptyBlock(sidePos) && random.nextFloat().compareTo(0.75f) == -1) {
                                    level.setBlock(sidePos, blockToUse, 2)
                                }
                            }
                        }
                    }
                    break
                }
            }
        }

        // 3. Светящиеся лозы Бездны (abyssal_vine) свисают сверху вниз (14 гирлянд на чанк)
        repeat(14) {
            val vx = chunkStartX + random.nextInt(16)
            val vz = chunkStartZ + random.nextInt(16)
            val scanStart = 45 + random.nextInt(205)

            for (y in scanStart downTo 25) {
                val anchor = BlockPos(vx, y, vz)
                val anchorState = level.getBlockState(anchor)
                if ((anchorState.`is`(ModBlocks.ABYSSAL_STONE.get()) || anchorState.`is`(ModBlocks.ABYSSAL_TUFF.get())) &&
                    level.isEmptyBlock(anchor.below())
                ) {
                    val vineLen = 4 + random.nextInt(13)
                    for (dy in 1..vineLen) {
                        val vPos = anchor.below(dy)
                        if (!level.isEmptyBlock(vPos)) break
                        level.setBlock(vPos, vineState, 2)
                    }
                    break
                }
            }
        }

        // 4. Грибные рощи Бездны (широкие поляны Мицелия Бездны + 3..6 гигантских грибов + малые грибы)
        if (random.nextFloat().compareTo(0.38f) == -1) {
            val gx = chunkStartX + 4 + random.nextInt(8)
            val gz = chunkStartZ + 4 + random.nextInt(8)
            val scanY = 50 + random.nextInt(160)

            var surfaceGround: BlockPos? = null
            for (y in scanY downTo 25) {
                val p = BlockPos(gx, y, gz)
                if (level.getBlockState(p).`is`(ModBlocks.ABYSSAL_STONE.get()) && level.isEmptyBlock(p.above())) {
                    surfaceGround = p
                    break
                }
            }

            if (surfaceGround != null) {
                val radius = 9 + random.nextInt(6) // Радиус 9..14 блоков (диаметр ~20-28 блоков!)
                val myceliumSpots = mutableListOf<BlockPos>()

                for (dx in -radius..radius) {
                    for (dz in -radius..radius) {
                        val distSq = dx * dx + dz * dz
                        // Рваный природный край поляны
                        val edgeJitter = random.nextInt(14)
                        if (distSq > radius * radius - edgeJitter) continue

                        for (dy in -3..3) {
                            val check = surfaceGround.offset(dx, dy, dz)
                            if (level.getBlockState(check).`is`(ModBlocks.ABYSSAL_STONE.get()) &&
                                level.isEmptyBlock(check.above())
                            ) {
                                level.setBlock(check, myceliumState, 2)
                                myceliumSpots.add(check)

                                if (random.nextFloat().compareTo(0.24f) == -1) {
                                    level.setBlock(check.above(), smallMushroomState, 2)
                                }
                                break
                            }
                        }
                    }
                }

                // Выращиваем 3..6 гигантских грибов по всей поляне
                if (myceliumSpots.isNotEmpty()) {
                    val giantCount = 3 + random.nextInt(4)
                    myceliumSpots.shuffle()
                    var grown = 0
                    for (spot in myceliumSpots) {
                        if (grown >= giantCount) break
                        val aboveSpot = spot.above()
                        val stateAbove = level.getBlockState(aboveSpot)
                        if (stateAbove.isAir || stateAbove.`is`(ModBlocks.ABYSSAL_MUSHROOM.get())) {
                            if (AbyssalMushroomFeature.growGiantMushroom(level, aboveSpot, random)) {
                                grown++
                            }
                        }
                    }
                }
            }
        }

        // 5. Стандартные жилы Руды Абиссалита (7 жил по 4–7 блоков на чанк внутри Камня/Туфа Бездны)
        val oreState = ModBlocks.ABYSSAL_ORE.get().defaultBlockState()
        val rawBlockState = ModBlocks.RAW_ABYSSAL_BLOCK.get().defaultBlockState()

        repeat(7) {
            var ox = chunkStartX + 2 + random.nextInt(12)
            var oy = 25 + random.nextInt(215)
            var oz = chunkStartZ + 2 + random.nextInt(12)
            val veinSize = 4 + random.nextInt(4)

            repeat(veinSize) {
                val pos = BlockPos(ox, oy, oz)
                val targetState = level.getBlockState(pos)
                if (targetState.`is`(ModBlocks.ABYSSAL_STONE.get()) || targetState.`is`(ModBlocks.ABYSSAL_TUFF.get())) {
                    level.setBlock(pos, oreState, 2)
                }
                ox += random.nextInt(3) - 1
                oy += random.nextInt(3) - 1
                oz += random.nextInt(3) - 1
            }
        }

        // 6. Редкая гигантская рудная жила в стиле пещер 1.18+ (Туф Бездны + Руда + целые блоки рудного абиссалита!)
        if (random.nextInt(18) == 0) {
            var vx = chunkStartX + 4 + random.nextInt(8)
            var vy = 45 + random.nextInt(175)
            var vz = chunkStartZ + 4 + random.nextInt(8)
            val steps = 16 + random.nextInt(10)

            repeat(steps) {
                val r = 2 + random.nextInt(2)
                for (dx in -r..r) {
                    for (dy in -r..r) {
                        for (dz in -r..r) {
                            if (dx * dx + dy * dy + dz * dz > r * r + 1) continue
                            val p = BlockPos(vx + dx, vy + dy, vz + dz)
                            val st = level.getBlockState(p)
                            if (st.`is`(ModBlocks.ABYSSAL_STONE.get()) || st.`is`(ModBlocks.ABYSSAL_TUFF.get())) {
                                val roll = random.nextFloat()
                                val blockToSet = when {
                                    roll < 0.08f -> rawBlockState // 8% — целый блок рудного абиссалита!
                                    roll < 0.42f -> oreState      // 34% — руда абиссалита
                                    else -> tuffState             // 58% — матрица из туфа Бездны
                                }
                                level.setBlock(p, blockToSet, 2)
                            }
                        }
                    }
                }
                vx += random.nextInt(3) - 1
                vy += random.nextInt(3) - 1
                vz += random.nextInt(3) - 1
            }
        }

        // 7. Сталагмиты Бездны (abyssal_stalagmite) на полу из Камня Бездны (группами по 1–4 шипа)
        val stalagmiteState = ModBlocks.ABYSSAL_STALAGMITE.get().defaultBlockState()
        repeat(5) {
            val sx = chunkStartX + 2 + random.nextInt(12)
            val sz = chunkStartZ + 2 + random.nextInt(12)
            val scanStart = 35 + random.nextInt(195)

            for (y in scanStart downTo 22) {
                val floorPos = BlockPos(sx, y, sz)
                if (level.getBlockState(floorPos).`is`(ModBlocks.ABYSSAL_STONE.get()) &&
                    level.isEmptyBlock(floorPos.above())
                ) {
                    val clusterCount = 1 + random.nextInt(4)
                    repeat(clusterCount) {
                        val ox = sx + random.nextInt(5) - 2
                        val oz = sz + random.nextInt(5) - 2
                        for (dy in -2..2) {
                            val checkGround = BlockPos(ox, y + dy, oz)
                            if (level.getBlockState(checkGround).`is`(ModBlocks.ABYSSAL_STONE.get()) &&
                                level.isEmptyBlock(checkGround.above())
                            ) {
                                level.setBlock(checkGround.above(), stalagmiteState, 2)
                                break
                            }
                        }
                    }
                    break
                }
            }
        }

        return true
    }
}
