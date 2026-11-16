package ru.rlyqaz.divinesouls.entity.custom

import net.minecraft.core.BlockPos
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.RandomSource
import net.minecraft.world.Difficulty
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.SpawnGroupData
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import ru.rlyqaz.divinesouls.sound.ModSounds

class SandyCrabEntity(
    entityType: EntityType<out Monster>,
    level: Level
) : Monster(entityType, level) {

    companion object {
        private val DATA_BURROWED: EntityDataAccessor<Boolean> =
            SynchedEntityData.defineId(SandyCrabEntity::class.java, EntityDataSerializers.BOOLEAN)

        fun isSandOrGround(state: BlockState): Boolean {
            val block = state.block
            return block == Blocks.SAND ||
                    block == Blocks.RED_SAND ||
                    block == Blocks.SANDSTONE ||
                    block == Blocks.RED_SANDSTONE ||
                    block == Blocks.DIRT ||
                    block == Blocks.GRASS_BLOCK ||
                    block == Blocks.COARSE_DIRT ||
                    block == Blocks.TERRACOTTA ||
                    block == Blocks.RED_TERRACOTTA ||
                    block == Blocks.ORANGE_TERRACOTTA ||
                    block == Blocks.YELLOW_TERRACOTTA ||
                    block == Blocks.WHITE_TERRACOTTA ||
                    block == Blocks.LIGHT_GRAY_TERRACOTTA ||
                    block == Blocks.BROWN_TERRACOTTA
        }

        fun createAttributes(): AttributeSupplier.Builder {
            return createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 3.5)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.25)
                .add(Attributes.FOLLOW_RANGE, 14.0)
        }

        fun checkSandyCrabSpawnRules(
            type: EntityType<SandyCrabEntity>,
            level: ServerLevelAccessor,
            spawnType: MobSpawnType,
            pos: BlockPos,
            random: RandomSource
        ): Boolean {
            if (level.difficulty == Difficulty.PEACEFUL) return false
            if (!level.canSeeSky(pos)) return false

            val below = level.getBlockState(pos.below())
            if (!isSandOrGround(below) || !checkAnyLightMonsterSpawnRules(type, level, spawnType, pos, random)) {
                return false
            }

            // Прореживаем спавн, так как днём в пустыне у краба нет конкурентов
            if (random.nextInt(4) != 0) return false

            // Не спавним нового, если в радиусе 24 блоков уже есть 2 или больше крабов
            val nearbyCrabs = level.getEntitiesOfClass(
                SandyCrabEntity::class.java,
                net.minecraft.world.phys.AABB(pos).inflate(24.0)
            )
            return nearbyCrabs.size < 2
        }
    }

    private var idleBurrowTimer = 0
    private var lockedYaw = 0.0f

    var isBurrowed: Boolean
        get() = entityData.get(DATA_BURROWED)
        set(value) {
            if (value && !isBurrowed) {
                lockedYaw = yRot
            }
            entityData.set(DATA_BURROWED, value)
        }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(DATA_BURROWED, false)
    }

    override fun registerGoals() {
        goalSelector.addGoal(0, FloatGoal(this))
        goalSelector.addGoal(1, MeleeAttackGoal(this, 1.1, false))
        goalSelector.addGoal(2, object : WaterAvoidingRandomStrollGoal(this, 0.8) {
            override fun canUse(): Boolean = !isBurrowed && super.canUse()
            override fun canContinueToUse(): Boolean = !isBurrowed && super.canContinueToUse()
        })
        goalSelector.addGoal(3, object : LookAtPlayerGoal(this, Player::class.java, 8.0f) {
            override fun canUse(): Boolean = !isBurrowed && super.canUse()
            override fun canContinueToUse(): Boolean = !isBurrowed && super.canContinueToUse()
        })
        goalSelector.addGoal(4, object : RandomLookAroundGoal(this) {
            override fun canUse(): Boolean = !isBurrowed && super.canUse()
            override fun canContinueToUse(): Boolean = !isBurrowed && super.canContinueToUse()
        })

        targetSelector.addGoal(1, HurtByTargetGoal(this))
        targetSelector.addGoal(2, object : NearestAttackableTargetGoal<Player>(this, Player::class.java, true) {
            override fun canUse(): Boolean = !isBurrowed && super.canUse()
        })
    }
    override fun aiStep() {
        super.aiStep()

        if (isBurrowed) {
            yRot = lockedYaw
            yBodyRot = lockedYaw
            yHeadRot = lockedYaw
            yRotO = lockedYaw
            yBodyRotO = lockedYaw
            yHeadRotO = lockedYaw
        }

        if (level().isClientSide) return

        var nearestPlayer: Player? = null
        for (player in level().players()) {
            if (!player.isCreative && !player.isSpectator && player.distanceToSqr(this) <= 20.25) {
                nearestPlayer = player
                break
            }
        }

        if (isBurrowed) {
            navigation.stop()
            deltaMovement = deltaMovement.multiply(0.0, 1.0, 0.0)

            if (nearestPlayer != null || target != null) {
                unburrowAndAmbush(nearestPlayer)
            }
        } else {
            if (target == null && nearestPlayer == null && onGround() && canBurrowHere()) {
                idleBurrowTimer++
                if (idleBurrowTimer >= 80) {
                    isBurrowed = true
                    idleBurrowTimer = 0
                    spawnDigParticles()
                }
            } else {
                idleBurrowTimer = 0
            }
        }
    }

    private fun canBurrowHere(): Boolean {
        val below = level().getBlockState(blockPosition().below())
        return isSandOrGround(below)
    }

    private fun unburrowAndAmbush(player: Player?) {
        isBurrowed = false
        idleBurrowTimer = 0
        spawnDigParticles()
        playSound(SoundEvents.SAND_BREAK, 1.0f, 0.85f)

        if (player != null) {
            target = player
            val dx = player.x - x
            val dz = player.z - z
            val dist = kotlin.math.sqrt(dx * dx + dz * dz).coerceAtLeast(0.01)
            setDeltaMovement((dx / dist) * 0.30, 0.25, (dz / dist) * 0.30)
        }
    }

    private fun spawnDigParticles() {
        val serverLevel = level() as? ServerLevel ?: return
        val state = serverLevel.getBlockState(blockPosition().below())
        if (!state.isAir) {
            serverLevel.sendParticles(
                BlockParticleOption(ParticleTypes.BLOCK, state),
                x, y + 0.1, z,
                14, 0.3, 0.1, 0.3, 0.05
            )
        }
    }

    override fun hurt(source: DamageSource, amount: Float): Boolean {
        if (isBurrowed && !level().isClientSide) {
            isBurrowed = false
            spawnDigParticles()
        }
        return super.hurt(source, amount)
    }

    override fun getAmbientSound(): SoundEvent? {
        return if (isBurrowed) null else ModSounds.SANDY_CRAB_AMBIENT.get()
    }

    override fun getHurtSound(damageSource: DamageSource): SoundEvent = ModSounds.SANDY_CRAB_HURT.get()

    override fun getDeathSound(): SoundEvent = ModSounds.SANDY_CRAB_DEATH.get()

    // Разрешаем спавн и перемещение при любом дневном освещении (иначе Monster отбраковывает спавн днём!)
    override fun getWalkTargetValue(pos: BlockPos, level: net.minecraft.world.level.LevelReader): Float {
        return 0.0f
    }

    @Suppress("DEPRECATION")
    override fun finalizeSpawn(
        level: ServerLevelAccessor,
        difficulty: DifficultyInstance,
        spawnType: MobSpawnType,
        spawnGroupData: SpawnGroupData?
    ): SpawnGroupData? {
        val data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData)

        var playerNearby = false
        for (player in level.players()) {
            if (!player.isCreative && !player.isSpectator && player.distanceToSqr(this) <= 20.25) {
                playerNearby = true
                break
            }
        }

        if (!playerNearby && canBurrowHere()) {
            lockedYaw = yRot
            isBurrowed = true
        }

        return data
    }
}
