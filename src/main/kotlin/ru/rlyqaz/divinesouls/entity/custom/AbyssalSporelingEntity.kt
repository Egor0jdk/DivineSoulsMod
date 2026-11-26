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

class AbyssalSporelingEntity(
    entityType: EntityType<AbyssalSporelingEntity>,
    level: Level
) : Monster(entityType, level) {

    private var sporeCloudCooldown = 40

    init {
        this.xpReward = 14
    }

    companion object {
        fun createAttributes(): AttributeSupplier.Builder {
            return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 38.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.35)
                .add(Attributes.FOLLOW_RANGE, 22.0)
        }

        fun checkAbyssalSporelingSpawnRules(
            type: EntityType<AbyssalSporelingEntity>,
            level: ServerLevelAccessor,
            spawnType: MobSpawnType,
            pos: BlockPos,
            random: RandomSource
        ): Boolean {
            if (level.difficulty == Difficulty.PEACEFUL) return false
            // Спавнится на средних и верхних террасах Бездны (Y = 75..235)
            if (pos.y < 75 || pos.y > 235) return false
            if (!level.getBlockState(pos).isAir || !level.getBlockState(pos.above()).isAir) return false

            val below = level.getBlockState(pos.below())
            val isMycelium = below.`is`(ModBlocks.ABYSSAL_MYCELIUM.get())
            val isAbyssStone = below.`is`(ModBlocks.ABYSSAL_STONE.get()) || below.`is`(ModBlocks.ABYSSAL_TUFF.get())
            if (!isMycelium && !isAbyssStone) return false

            // На обычном камне спавнится реже, на Мицелии Бездны — всегда с радостью!
            if (!isMycelium && random.nextInt(3) != 0) return false

            val nearby = level.getEntitiesOfClass(
                AbyssalSporelingEntity::class.java,
                AABB(pos).inflate(36.0)
            )
            return nearby.size < 3
        }
    }

    override fun registerGoals() {
        this.goalSelector.addGoal(0, FloatGoal(this))
        this.goalSelector.addGoal(1, MeleeAttackGoal(this, 1.15, true))
        this.goalSelector.addGoal(2, WaterAvoidingRandomStrollGoal(this, 0.85))
        this.goalSelector.addGoal(3, LookAtPlayerGoal(this, Player::class.java, 10.0f))
        this.goalSelector.addGoal(4, RandomLookAroundGoal(this))

        this.targetSelector.addGoal(1, HurtByTargetGoal(this))
        this.targetSelector.addGoal(2, NearestAttackableTargetGoal(this, Player::class.java, true))
    }

    override fun customServerAiStep() {
        super.customServerAiStep()
        val serverLevel = level() as? ServerLevel ?: return

        if (sporeCloudCooldown > 0) {
            sporeCloudCooldown--
            return
        }

        val currentTarget = target
        if (currentTarget != null && currentTarget.isAlive && distanceToSqr(currentTarget) <= 14.0) {
            sporeCloudCooldown = 90
            releaseSporeCloud(serverLevel)
        }
    }

    private fun releaseSporeCloud(level: ServerLevel) {
        level.playSound(
            null, x, y, z,
            SoundEvents.FUNGUS_BREAK, SoundSource.HOSTILE,
            1.2f, 0.7f
        )
        level.sendParticles(
            ParticleTypes.WARPED_SPORE,
            x, y + 0.7, z,
            40, 1.6, 0.6, 1.6, 0.06
        )
        level.sendParticles(
            ParticleTypes.WITCH,
            x, y + 0.7, z,
            24, 1.5, 0.5, 1.5, 0.04
        )

        val victims = level.getEntitiesOfClass(LivingEntity::class.java, boundingBox.inflate(3.5, 1.5, 3.5)) { e ->
            e != this && e !is AbyssalSporelingEntity && e.isAlive
        }
        for (victim in victims) {
            victim.hurt(level.damageSources().indirectMagic(this, this), 4.0f)
            victim.addEffect(MobEffectInstance(MobEffects.DARKNESS, 60, 0), this)
            victim.addEffect(MobEffectInstance(MobEffects.CONFUSION, 70, 0), this)
        }
    }

    override fun aiStep() {
        super.aiStep()
        if (level().isClientSide && random.nextInt(4) == 0) {
            level().addParticle(
                ParticleTypes.WARPED_SPORE,
                x + (random.nextDouble() - 0.5) * 0.6,
                y + 0.6 + random.nextDouble() * 0.5,
                z + (random.nextDouble() - 0.5) * 0.6,
                0.0, 0.02, 0.0
            )
        }
    }

    override fun getWalkTargetValue(pos: BlockPos, level: LevelReader): Float = 1.0f

    override fun getAmbientSound(): SoundEvent = SoundEvents.SHROOMLIGHT_STEP

    override fun getHurtSound(damageSource: DamageSource): SoundEvent = SoundEvents.FUNGUS_HIT

    override fun getDeathSound(): SoundEvent = SoundEvents.FUNGUS_BREAK

    override fun getVoicePitch(): Float = 0.8f
}
