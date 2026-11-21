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

class SculkGrassBlock(properties: Properties) : Block(properties), net.minecraft.world.level.block.BonemealableBlock {

    companion object {
        private fun canBeGrass(
            state: net.minecraft.world.level.block.state.BlockState,
            level: net.minecraft.world.level.LevelReader,
            pos: net.minecraft.core.BlockPos
        ): Boolean {
            val abovePos = pos.above()
            val aboveState = level.getBlockState(abovePos)
            if (aboveState.`is`(net.minecraft.world.level.block.Blocks.SNOW) &&
                aboveState.getValue(net.minecraft.world.level.block.SnowLayerBlock.LAYERS) == 1
            ) {
                return true
            }
            if (aboveState.fluidState.amount == 8) {
                return false
            }
            val lightBlocked = net.minecraft.world.level.lighting.LightEngine.getLightBlockInto(
                level, state, pos, aboveState, abovePos,
                net.minecraft.core.Direction.UP, aboveState.getLightBlock(level, abovePos)
            )
            return lightBlocked < level.maxLightLevel
        }

        private fun canPropagate(
            state: net.minecraft.world.level.block.state.BlockState,
            level: net.minecraft.world.level.LevelReader,
            pos: net.minecraft.core.BlockPos
        ): Boolean {
            return canBeGrass(state, level, pos) &&
                    !level.getFluidState(pos.above()).`is`(net.minecraft.tags.FluidTags.WATER)
        }
    }

    override fun isRandomlyTicking(state: net.minecraft.world.level.block.state.BlockState): Boolean = true

    override fun randomTick(
        state: net.minecraft.world.level.block.state.BlockState,
        level: net.minecraft.server.level.ServerLevel,
        pos: net.minecraft.core.BlockPos,
        random: net.minecraft.util.RandomSource
    ) {
        if (!canBeGrass(state, level, pos)) {
            if (!level.isAreaLoaded(pos, 1)) return
            level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.SCULK.defaultBlockState())
            return
        }

        if (!level.isAreaLoaded(pos, 3)) return

        if (level.getMaxLocalRawBrightness(pos.above()) >= 9) {
            val defaultGrass = this.defaultBlockState()
            repeat(4) {
                val targetPos = pos.offset(
                    random.nextInt(3) - 1,
                    random.nextInt(5) - 3,
                    random.nextInt(3) - 1
                )
                if (level.getBlockState(targetPos).`is`(net.minecraft.world.level.block.Blocks.SCULK) &&
                    canPropagate(defaultGrass, level, targetPos)
                ) {
                    level.setBlockAndUpdate(targetPos, defaultGrass)
                }
            }
        }
    }

    override fun isValidBonemealTarget(
        level: net.minecraft.world.level.LevelReader,
        pos: net.minecraft.core.BlockPos,
        state: net.minecraft.world.level.block.state.BlockState
    ): Boolean = level.getBlockState(pos.above()).isAir

    override fun isBonemealSuccess(
        level: net.minecraft.world.level.Level,
        random: net.minecraft.util.RandomSource,
        pos: net.minecraft.core.BlockPos,
        state: net.minecraft.world.level.block.state.BlockState
    ): Boolean = true

    override fun performBonemeal(
        level: net.minecraft.server.level.ServerLevel,
        random: net.minecraft.util.RandomSource,
        pos: net.minecraft.core.BlockPos,
        state: net.minecraft.world.level.block.state.BlockState
    ) {
        val aboveStart = pos.above()
        val sculkGrassState = ru.rlyqaz.divinesouls.block.ModBlocks.SCULK_GRASS.get().defaultBlockState()
        val sculkFlowerState = ru.rlyqaz.divinesouls.block.ModBlocks.SCULK_FLOWER.get().defaultBlockState()

        outer@ for (attempt in 0 until 48) {
            var targetPos = aboveStart
            val steps = attempt / 16
            var step = 0
            while (step < steps) {
                targetPos = targetPos.offset(
                    random.nextInt(3) - 1,
                    (random.nextInt(3) - 1) * random.nextInt(3) / 2,
                    random.nextInt(3) - 1
                )
                if (!level.getBlockState(targetPos.below()).`is`(this) ||
                    level.getBlockState(targetPos).isCollisionShapeFullBlock(level, targetPos)
                ) {
                    continue@outer
                }
                step++
            }

            if (level.getBlockState(targetPos).isAir && level.getBlockState(targetPos.below()).`is`(this)) {
                val plantToPlace = if (random.nextInt(6) == 0) sculkFlowerState else sculkGrassState
                if (plantToPlace.canSurvive(level, targetPos)) {
                    level.setBlock(targetPos, plantToPlace, 3)
                }
            }
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
