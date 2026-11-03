package ru.rlyqaz.divinesouls.worldgen

import com.mojang.serialization.Codec
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import ru.rlyqaz.divinesouls.block.ModBlocks

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.21.1 NeoForge)
 * КЛАСС: EndOasisFeature (Генератор Оазисов Края)
 * ====================================================================
 */
class EndOasisFeature(codec: Codec<NoneFeatureConfiguration>) : Feature<NoneFeatureConfiguration>(codec) {

    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val random = context.random()
        val originPos = context.origin()

        val x = originPos.x + random.nextInt(16)
        val z = originPos.z + random.nextInt(16)

        // В 1.21.1 внутренний класс хитмапа называется Heightmap.Types вместо Type
        val surfacePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, BlockPos(x, 0, z))
        val groundPos = surfacePos.below()

        if (!level.getBlockState(groundPos).`is`(Blocks.END_STONE)) {
            return false
        }

        val radius = 12 + random.nextInt(5)

        for (dx in -radius..radius) {
            for (dz in -radius..radius) {
                if (dx * dx + dz * dz <= radius * radius) {
                    val currentGround = groundPos.offset(dx, 0, dz)

                    if (level.getBlockState(currentGround).`is`(Blocks.END_STONE) && level.isEmptyBlock(currentGround.above())) {
                        level.setBlock(currentGround, ModBlocks.END_GRASS_BLOCK.get().defaultBlockState(), 2)

                        val airPos = currentGround.above()
                        if (level.isEmptyBlock(airPos) && random.nextFloat() < 0.35f) {
                            level.setBlock(airPos, ModBlocks.END_BUSH_GRASS.get().defaultBlockState(), 2)
                        }
                    }
                }
            }
        }

        EndTree.generateTreeSafely(level, surfacePos, random)
        return true
    }
}
