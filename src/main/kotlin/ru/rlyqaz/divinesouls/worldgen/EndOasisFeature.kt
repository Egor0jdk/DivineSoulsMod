package ru.rlyqaz.divinesouls.worldgen

import com.mojang.serialization.Codec
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import ru.rlyqaz.divinesouls.block.ModBlocks
import kotlin.math.abs

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.21.1 NeoForge)
 * КЛАСС: EndOasisFeature (Генератор Покрытия Оазиса Края)
 * ИСПРАВЛЕНИЕ: Допуск генерации в пределах соседних чанков (дистанция <= 1)
 *              возвращает бесшовные круглые поляны без ошибок far chunk.
 * ====================================================================
 */
class EndOasisFeature(codec: Codec<NoneFeatureConfiguration>) : Feature<NoneFeatureConfiguration>(codec) {

    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val random = context.random()
        val originPos = context.origin()

        // Свободный хаотичный рандом спавна по чанку
        val x = originPos.x + random.nextInt(16)
        val z = originPos.z + random.nextInt(16)

        val surfacePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, BlockPos(x, 0, z))
        val groundPos = surfacePos.below()

        if (!level.getBlockState(groundPos).`is`(Blocks.END_STONE)) {
            return false
        }

        // Большой, красивый RPG-радиус поляны (от 12 до 16 блоков)
        val radius = 12 + random.nextInt(5)

        for (dx in -radius..radius) {
            for (dz in -radius..radius) {
                if (dx * dx + dz * dz <= radius * radius) {
                    val currentGround = groundPos.offset(dx, 0, dz)

                    // ВЫЧИСЛЯЕМ ДИСТАНЦИЮ В ЧАНКАХ:
                    // Находим, на сколько чанков целевой блок отдалился от центрального чанка originPos
                    val chunkDistX = abs((currentGround.x shr 4) - (originPos.x shr 4))
                    val chunkDistZ = abs((currentGround.z shr 4) - (originPos.z shr 4))

                    // БАГФИКС: Разрешаем генерацию только внутри текущего чанка и его прямых соседей
                    // (дистанция не больше 1 чанка). Это возвращает круглую бесшовную форму полянам,
                    // но полностью страхует от вылета за границы региона 3х3 чанка!
                    if (chunkDistX <= 1 && chunkDistZ <= 1) {
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
        }
        return true
    }
}
