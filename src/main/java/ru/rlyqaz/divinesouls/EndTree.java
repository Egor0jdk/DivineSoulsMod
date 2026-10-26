package ru.rlyqaz.divinesouls;

import net.minecraft.block.BlockState;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.trees.Tree;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;
import javax.annotation.Nullable;

public class EndTree extends Tree {

    @Nullable
    @Override
    protected ConfiguredFeature<BaseTreeFeatureConfig, ?> getConfiguredFeature(Random rand, boolean largeHive) {
        return null;
    }

    @Override
    public boolean growTree(ServerWorld world, ChunkGenerator chunkGenerator, BlockPos pos, BlockState state, Random rand) {
        world.removeBlock(pos, false);

        BlockState logState = ModBlocks.END_WOOD_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState leavesState = ModBlocks.END_WOOD_LEAVES.get().defaultBlockState();

        Direction mainDir = rand.nextBoolean() ? Direction.NORTH : Direction.EAST;
        Direction oppDir = mainDir.getOpposite();

        // Слой 1 и 2: Плюсики из 5 блоков
        for (int h = 0; h <= 1; h++) {
            BlockPos centerPos = pos.above(h);
            if (isReplaceable(world, centerPos)) world.setBlock(centerPos, logState, 2);

            Direction[] dirs = { Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST };
            for (Direction d : dirs) {
                BlockPos rootPos = centerPos.relative(d);
                if (isReplaceable(world, rootPos)) world.setBlock(rootPos, logState, 2);
            }
        }

        // Слой 3, 4, 5: Чистый одиночный ствол
        for (int h = 2; h <= 4; h++) {
            BlockPos trunkPos = pos.above(h);
            if (isReplaceable(world, trunkPos)) world.setBlock(trunkPos, logState, 2);
        }

        // Слой 6: Начало разделения веток
        BlockPos layer6Center = pos.above(5);
        BlockPos branch1_L6 = layer6Center.relative(mainDir);
        BlockPos branch2_L6 = layer6Center.relative(oppDir);
        if (isReplaceable(world, branch1_L6)) world.setBlock(branch1_L6, logState, 2);
        if (isReplaceable(world, branch2_L6)) world.setBlock(branch2_L6, logState, 2);

        // Слой 7: Дальний разнос веток
        BlockPos layer7Center = pos.above(6);
        BlockPos branch1_L7 = layer7Center.relative(mainDir, 2);
        BlockPos branch2_L7 = layer7Center.relative(oppDir, 2);
        if (isReplaceable(world, branch1_L7)) world.setBlock(branch1_L7, logState, 2);
        if (isReplaceable(world, branch2_L7)) world.setBlock(branch2_L7, logState, 2);

        // Маленькая шапка листвы над левой веткой
        BlockPos smallCapCenter = branch1_L7.above();
        if (isReplaceable(world, smallCapCenter)) world.setBlock(smallCapCenter, logState, 2);
        buildLeavesCap(world, smallCapCenter, leavesState, false, 2);

        // Большая шапка листвы над правой веткой
        BlockPos bigCapCenter = branch2_L7.above(2);
        BlockPos bigCapSupport = branch2_L7.above();
        if (isReplaceable(world, bigCapSupport)) world.setBlock(bigCapSupport, logState, 2);
        if (isReplaceable(world, bigCapCenter)) world.setBlock(bigCapCenter, logState, 2);
        buildLeavesCap(world, bigCapCenter, leavesState, true, 2);

        return true;
    }
    public static void generateTreeSafely(ISeedReader world, BlockPos pos, Random rand) {
        BlockState logState = ModBlocks.END_WOOD_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState leavesState = ModBlocks.END_WOOD_LEAVES.get().defaultBlockState();

        Direction mainDir = rand.nextBoolean() ? Direction.NORTH : Direction.EAST;
        Direction oppDir = mainDir.getOpposite();

        // Слой 1 и 2: Плюсики из 5 блоков
        for (int h = 0; h <= 1; h++) {
            BlockPos centerLog = pos.above(h);
            if (world.isEmptyBlock(centerLog)) world.setBlock(centerLog, logState, 18);

            Direction[] dirs = { Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST };
            for (Direction d : dirs) {
                BlockPos rootPos = centerLog.relative(d);
                if (world.isEmptyBlock(rootPos)) world.setBlock(rootPos, logState, 18);
            }
        }

        // Слой 3, 4, 5: Чистый одиночный ствол
        for (int h = 2; h <= 4; h++) {
            BlockPos trunkPos = pos.above(h);
            if (world.isEmptyBlock(trunkPos)) world.setBlock(trunkPos, logState, 18);
        }

        // Слой 6: Разветвление
        BlockPos layer6Center = pos.above(5);
        BlockPos branch1_L6 = layer6Center.relative(mainDir);
        BlockPos branch2_L6 = layer6Center.relative(oppDir);
        if (world.isEmptyBlock(branch1_L6)) world.setBlock(branch1_L6, logState, 18);
        if (world.isEmptyBlock(branch2_L6)) world.setBlock(branch2_L6, logState, 18);

        // Слой 7: Дальний разнос веток
        BlockPos layer7Center = pos.above(6);
        BlockPos branch1_L7 = layer7Center.relative(mainDir, 2);
        BlockPos branch2_L7 = layer7Center.relative(oppDir, 2);
        if (world.isEmptyBlock(branch1_L7)) world.setBlock(branch1_L7, logState, 18);
        if (world.isEmptyBlock(branch2_L7)) world.setBlock(branch2_L7, logState, 18);

        // Маленькая шапка
        BlockPos smallCapCenter = branch1_L7.above();
        if (world.isEmptyBlock(smallCapCenter)) world.setBlock(smallCapCenter, logState, 18);
        buildLeavesCap(world, smallCapCenter, leavesState, false, 18);

        // Большая шапка
        BlockPos bigCapCenter = branch2_L7.above(2);
        BlockPos bigCapSupport = branch2_L7.above();
        if (world.isEmptyBlock(bigCapSupport)) world.setBlock(bigCapSupport, logState, 18);
        if (world.isEmptyBlock(bigCapCenter)) world.setBlock(bigCapCenter, logState, 18);
        buildLeavesCap(world, bigCapCenter, leavesState, true, 18);
    }

    private static void buildLeavesCap(ISeedReader world, BlockPos logPos, BlockState leaves, boolean isBig, int flag) {
        if (isBig) {
            BlockPos layer1 = logPos.below();
            for (int lx = -2; lx <= 2; lx++) {
                for (int lz = -2; lz <= 2; lz++) {
                    int absX = Math.abs(lx);
                    int absZ = Math.abs(lz);
                    if (absX == 2 && absZ == 2) continue;
                    if ((absX == 2 && absZ == 1) || (absX == 1 && absZ == 2)) continue;

                    BlockPos p = layer1.offset(lx, 0, lz);
                    if (world.getBlockState(p).getBlock() == ModBlocks.END_WOOD_LOG.get()) continue;
                    if (world.isEmptyBlock(p)) world.setBlock(p, leaves, flag);
                }
            }
        }

        for (int lx = -2; lx <= 2; lx++) {
            for (int lz = -2; lz <= 2; lz++) {
                if (lx == 0 && lz == 0) continue;
                if (Math.abs(lx) == 2 && Math.abs(lz) == 2) continue;

                BlockPos p = logPos.offset(lx, 0, lz);
                if (world.isEmptyBlock(p)) world.setBlock(p, leaves, flag);
            }
        }

        BlockPos layer3 = logPos.above();
        for (int lx = -1; lx <= 1; lx++) {
            for (int lz = -1; lz <= 1; lz++) {
                if (Math.abs(lx) == 1 && Math.abs(lz) == 1) continue;

                BlockPos p = layer3.offset(lx, 0, lz);
                if (world.isEmptyBlock(p)) world.setBlock(p, leaves, flag);
            }
        }
    }

    private static boolean isReplaceable(ISeedReader world, BlockPos pos) {
        return world.isEmptyBlock(pos)
                || world.getBlockState(pos).getBlock() == ModBlocks.XEN_BUSH_GRASS.get()
                || world.getBlockState(pos).getBlock() == ModBlocks.XEN_GRASS_BLOCK.get();
    }
}
