package ru.rlyqaz.divinesouls.entity.custom

import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.Difficulty
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobSpawnType
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
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.phys.AABB
import ru.rlyqaz.divinesouls.block.ModBlocks

class AbyssalWalkerEntity(
    entityType: EntityType<AbyssalWalkerEntity>,
    level: Level
) : Monster(entityType, level) {

    private var rootSnareCooldown = 50

    init {
        this.xpReward = 18
    }

    companion object {
        fun createAttributes(): AttributeSupplier.Builder {
            return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 56.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 11.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.55)
                .add(Attributes.STEP_HEIGHT, 1.1)
                .add(Attributes.FOLLOW_RANGE, 26.0)
        }

        fun checkAbyssalWalkerSpawnRules(
            type: EntityType<AbyssalWalkerEntity>,
            level: ServerLevelAccessor,
            spawnType: MobSpawnType,
            pos: BlockPos,
            random: RandomSource
        ): Boolean {
            if (level.difficulty == Difficulty.PEACEFUL) return false
            if (pos.y < 75 || pos.y > 235) return false
            if (!level.getBlockState(pos).isAir ||
                !level.getBlockState(pos.above()).isAir ||
                !level.getBlockState(pos.above(2)).isAir
            ) return false

            val below = level.getBlockState(pos.below())
            val isMycelium = below.`is`(ModBlocks.ABYSSAL_MYCELIUM.get())
            val isAbyssStone = below.`is`(ModBlocks.ABYSSAL_STONE.get()) || below.`is`(ModBlocks.ABYSSAL_TUFF.get())
            if (!isMycelium && !isAbyssStone) return false

            if (!isMycelium && random.nextInt(3) != 0) return false

            val nearby = level.getEntitiesOfClass(
                AbyssalWalkerEntity::class.java,
                AABB(pos).inflate(44.0)
            )
            return nearby.size < 2
        }
    }

    override fun registerGoals() {
        this.goalSelector.addGoal(0, FloatGoal(this))
        this.goalSelector.addGoal(1, MeleeAttackGoal(this, 1.15, true))
        this.goalSelector.addGoal(2, WaterAvoidingRandomStrollGoal(this, 0.85))
        this.goalSelector.addGoal(3, LookAtPlayerGoal(this, Player::class.java, 12.0f))
        this.goalSelector.addGoal(4, RandomLookAroundGoal(this))

        this.targetSelector.addGoal(1, HurtByTargetGoal(this))
        this.targetSelector.addGoal(2, NearestAttackableTargetGoal(this, Player::class.java, true))
    }

    override fun customServerAiStep() {
        super.customServerAiStep()
        val serverLevel = level() as? ServerLevel ?: return

        if (rootSnareCooldown > 0) {
            rootSnareCooldown--
            return
        }

        val currentTarget = target
        if (currentTarget != null && currentTarget.isAlive) {
            val distSq = distanceToSqr(currentTarget)
            if (distSq in 9.0..64.0 && hasLineOfSight(currentTarget)) {
                rootSnareCooldown = 95
                castMyceliumTendrils(serverLevel, currentTarget)
            }
        }
    }

    private fun castMyceliumTendrils(level: ServerLevel, prey: LivingEntity) {
        level.playSound(
            null, x, y, z,
            SoundEvents.NYLIUM_BREAK, SoundSource.HOSTILE,
            1.2f, 0.65f
        )

        // Линия мицелиевых корней по земле от скитальца к цели
        val steps = 10
        for (i in 1..steps) {
            val t = i.toDouble() / steps.toDouble()
            val px = x + (prey.x - x) * t
            val py = y + (prey.y - y) * t + 0.15
            val pz = z + (prey.z - z) * t
            level.sendParticles(ParticleTypes.MYCELIUM, px, py, pz, 6, 0.25, 0.1, 0.25, 0.02)
            level.sendParticles(ParticleTypes.WITCH, px, py + 0.2, pz, 3, 0.15, 0.1, 0.15, 0.01)
        }

        prey.addEffect(MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), this)
        // Лёгкий рывок корнями в сторону скитальца
        val pullX = (x - prey.x) * 0.12
        val pullZ = (z - prey.z) * 0.12
        prey.push(pullX, 0.15, pullZ)
        prey.hurtMarked = true
    }

    override fun aiStep() {
        super.aiStep()
        if (level().isClientSide && random.nextInt(4) == 0) {
            level().addParticle(
                ParticleTypes.MYCELIUM,
                x + (random.nextDouble() - 0.5) * 0.7,
                y + 0.2 + random.nextDouble() * 1.6,
                z + (random.nextDouble() - 0.5) * 0.7,
                0.0, 0.01, 0.0
            )
        }
    }

    override fun getWalkTargetValue(pos: BlockPos, level: LevelReader): Float = 1.0f

    override fun getAmbientSound(): SoundEvent = SoundEvents.NYLIUM_STEP

    override fun getHurtSound(damageSource: DamageSource): SoundEvent = SoundEvents.SHROOMLIGHT_HIT

    override fun getDeathSound(): SoundEvent = SoundEvents.NYLIUM_BREAK

    override fun getVoicePitch(): Float = 0.55f
}
