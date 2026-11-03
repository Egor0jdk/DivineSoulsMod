package ru.rlyqaz.divinesouls.item.custom

import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.EndPortalFrameBlock
import kotlin.math.abs

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.21.1 NeoForge)
 * КЛАСС: WitherCoreItem (Ядро Визера)
 * ЛОГИКА: Ручная активация портала Энда 3х3 после проверки всех 12 глаз
 * ====================================================================
 */
class WitherCoreItem(properties: Properties) : Item(properties) {

    override fun useOn(context: UseOnContext): InteractionResult {
        val level = context.level
        val pos = context.clickedPos // Координаты рамки, по которой кликнули

        if (!level.isClientSide) {
            val clickedState = level.getBlockState(pos)

            // Проверяем клик Ядром по заполненной рамке портала Края
            if (clickedState.`is`(Blocks.END_PORTAL_FRAME) && clickedState.getValue(EndPortalFrameBlock.HAS_EYE)) {

                // Твой алгоритм поиска центра: сканируем область 7х7 вокруг кликнутого блока
                var centerPos: BlockPos? = null
                for (x in -3..3) {
                    for (z in -3..3) {
                        val checkPos = pos.offset(x, 0, z)

                        // Центр портала — это воздух, который со всех 4 сторон на расстоянии 2 блоков окружен рамками
                        if (level.isEmptyBlock(checkPos)) {
                            if (level.getBlockState(checkPos.offset(2, 0, 0)).`is`(Blocks.END_PORTAL_FRAME) &&
                                level.getBlockState(checkPos.offset(-2, 0, 0)).`is`(Blocks.END_PORTAL_FRAME) &&
                                level.getBlockState(checkPos.offset(0, 0, 2)).`is`(Blocks.END_PORTAL_FRAME) &&
                                level.getBlockState(checkPos.offset(0, 0, -2)).`is`(Blocks.END_PORTAL_FRAME)) {
                                centerPos = checkPos
                                break
                            }
                        }
                    }
                    if (centerPos != null) break
                }

                // Если геометрический центр структуры успешно найден — делаем финальный чек на наличие ВСЕХ глаз
                if (centerPos != null) {
                    var allEyesPresent = true

                    // Сканируем кольцо портала в радиусе 2 блоков от найденного центра
                    for (x in -2..2) {
                        for (z in -2..2) {
                            // Проверяем только края (само кольцо рамок), пропуская внутренний квадрат 3х3
                            if (abs(x) == 2 || abs(z) == 2) {
                                val framePos = centerPos.offset(x, 0, z)
                                val frameState = level.getBlockState(framePos)

                                // Если в кольце структуры нашелся блок рамки, но в нём НЕТ глаза — ломаем активацию
                                if (frameState.`is`(Blocks.END_PORTAL_FRAME)) {
                                    if (!frameState.getValue(EndPortalFrameBlock.HAS_EYE)) {
                                        allEyesPresent = false
                                        break
                                    }
                                }
                            }
                        }
                        if (!allEyesPresent) break
                    }

                    // Только если все глаза на месте — заливаем ровный квадрат 3х3 блоков портала
                    if (allEyesPresent) {
                        for (x in -1..1) {
                            for (z in -1..1) {
                                val target = centerPos.offset(x, 0, z)
                                if (level.isEmptyBlock(target)) {
                                    level.setBlock(target, Blocks.END_PORTAL.defaultBlockState(), 2)
                                }
                            }
                        }

                        val player = context.player
                        if (player != null && !player.isCreative) {
                            context.itemInHand.shrink(1)
                        }
                        return InteractionResult.SUCCESS
                    }
                }
            }
        }
        return InteractionResult.PASS
    }
}
