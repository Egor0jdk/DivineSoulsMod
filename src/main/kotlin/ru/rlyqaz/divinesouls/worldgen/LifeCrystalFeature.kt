package ru.rlyqaz.divinesouls.worldgen

import com.mojang.serialization.Codec
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import ru.rlyqaz.divinesouls.block.ModBlocks

class LifeCrystalFeature(codec: Codec<NoneFeatureConfiguration>) : Feature<NoneFeatureConfiguration>(codec) {

    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val random = context.random()
        val origin = context.origin()

        val chunkStartX = origin.x and -16
        val chunkStartZ = origin.z and -16
        val centerX = chunkStartX + 4 + random.nextInt(8)
        val centerZ = chunkStartZ + 4 + random.nextInt(8)

        val surfacePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, BlockPos(centerX, 0, centerZ))
        if (!isValidBase(level.getBlockState(surfacePos.below()))) return false

        // Запрещаем спавн ближе 24 блоков от другого кластера кристаллов жизни
        val minSpacing = 24
        for (dx in -minSpacing..minSpacing step 2) {
            for (dz in -minSpacing..minSpacing step 2) {
                for (dy in -6..6) {
                    val checkPos = surfacePos.offset(dx, dy, dz)
                    if (level.getBlockState(checkPos).`is`(ModBlocks.LIFE_CRYSTAL_CLUSTER.get())) {
                        return false
                    }
                }
            }
        }

        // Ставим 1 главный кристалл и с шансом 55% второй прямо рядом (1-2 блока в сторону)
        val crystalState = ModBlocks.LIFE_CRYSTAL_CLUSTER.get().defaultBlockState()
        level.setBlock(surfacePos, crystalState, 2)

        if (random.nextFloat() < 0.55f) {
            val offsets = listOf(
                1 to 0, -1 to 0, 0 to 1, 0 to -1,
                1 to 1, -1 to 1, 1 to -1, -1 to -1
            ).shuffled()

            for ((ox, oz) in offsets) {
                val secondSurface = level.getHeightmapPos(
                    Heightmap.Types.WORLD_SURFACE_WG,
                    BlockPos(centerX + ox, 0, centerZ + oz)
                )
                if (kotlin.math.abs(secondSurface.y - surfacePos.y) <= 1 &&
                    level.getBlockState(secondSurface).isAir &&
                    isValidBase(level.getBlockState(secondSurface.below()))
                ) {
                    level.setBlock(secondSurface, crystalState, 2)
                    break
                }
            }
        }

        return true
    }

    private fun isValidBase(state: net.minecraft.world.level.block.state.BlockState): Boolean {
        return state.`is`(Blocks.END_STONE) || state.`is`(ModBlocks.END_GRASS_BLOCK.get())
    }
}
