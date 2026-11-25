package ru.rlyqaz.divinesouls.block.custom

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape
import ru.rlyqaz.divinesouls.DivineSoulsMod

class AbyssPortalBlock(properties: Properties) : Block(properties) {

    companion object {
        val AXIS: EnumProperty<Direction.Axis> = BlockStateProperties.HORIZONTAL_AXIS
        private val X_AXIS_AABB: VoxelShape = box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0)
        private val Z_AXIS_AABB: VoxelShape = box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0)

        val THE_ABYSS_KEY: ResourceKey<Level> = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "the_abyss")
        )
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.X))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(AXIS)
    }

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return if (state.getValue(AXIS) == Direction.Axis.Z) Z_AXIS_AABB else X_AXIS_AABB
    }

    override fun updateShape(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        level: LevelAccessor,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        val axis = state.getValue(AXIS)
        val isFrameAxis = (axis == Direction.Axis.X && direction.axis == Direction.Axis.X) ||
                (axis == Direction.Axis.Z && direction.axis == Direction.Axis.Z) ||
                direction.axis == Direction.Axis.Y

        if (isFrameAxis) {
            val validNeighbor = neighborState.`is`(this) || neighborState.`is`(Blocks.REINFORCED_DEEPSLATE)
            if (!validNeighbor) {
                return Blocks.AIR.defaultBlockState()
            }
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos)
    }

    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity) {
        if (level.isClientSide || entity.isPassenger || entity.isVehicle || !entity.canUsePortal(false)) return
        val serverLevel = level as? ServerLevel ?: return

        // Телепортация в Бездну и обратно (работает через Entity.setAsInsidePortal с ванильной задержкой в 4 сек, как у портала Незера!)
        if (entity.isOnPortalCooldown) {
            entity.setPortalCooldown()
            return
        }

        val targetDim = if (serverLevel.dimension() == THE_ABYSS_KEY) Level.OVERWORLD else THE_ABYSS_KEY
        val destLevel = serverLevel.server.getLevel(targetDim) ?: return

        entity.setPortalCooldown()
        ru.rlyqaz.divinesouls.worldgen.AbyssPortalManager.teleportEntityThroughPortal(entity, serverLevel, destLevel, pos)
    }

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        if (random.nextInt(40) == 0) {
            level.playLocalSound(
                pos.x + 0.5, pos.y + 0.5, pos.z + 0.5,
                SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS,
                0.4f, 0.65f, false
            )
        }

        for (i in 0 until 3) {
            val px = pos.x + random.nextDouble()
            val py = pos.y + random.nextDouble()
            val pz = pos.z + random.nextDouble()
            val vx = (random.nextDouble() - 0.5) * 0.25
            val vy = (random.nextDouble() - 0.5) * 0.25
            val vz = (random.nextDouble() - 0.5) * 0.25

            level.addParticle(
                if (random.nextBoolean()) ParticleTypes.SCULK_SOUL else ParticleTypes.PORTAL,
                px, py, pz, vx, vy, vz
            )
        }
    }
}
