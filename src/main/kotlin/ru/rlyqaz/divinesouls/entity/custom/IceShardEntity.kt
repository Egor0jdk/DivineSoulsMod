package ru.rlyqaz.divinesouls.entity.custom

import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.ThrowableItemProjectile
import net.minecraft.world.item.Item
import net.minecraft.world.level.Level
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import ru.rlyqaz.divinesouls.entity.ModEntities
import ru.rlyqaz.divinesouls.item.ModItems

class IceShardEntity : ThrowableItemProjectile {

    var baseDamage: Float = 5.0f
    var customGravity: Double = 0.015
    var maxLifeTicks: Int = 100

    constructor(entityType: EntityType<out IceShardEntity>, level: Level) : super(entityType, level)

    constructor(
        level: Level,
        shooter: LivingEntity,
        damage: Float = 5.0f,
        gravity: Double = 0.015,
        lifeTicks: Int = 100
    ) : super(
        ModEntities.ICE_SHARD.get(),
        shooter,
        level
    ) {
        this.baseDamage = damage
        this.customGravity = gravity
        this.maxLifeTicks = lifeTicks
        if (gravity == 0.0) {
            this.isNoGravity = true
        }
    }

    override fun getDefaultItem(): Item = ModItems.ICE_SHARD_ITEM.get()

    override fun getDefaultGravity(): Double = customGravity

    override fun tick() {
        super.tick()

        if (tickCount.compareTo(maxLifeTicks) != -1) {
            if (!level().isClientSide) {
                level().broadcastEntityEvent(this, 3)
                discard()
            }
            return
        }
    }

    override fun onHitEntity(result: EntityHitResult) {
        super.onHitEntity(result)
        val target = result.entity
        val owner = this.owner
        if (target == owner) return

        // Сбрасываем неуязвимость от предыдущего шипа в очереди, чтобы все 3 шипа нанесли урон!
        target.invulnerableTime = 0

        if (target.hurt(damageSources().indirectMagic(this, owner), baseDamage)) {
            if (target is LivingEntity) {
                // Замедление I (amplifier = 0) на 1.5 секунды (30 тиков) и лёгкое обморожение
                target.addEffect(MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 0), owner)
                target.ticksFrozen = (target.ticksFrozen + 35).coerceAtMost(target.ticksRequiredToFreeze + 20)
            }
        }
    }

    override fun onHit(result: HitResult) {
        super.onHit(result)
        if (!level().isClientSide) {
            playSound(SoundEvents.GLASS_BREAK, 0.6f, 1.6f)
            level().broadcastEntityEvent(this, 3)
            discard()
        }
    }

    override fun handleEntityEvent(id: Byte) {
        if (id.toInt() == 3) {
            for (i in 0 until 8) {
                level().addParticle(
                    ParticleTypes.SNOWFLAKE,
                    x, y, z,
                    (random.nextDouble() - 0.5) * 0.25,
                    (random.nextDouble() - 0.5) * 0.25,
                    (random.nextDouble() - 0.5) * 0.25
                )
            }
        }
    }
}
