package ru.rlyqaz.divinesouls;

import net.minecraft.block.Blocks;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WitherCoreItem extends Item {

    public WitherCoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos(); // Координаты рамки, по которой кликнули

        if (!world.isClientSide()) {
            // Проверяем клик Ядром по заполненной рамке портала Края
            if (world.getBlockState(pos).getBlock() == Blocks.END_PORTAL_FRAME &&
                    world.getBlockState(pos).getValue(EndPortalFrameBlock.HAS_EYE)) {

                // Твой алгоритм поиска центра: сканируем область 5х5 вокруг кликнутого блока
                BlockPos centerPos = null;
                for (int x = -3; x <= 3; x++) {
                    for (int z = -3; z <= 3; z++) {
                        BlockPos checkPos = pos.offset(x, 0, z);
                        // Центр портала — это воздух, который со всех 4 сторон на расстоянии 2 блоков окружен рамками
                        if (world.isEmptyBlock(checkPos)) {
                            if (world.getBlockState(checkPos.offset(2, 0, 0)).getBlock() == Blocks.END_PORTAL_FRAME &&
                                    world.getBlockState(checkPos.offset(-2, 0, 0)).getBlock() == Blocks.END_PORTAL_FRAME &&
                                    world.getBlockState(checkPos.offset(0, 0, 2)).getBlock() == Blocks.END_PORTAL_FRAME &&
                                    world.getBlockState(checkPos.offset(0, 0, -2)).getBlock() == Blocks.END_PORTAL_FRAME) {
                                centerPos = checkPos;
                                break;
                            }
                        }
                    }
                    if (centerPos != null) break;
                }

                // Если геометрический центр структуры успешно найден — делаем финальный чек на наличие ВСЕХ глаз
                if (centerPos != null) {
                    boolean allEyesPresent = true;

                    // Сканируем кольцо портала в радиусе 2 блоков от найденного центра
                    for (int x = -2; x <= 2; x++) {
                        for (int z = -2; z <= 2; z++) {
                            // Проверяем только края (само кольцо рамок), пропуская внутренний квадрат 3х3
                            if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                                BlockPos framePos = centerPos.offset(x, 0, z);
                                net.minecraft.block.BlockState frameState = world.getBlockState(framePos);

                                // Если в кольце структуры нашелся блок рамки, но в нём НЕТ глаза — ломаем активацию
                                if (frameState.getBlock() == Blocks.END_PORTAL_FRAME) {
                                    if (!frameState.getValue(EndPortalFrameBlock.HAS_EYE)) {
                                        allEyesPresent = false;
                                        break;
                                    }
                                }
                            }
                        }
                        if (!allEyesPresent) break;
                    }

                    // Только если все глаза на месте — заливаем ровный квадрат 3х3 блоков портала
                    if (allEyesPresent) {
                        for (int x = -1; x <= 1; x++) {
                            for (int z = -1; z <= 1; z++) {
                                BlockPos target = centerPos.offset(x, 0, z);
                                if (world.isEmptyBlock(target)) {
                                    world.setBlock(target, Blocks.END_PORTAL.defaultBlockState(), 2);
                                }
                            }
                        }

                        if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
                            context.getItemInHand().shrink(1);
                        }
                        return ActionResultType.SUCCESS;
                    }
                }
            }
        }
        return ActionResultType.PASS;
    }
}
