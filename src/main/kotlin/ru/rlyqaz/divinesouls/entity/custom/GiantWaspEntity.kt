package ru.rlyqaz.divinesouls.entity.custom

import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
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

class GiantWaspEntity(
    entityType: EntityType<out Monster>,
    level: Level
) : Monster(entityType, level) {

    companion object {
        fun createAttributes(): AttributeSupplier.Builder {
            return createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 22.0)
                .add(Attributes.FLYING_SPEED, 0.55)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.ATTACK_DAMAGE, 4.5)
                .add(Attributes.FOLLOW_RANGE, 20.0)
        }

        fun checkGiantWaspSpawnRules(
            type: EntityType<GiantWaspEntity>,
            level: ServerLevelAccessor,
            spawnType: MobSpawnType,
            pos: BlockPos,
            random: RandomSource
        ): Boolean {
            if (level.difficulty == Difficulty.PEACEFUL) return false
            // Спавнится в джунглях днём и ночью (наземно или в кронах)
            if (pos.y < 60) return false
            val nearbyWasps = level.getEntitiesOfClass(
                GiantWaspEntity::class.java,
                net.minecraft.world.phys.AABB(pos).inflate(32.0)
            )
            return nearbyWasps.size < 2 && level.getBlockState(pos).isAir
        }
    }

    init {
        moveControl = FlyingMoveControl(this, 20, true)
        isNoGravity = true
        xpReward = 7
    }

    override fun createNavigation(level: Level): PathNavigation {
        val nav = FlyingPathNavigation(this, level)
        nav.setCanOpenDoors(false)
        nav.setCanFloat(true)
        nav.setCanPassDoors(true)
        return nav
    }

    override fun registerGoals() {
        goalSelector.addGoal(0, FloatGoal(this))
        goalSelector.addGoal(1, MeleeAttackGoal(this, 1.25, true))
        goalSelector.addGoal(2, WaterAvoidingRandomFlyingGoal(this, 1.0))
        goalSelector.addGoal(3, LookAtPlayerGoal(this, Player::class.java, 10.0f))
        goalSelector.addGoal(4, RandomLookAroundGoal(this))

        targetSelector.addGoal(1, HurtByTargetGoal(this))
        targetSelector.addGoal(2, NearestAttackableTargetGoal(this, Player::class.java, true))
    }

    override fun doHurtTarget(target: Entity): Boolean {
        val hurt = super.doHurtTarget(target)
        if (hurt && target is LivingEntity) {
            // Накладывает Отравление I на 3.5 секунды (70 тиков)
            target.addEffect(MobEffectInstance(MobEffects.POISON, 70, 0), this)
        }
        return hurt
    }

    override fun causeFallDamage(fallDistance: Float, multiplier: Float, source: DamageSource): Boolean = false

    override fun checkFallDamage(y: Double, onGround: Boolean, state: BlockState, pos: BlockPos) {
        // Летающий моб не получает урон от падения
    }

    override fun getAmbientSound(): SoundEvent = SoundEvents.BEE_LOOP_AGGRESSIVE

    override fun getHurtSound(damageSource: DamageSource): SoundEvent = SoundEvents.BEE_HURT

    override fun getDeathSound(): SoundEvent = SoundEvents.BEE_DEATH

    override fun getVoicePitch(): Float = 0.85f
}
