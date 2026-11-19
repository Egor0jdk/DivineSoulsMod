package ru.rlyqaz.divinesouls.worldgen

import com.mojang.serialization.Codec
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.ChestBlockEntity
import net.minecraft.world.level.block.entity.SpawnerBlockEntity
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import net.minecraft.world.level.storage.loot.LootTable
import ru.rlyqaz.divinesouls.entity.ModEntities
import kotlin.math.cos
import kotlin.math.sin

class SpiderNestFeature(codec: Codec<NoneFeatureConfiguration>) : Feature<NoneFeatureConfiguration>(codec) {

    companion object {
        private val JUNGLE_LOOT: ResourceKey<LootTable> = ResourceKey.create(
            Registries.LOOT_TABLE,
            ResourceLocation.withDefaultNamespace("chests/jungle_temple")
        )
    }

    private data class Lobe(val cx: Int, val cy: Int, val cz: Int, val rx: Double, val ry: Double, val rz: Double)

    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level: WorldGenLevel = context.level()
        val origin: BlockPos = context.origin()
        val random: RandomSource = context.random()

        // Проверяем, что место глубоко под землёй в твёрдой породе
        if (!level.getBlockState(origin).isSolidRender(level, origin)) return false
        if (!level.getBlockState(origin.above(5)).isSolidRender(level, origin.above(5))) return false

        // Собираем рваную асимметричную пещеру из главного зала + 3 случайных ответвлений
        val lobes = mutableListOf(
            Lobe(0, 0, 0, 6.5, 4.2, 6.5)
        )
        for (i in 0 until 3) {
            val angle = random.nextDouble() * Math.PI * 2.0
            val dist = 4.5 + random.nextDouble() * 3.5
            lobes.add(
                Lobe(
                    cx = (cos(angle) * dist).toInt(),
                    cy = random.nextIntBetweenInclusive(-1, 2),
                    cz = (sin(angle) * dist).toInt(),
                    rx = 3.8 + random.nextDouble() * 2.2,
                    ry = 2.8 + random.nextDouble() * 1.4,
                    rz = 3.8 + random.nextDouble() * 2.2
                )
            )
        }

        val boundXZ = 12
        val boundY = 7

        // 1. Вырезаем кривоватую многокамерную каверну и оформляем стены
        for (dx in -boundXZ..boundXZ) {
            for (dy in -boundY..boundY) {
                for (dz in -boundXZ..boundXZ) {
                    var minNormDist = 999.0
                    for (lobe in lobes) {
                        val nx = (dx - lobe.cx) / lobe.rx
                        val ny = (dy - lobe.cy) / lobe.ry
                        val nz = (dz - lobe.cz) / lobe.rz
                        val d = nx * nx + ny * ny + nz * nz
                        if (d.compareTo(minNormDist) == -1) {
                            minNormDist = d
                        }
                    }

                    // Добавляем грубый природный шум, чтобы стены и пол были рваными, а не гладкими
                    val jaggedNoise = (random.nextDouble() - 0.5) * 0.38
                    val finalDist = minNormDist + jaggedNoise

                    val targetPos = origin.offset(dx, dy, dz)
                    val currentState = level.getBlockState(targetPos)
                    if (currentState.`is`(Blocks.BEDROCK) || currentState.hasBlockEntity()) continue

                    if (finalDist.compareTo(0.82) == -1) {
                        level.setBlock(targetPos, Blocks.CAVE_AIR.defaultBlockState(), 2)
                    } else if (finalDist.compareTo(1.22) == -1) {
                        val wallBlock = when (random.nextInt(6)) {
                            0, 1 -> Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                            2 -> Blocks.COBBLED_DEEPSLATE.defaultBlockState()
                            3 -> Blocks.ROOTED_DIRT.defaultBlockState()
                            4 -> Blocks.MOSS_BLOCK.defaultBlockState()
                            else -> Blocks.TUFF.defaultBlockState()
                        }
                        level.setBlock(targetPos, wallBlock, 2)
                    }
                }
            }
        }

        // 2. Развешиваем густую паутину по углам, стенам и свисающие пряди с потолка
        for (dx in -boundXZ + 1 until boundXZ) {
            for (dy in -boundY + 1 until boundY) {
                for (dz in -boundXZ + 1 until boundXZ) {
                    val pos = origin.offset(dx, dy, dz)
                    if (!level.getBlockState(pos).isAir) continue

                    val touchesCeiling = level.getBlockState(pos.above()).isSolidRender(level, pos.above())
                    val touchesWall = level.getBlockState(pos.north()).isSolidRender(level, pos.north()) ||
                            level.getBlockState(pos.south()).isSolidRender(level, pos.south()) ||
                            level.getBlockState(pos.east()).isSolidRender(level, pos.east()) ||
                            level.getBlockState(pos.west()).isSolidRender(level, pos.west())

                    if (touchesCeiling && random.nextFloat().compareTo(0.55f) == -1) {
                        level.setBlock(pos, Blocks.COBWEB.defaultBlockState(), 2)
                        // Тянем прядь паутины вниз на 1–2 блока от потолка
                        if (level.getBlockState(pos.below()).isAir && random.nextFloat().compareTo(0.5f) == -1) {
                            level.setBlock(pos.below(), Blocks.COBWEB.defaultBlockState(), 2)
                        }
                    } else if (touchesWall && random.nextFloat().compareTo(0.42f) == -1) {
                        level.setBlock(pos, Blocks.COBWEB.defaultBlockState(), 2)
                    } else if (random.nextFloat().compareTo(0.06f) == -1) {
                        level.setBlock(pos, Blocks.COBWEB.defaultBlockState(), 2)
                    }
                }
            }
        }

        // 3. Находим пол в центре главного зала и ставим спавнер Джунглевых пауков
        val centerFloor = findFloor(level, origin, boundY)
        if (centerFloor != null) {
            level.setBlock(centerFloor.below(), Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 2)
            level.setBlock(centerFloor, Blocks.SPAWNER.defaultBlockState(), 2)
            val spawnerEntity = level.getBlockEntity(centerFloor)
            if (spawnerEntity is SpawnerBlockEntity) {
                spawnerEntity.setEntityId(ModEntities.JUNGLE_SPIDER.get(), random)
            }
        }

        // 4. В одной из дальних боковых ниш прячем сундук с сокровищами джунглей под паутиной
        val sideLobe = lobes[random.nextIntBetweenInclusive(1, lobes.size - 1)]
        val chestSearchPos = origin.offset(sideLobe.cx, sideLobe.cy, sideLobe.cz)
        val chestFloor = findFloor(level, chestSearchPos, boundY)
        if (chestFloor != null && chestFloor != centerFloor) {
            level.setBlock(chestFloor.below(), Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 2)
            level.setBlock(chestFloor, Blocks.CHEST.defaultBlockState(), 2)
            val chestEntity = level.getBlockEntity(chestFloor)
            if (chestEntity is ChestBlockEntity) {
                chestEntity.setLootTable(JUNGLE_LOOT, random.nextLong())
            }
            if (level.getBlockState(chestFloor.above()).isAir) {
                level.setBlock(chestFloor.above(), Blocks.COBWEB.defaultBlockState(), 2)
            }
        }

        return true
    }

    private fun findFloor(level: WorldGenLevel, start: BlockPos, maxDrop: Int): BlockPos? {
        for (dy in 0..maxDrop) {
            val checkPos = start.below(dy)
            if (level.getBlockState(checkPos).isAir && level.getBlockState(checkPos.below()).isSolidRender(level, checkPos.below())) {
                return checkPos
            }
        }
        return null
    }
}
