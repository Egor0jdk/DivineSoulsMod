package ru.rlyqaz.divinesouls.entity.custom

import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundEvent
import net.minecraft.util.RandomSource
import net.minecraft.world.Difficulty
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.control.FlyingMoveControl
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation
import net.minecraft.world.entity.ai.navigation.PathNavigation
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import ru.rlyqaz.divinesouls.sound.ModSounds

class AbyssalShadowEntity(
    entityType: EntityType<AbyssalShadowEntity>,
    level: Level
) : Monster(entityType, level) {

    init {
        this.moveControl = FlyingMoveControl(this, 75, true)
        this.isNoGravity = true
        this.xpReward = 18
    }

    companion object {
        fun createAttributes(): AttributeSupplier.Builder {
            return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 52.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.ATTACK_DAMAGE, 13.0)
                .add(Attributes.FLYING_SPEED, 0.36)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
        }

        fun checkAbyssalShadowSpawnRules(
            type: EntityType<AbyssalShadowEntity>,
            level: ServerLevelAccessor,
            spawnType: MobSpawnType,
            pos: BlockPos,
            random: RandomSource
        ): Boolean {
            if (level.difficulty == Difficulty.PEACEFUL) return false
            // Обитает только в нижних глубоких слоях Бездны (Y = 20..95)
            if (pos.y < 20 || pos.y > 95) return false
            if (!level.getBlockState(pos).isAir || !level.getBlockState(pos.above()).isAir) return false

            // Запрещаем спавн над пустотой: в пределах 4 блоков вниз должна быть твёрдая опора острова
            var hasGroundBelow = false
            for (dy in 1..4) {
                val belowState = level.getBlockState(pos.below(dy))
                if (belowState.isSolidRender(level, pos.below(dy))) {
                    hasGroundBelow = true
                    break
                }
            }
            if (!hasGroundBelow) return false

            val nearby = level.getEntitiesOfClass(
                AbyssalShadowEntity::class.java,
                AABB(pos).inflate(44.0)
            )
            return nearby.size < 2
        }
    }

    override fun registerGoals() {
        this.goalSelector.addGoal(1, FloatGoal(this))
        this.goalSelector.addGoal(2, MeleeAttackGoal(this, 1.15, true))
        this.goalSelector.addGoal(3, WaterAvoidingRandomFlyingGoal(this, 0.95))
        this.goalSelector.addGoal(4, LookAtPlayerGoal(this, Player::class.java, 12.0f))
        this.goalSelector.addGoal(5, RandomLookAroundGoal(this))

        this.targetSelector.addGoal(1, HurtByTargetGoal(this))
        this.targetSelector.addGoal(2, NearestAttackableTargetGoal(this, Player::class.java, true))
    }

    override fun createNavigation(level: Level): PathNavigation {
        val navigation = FlyingPathNavigation(this, level)
        navigation.setCanOpenDoors(false)
        navigation.setCanPassDoors(true)
        return navigation
    }

    override fun aiStep() {
        super.aiStep()
        if (this.level().isClientSide && this.random.nextInt(2) == 0) {
            this.level().addParticle(
                ParticleTypes.SCULK_SOUL,
                this.x + (this.random.nextDouble() - 0.5) * 0.5,
                this.y + 0.4 + this.random.nextDouble() * 1.1,
                this.z + (this.random.nextDouble() - 0.5) * 0.5,
                0.0,
                0.015,
                0.0
            )
        }
    }

    override fun doHurtTarget(entity: Entity): Boolean {
        if (super.doHurtTarget(entity)) {
            if (entity is LivingEntity) {
                entity.addEffect(MobEffectInstance(MobEffects.DARKNESS, 60, 0), this)
            }
            return true
        }
        return false
    }

    override fun travel(travelVector: Vec3) {
        if (this.isControlledByLocalInstance) {
            val baseSpeed = this.getAttributeValue(Attributes.MOVEMENT_SPEED).toFloat()
            this.moveRelative(baseSpeed, travelVector)
            this.move(net.minecraft.world.entity.MoverType.SELF, this.deltaMovement)
            this.deltaMovement = this.deltaMovement.scale(0.76)
        }
        this.calculateEntityAnimation(false)
    }

    override fun causeFallDamage(fallDistance: Float, multiplier: Float, source: DamageSource): Boolean = false

    override fun checkFallDamage(y: Double, onGround: Boolean, state: BlockState, pos: BlockPos) {}

    override fun getAmbientSound(): SoundEvent? {
        return if (this.random.nextBoolean()) {
            net.minecraft.sounds.SoundEvents.PHANTOM_AMBIENT
        } else {
            net.minecraft.sounds.SoundEvents.SOUL_ESCAPE.value()
        }
    }

    override fun getHurtSound(damageSource: DamageSource): SoundEvent =
        net.minecraft.sounds.SoundEvents.PHANTOM_HURT

    override fun getDeathSound(): SoundEvent =
        net.minecraft.sounds.SoundEvents.PHANTOM_DEATH

    override fun getVoicePitch(): Float = 0.45f + (this.random.nextFloat() * 0.1f)

    override fun getSoundVolume(): Float = 0.75f
}
