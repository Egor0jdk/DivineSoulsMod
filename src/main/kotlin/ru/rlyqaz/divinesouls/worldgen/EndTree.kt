package ru.rlyqaz.divinesouls.worldgen

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockState
import ru.rlyqaz.divinesouls.block.ModBlocks
import kotlin.math.abs

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.21.1 NeoForge)
 * КЛАСС: EndTree (Процедурный генератор Дерева Края)
 * ЛОГИКА: Самописная поблочная генерация структуры дерева
 * ====================================================================
 */
object EndTree {

    // Основной триггер выращивания, который мы будем вручную вызывать из саженца
    fun growTree(
        level: ServerLevel,
        pos: BlockPos,
        random: RandomSource
    ): Boolean {
        level.removeBlock(pos, false)

        val logState = ModBlocks.END_WOOD_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y)
        val leavesState = ModBlocks.END_WOOD_LEAVES.get().defaultBlockState()

        val mainDir = if (random.nextBoolean()) Direction.NORTH else Direction.EAST
        val oppDir = mainDir.opposite

        // Слой 1 и 2: Плюсики из 5 блоков
        for (h in 0..1) {
            val centerPos = pos.above(h)
            if (isReplaceable(level, centerPos)) level.setBlock(centerPos, logState, 2)

            val dirs = arrayOf(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
            for (d in dirs) {
                val rootPos = centerPos.relative(d)
                if (isReplaceable(level, rootPos)) level.setBlock(rootPos, logState, 2)
            }
        }

        // Слой 3, 4, 5: Чистый одиночный ствол
        for (h in 2..4) {
            val trunkPos = pos.above(h)
            if (isReplaceable(level, trunkPos)) level.setBlock(trunkPos, logState, 2)
        }

        // Слой 6: Начало разделения веток
        val layer6Center = pos.above(5)
        val branch1L6 = layer6Center.relative(mainDir)
        val branch2L6 = layer6Center.relative(oppDir)
        if (isReplaceable(level, branch1L6)) level.setBlock(branch1L6, logState, 2)
        if (isReplaceable(level, branch2L6)) level.setBlock(branch2L6, logState, 2)

        // Слой 7: Дальний разнос веток
        val layer7Center = pos.above(6)
        val branch1L7 = layer7Center.relative(mainDir, 2)
        val branch2L7 = layer7Center.relative(oppDir, 2)
        if (isReplaceable(level, branch1L7)) level.setBlock(branch1L7, logState, 2)
        if (isReplaceable(level, branch2L7)) level.setBlock(branch2L7, logState, 2)

        // Маленькая шапка листвы над левой веткой
        val smallCapCenter = branch1L7.above()
        if (isReplaceable(level, smallCapCenter)) level.setBlock(smallCapCenter, logState, 2)
        buildLeavesCap(level, smallCapCenter, leavesState, false, 2)

        // Большая шапка листвы над правой веткой
        val bigCapCenter = branch2L7.above(2)
        val bigCapSupport = branch2L7.above()
        if (isReplaceable(level, bigCapSupport)) level.setBlock(bigCapSupport, logState, 2)
        if (isReplaceable(level, bigCapCenter)) level.setBlock(bigCapCenter, logState, 2)
        buildLeavesCap(level, bigCapCenter, leavesState, true, 2)

        return true
    }

    // Метод безопасной генерации дерева в мире (например, для будущих биомов)
    fun generateTreeSafely(level: LevelAccessor, pos: BlockPos, random: RandomSource) {
        val logState = ModBlocks.END_WOOD_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y)
        val leavesState = ModBlocks.END_WOOD_LEAVES.get().defaultBlockState()

        val mainDir = if (random.nextBoolean()) Direction.NORTH else Direction.EAST
        val oppDir = mainDir.opposite

        for (h in 0..1) {
            val centerLog = pos.above(h)
            if (level.isEmptyBlock(centerLog)) level.setBlock(centerLog, logState, 18)

            val dirs = arrayOf(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
            for (d in dirs) {
                val rootPos = centerLog.relative(d)
                if (level.isEmptyBlock(rootPos)) level.setBlock(rootPos, logState, 18)
            }
        }

        for (h in 2..4) {
            val trunkPos = pos.above(h)
            if (level.isEmptyBlock(trunkPos)) level.setBlock(trunkPos, logState, 18)
        }

        val layer6Center = pos.above(5)
        val branch1L6 = layer6Center.relative(mainDir)
        val branch2L6 = layer6Center.relative(oppDir)
        if (level.isEmptyBlock(branch1L6)) level.setBlock(branch1L6, logState, 18)
        if (level.isEmptyBlock(branch2L6)) level.setBlock(branch2L6, logState, 18)

        val layer7Center = pos.above(6)
        val branch1L7 = layer7Center.relative(mainDir, 2)
        val branch2L7 = layer7Center.relative(oppDir, 2)
        if (level.isEmptyBlock(branch1L7)) level.setBlock(branch1L7, logState, 18)
        if (level.isEmptyBlock(branch2L7)) level.setBlock(branch2L7, logState, 18)

        val smallCapCenter = branch1L7.above()
        if (level.isEmptyBlock(smallCapCenter)) level.setBlock(smallCapCenter, logState, 18)
        buildLeavesCap(level, smallCapCenter, leavesState, false, 18)

        val bigCapCenter = branch2L7.above(2)
        val bigCapSupport = branch2L7.above()
        if (level.isEmptyBlock(bigCapSupport)) level.setBlock(bigCapSupport, logState, 18)
        if (level.isEmptyBlock(bigCapCenter)) level.setBlock(bigCapCenter, logState, 18)
        buildLeavesCap(level, bigCapCenter, leavesState, true, 18)
    }

    private fun buildLeavesCap(level: LevelAccessor, logPos: BlockPos, leaves: BlockState, isBig: Boolean, flag: Int) {
        if (isBig) {
            val layer1 = logPos.below()
            for (lx in -2..2) {
                for (lz in -2..2) {
                    val absX = abs(lx)
                    val absZ = abs(lz)
                    if (absX == 2 && absZ == 2) continue
                    if ((absX == 2 && absZ == 1) || (absX == 1 && absZ == 2)) continue

                    val p = layer1.offset(lx, 0, lz)
                    if (level.getBlockState(p).`is`(ModBlocks.END_WOOD_LOG.get())) continue
                    if (level.isEmptyBlock(p)) level.setBlock(p, leaves, flag)
                }
            }
        }

        for (lx in -2..2) {
            for (lz in -2..2) {
                if (lx == 0 && lz == 0) continue
                if (abs(lx) == 2 && abs(lz) == 2) continue

                val p = logPos.offset(lx, 0, lz)
                if (level.isEmptyBlock(p)) level.setBlock(p, leaves, flag)
            }
        }

        val layer3 = logPos.above()
        for (lx in -1..1) {
            for (lz in -1..1) {
                if (abs(lx) == 1 && abs(lz) == 1) continue

                val p = layer3.offset(lx, 0, lz)
                if (level.isEmptyBlock(p)) level.setBlock(p, leaves, flag)
            }
        }
    }

    private fun isReplaceable(level: LevelAccessor, pos: BlockPos): Boolean {
        return level.isEmptyBlock(pos) ||
                level.getBlockState(pos).`is`(ModBlocks.END_BUSH_GRASS.get()) ||
                level.getBlockState(pos).`is`(ModBlocks.END_GRASS_BLOCK.get())
    }
}
