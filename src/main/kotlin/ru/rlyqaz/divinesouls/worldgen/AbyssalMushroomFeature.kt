package ru.rlyqaz.divinesouls.worldgen

import com.mojang.serialization.Codec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import ru.rlyqaz.divinesouls.block.ModBlocks
import kotlin.math.abs

class AbyssalMushroomFeature(codec: Codec<NoneFeatureConfiguration>) : Feature<NoneFeatureConfiguration>(codec) {

    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val pos = context.origin()
        val random = context.random()

        if (!level.getBlockState(pos.below()).`is`(ModBlocks.ABYSSAL_MYCELIUM.get())) return false
        return growGiantMushroom(level, pos, random)
    }

    companion object {
        fun growGiantMushroom(level: LevelAccessor, pos: BlockPos, random: RandomSource): Boolean {
            val height = 6 + random.nextInt(5) // 6..10 блоков высотой
            val capRadius = 2 // Аккуратная шляпка 5x5 с закруглёнными углами (как на 2-м скриншоте)

            for (dy in 1..(height + 1)) {
                val check = pos.above(dy)
                val state = level.getBlockState(check)
                if (!state.isAir && !state.`is`(ModBlocks.ABYSSAL_MUSHROOM.get()) && !state.`is`(ModBlocks.ABYSSAL_VINE.get())) {
                    return false
                }
            }

            level.removeBlock(pos, false)

            val stemState = ModBlocks.ABYSSAL_MUSHROOM_STEM.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y)
            val capState = ModBlocks.ABYSSAL_MUSHROOM_CAP.get().defaultBlockState()

            // Ножка гриба
            for (dy in 0 until height) {
                level.setBlock(pos.above(dy), stemState, 2)
            }

            val topCenter = pos.above(height)

            // 1. Верхняя шапка шляпки (3x3 плоская верхушка)
            for (dx in -1..1) {
                for (dz in -1..1) {
                    val topPos = topCenter.offset(dx, 0, dz)
                    if (level.isEmptyBlock(topPos)) {
                        level.setBlock(topPos, capState, 2)
                    }
                }
            }

            // 2. Свисающий ободок шляпки (на 1 блок ниже, стенками шириной 3 блока с 4 сторон, без углов)
            for (dx in -capRadius..capRadius) {
                for (dz in -capRadius..capRadius) {
                    val absX = abs(dx)
                    val absZ = abs(dz)
                    val isSideRim = (absX == capRadius && absZ <= 1) || (absZ == capRadius && absX <= 1)
                    if (isSideRim) {
                        val rimPos = topCenter.offset(dx, -1, dz)
                        if (level.isEmptyBlock(rimPos)) {
                            level.setBlock(rimPos, capState, 2)
                        }
                    }
                }
            }

            return true
        }
    }
}
