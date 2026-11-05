package ru.rlyqaz.divinesouls.worldgen

import com.mojang.serialization.Codec
import net.minecraft.core.BlockPos
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import ru.rlyqaz.divinesouls.block.ModBlocks

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.21.1 NeoForge)
 * КЛАСС: EndTreeFeature (Генератор Деревьев Края в мире)
 * ИСПРАВЛЕНИЕ: Безопасный отступ от краев чанка убирает "far chunk",
 *              а радиус-детектор брёвен предотвращает слипание деревьев.
 * ====================================================================
 */
class EndTreeFeature(codec: Codec<NoneFeatureConfiguration>) : Feature<NoneFeatureConfiguration>(codec) {

    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val random = context.random()
        val originPos = context.origin()

        // БАГФИКС 1: Вычисляем начало текущего чанка
        val chunkStartX = originPos.x and -16
        val chunkStartZ = originPos.z and -16

        // Выбираем случайную точку, но строго С ОРАНИЧЕНИЕМ (от 4 до 11 блока внутри чанка)
        // Это сохраняет хаотичный рандом лесных групп, но запирает крону внутри безопасной зоны!
        val x = chunkStartX + 4 + random.nextInt(8)
        val z = chunkStartZ + 4 + random.nextInt(8)

        val surfacePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, BlockPos(x, 0, z))

        // Базовая проверка покрытия
        val blockUnder = level.getBlockState(surfacePos.below())
        if (!blockUnder.`is`(ModBlocks.END_GRASS_BLOCK.get())) {
            return false
        }

        // БАГФИКС 2: Проверка на слипание деревьев
        // Сканируем квадрат 7х7 вокруг точки спавна на уровне земли и ствола.
        // Если рядом уже есть кастомные бревна — отменяем спавн, чтобы деревья не росли друг в друге!
        val checkRadius = 3
        for (dx in -checkRadius..checkRadius) {
            for (dz in -checkRadius..checkRadius) {
                for (dy in 0..5) {
                    val checkPos = surfacePos.offset(dx, dy, dz)
                    if (level.getBlockState(checkPos).`is`(ModBlocks.END_WOOD_LOG.get())) {
                        return false // Слишком близко другое дерево, уходим
                    }
                }
            }
        }

        // Запускаем генерацию (передаем WorldGenLevel через каст, так как EndTree ждет именно его)
        return EndTree.growTree(level as WorldGenLevel, surfacePos, random)
    }
}
