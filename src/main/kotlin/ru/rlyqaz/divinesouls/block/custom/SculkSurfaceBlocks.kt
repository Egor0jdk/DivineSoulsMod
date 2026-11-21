package ru.rlyqaz.divinesouls.block.custom

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.valueproviders.ConstantInt
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.BushBlock
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape
import ru.rlyqaz.divinesouls.block.ModBlocks

class SculkGrassBlock(properties: Properties) : Block(properties) {
    override fun spawnAfterBreak(
        state: BlockState,
        level: ServerLevel,
        pos: BlockPos,
        stack: ItemStack,
        dropExperience: Boolean
    ) {
        super.spawnAfterBreak(state, level, pos, stack, dropExperience)
        if (dropExperience) {
            tryDropExperience(level, pos, stack, ConstantInt.of(1))
        }
    }
}

class SculkLogBlock(properties: Properties) : RotatedPillarBlock(properties)

class SculkLeavesBlock(properties: Properties) : LeavesBlock(properties)

class SculkTallGrassBlock(properties: Properties) : BushBlock(properties) {

    companion object {
        val CODEC: MapCodec<SculkTallGrassBlock> = simpleCodec(::SculkTallGrassBlock)
        private val SHAPE: VoxelShape = box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0)
    }

    override fun codec(): MapCodec<out BushBlock> = CODEC

    override fun getShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape = SHAPE

    override fun mayPlaceOn(state: BlockState, level: BlockGetter, pos: BlockPos): Boolean {
        return state.`is`(ModBlocks.SCULK_GRASS_BLOCK.get()) || state.`is`(Blocks.SCULK)
    }
}

class SculkFlowerBlock(properties: Properties) : BushBlock(properties) {

    companion object {
        val CODEC: MapCodec<SculkFlowerBlock> = simpleCodec(::SculkFlowerBlock)
        private val SHAPE: VoxelShape = box(5.0, 0.0, 5.0, 11.0, 10.0, 11.0)
    }

    override fun codec(): MapCodec<out BushBlock> = CODEC

    override fun getShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape = SHAPE

    override fun mayPlaceOn(state: BlockState, level: BlockGetter, pos: BlockPos): Boolean {
        return state.`is`(ModBlocks.SCULK_GRASS_BLOCK.get()) || state.`is`(Blocks.SCULK)
    }

    override fun canSurvive(state: BlockState, level: LevelReader, pos: BlockPos): Boolean {
        val below = pos.below()
        return mayPlaceOn(level.getBlockState(below), level, below)
    }
}
