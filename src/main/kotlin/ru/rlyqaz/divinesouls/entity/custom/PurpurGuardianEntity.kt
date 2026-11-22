package ru.rlyqaz.divinesouls.entity.custom

import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.phys.AABB

class PurpurGuardianEntity(
    entityType: EntityType<out Monster>,
    level: Level
) : Monster(entityType, level) {

    init {
        xpReward = 12
    }

    companion object {
        fun createAttributes(): AttributeSupplier.Builder {
            return createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 38.0)
                .add(Attributes.MOVEMENT_SPEED, 0.31)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.ARMOR, 5.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.FOLLOW_RANGE, 36.0)
        }
    }

    override fun registerGoals() {
        goalSelector.addGoal(0, FloatGoal(this))
        goalSelector.addGoal(1, MeleeAttackGoal(this, 1.2, true))
        goalSelector.addGoal(2, WaterAvoidingRandomStrollGoal(this, 0.9))
        goalSelector.addGoal(3, LookAtPlayerGoal(this, Player::class.java, 12.0f))
        goalSelector.addGoal(4, RandomLookAroundGoal(this))

        targetSelector.addGoal(1, HurtByTargetGoal(this))
        targetSelector.addGoal(2, NearestAttackableTargetGoal(this, Player::class.java, true))
    }

    override fun customServerAiStep() {
        super.customServerAiStep()
        val serverLevel = level() as? ServerLevel ?: return

        // Если страж заспавнился на главном острове Энда, но Дракон уже повержен — он рассыпается в пустоту
        if (tickCount % 40 == 0 && serverLevel.dimension() == Level.END && position().horizontalDistanceSqr() < 160.0 * 160.0) {
            val activeDragons = serverLevel.getEntitiesOfClass(
                EnderDragon::class.java,
                AABB(-160.0, 0.0, -160.0, 160.0, 256.0, 160.0)
            )
            if (activeDragons.isEmpty()) {
                serverLevel.sendParticles(ParticleTypes.PORTAL, x, y + 1.0, z, 20, 0.4, 0.7, 0.4, 0.08)
                discard()
            }
        }
    }

    override fun doHurtTarget(entity: Entity): Boolean {
        if (super.doHurtTarget(entity)) {
            entity.push(0.0, 0.35, 0.0)
            return true
        }
        return false
    }

    override fun getWalkTargetValue(pos: BlockPos, level: LevelReader): Float = 1.0f

    override fun getAmbientSound(): SoundEvent = SoundEvents.SHULKER_AMBIENT

    override fun getHurtSound(damageSource: DamageSource): SoundEvent = SoundEvents.SHULKER_HURT

    override fun getDeathSound(): SoundEvent = SoundEvents.SHULKER_DEATH

    override fun getVoicePitch(): Float = 0.75f
}
