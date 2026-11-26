package ru.rlyqaz.divinesouls.entity.custom

import net.minecraft.core.particles.DustParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.ThrowableProjectile
import net.minecraft.world.level.Level
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import org.joml.Vector3f
import ru.rlyqaz.divinesouls.entity.ModEntities

class MechaLaserBoltEntity : ThrowableProjectile {

    var boltDamage: Float = 10.0f
    private var lifeTicks = 0

    constructor(entityType: EntityType<out MechaLaserBoltEntity>, level: Level) : super(entityType, level) {
        this.isNoGravity = true
    }

    constructor(level: Level, shooter: LivingEntity, damage: Float) : super(
        ModEntities.MECHA_LASER_BOLT.get(),
        shooter,
        level
    ) {
        this.boltDamage = damage
        this.isNoGravity = true
    }

    override fun defineSynchedData(builder: net.minecraft.network.syncher.SynchedEntityData.Builder) {}

    override fun getDefaultGravity(): Double = 0.0

    override fun tick() {
        val savedVel = deltaMovement
        super.tick()
        if (savedVel.lengthSqr() > 0.0001) {
            deltaMovement = savedVel.normalize().scale(0.72)
        }
        lifeTicks++

        if (level().isClientSide) {
            level().addParticle(
                DustParticleOptions(Vector3f(1.0f, 0.08f, 0.08f), 1.15f),
                x, y, z,
                0.0, 0.0, 0.0
            )
        } else if (lifeTicks >= 55) {
            discard()
        }
    }

    override fun onHitEntity(result: EntityHitResult) {
        super.onHitEntity(result)
        val target = result.entity
        val owner = this.owner
        if (target == owner || target is MechaWitherEntity) return

        target.invulnerableTime = 0
        if (target.hurt(damageSources().indirectMagic(this, owner), boltDamage)) {
            target.igniteForSeconds(2.0f)
        }
    }

    override fun onHit(result: HitResult) {
        super.onHit(result)
        if (!level().isClientSide) {
            playSound(SoundEvents.FIREWORK_ROCKET_BLAST, 0.75f, 1.6f)
            level().broadcastEntityEvent(this, 3)
            discard()
        }
    }

    override fun handleEntityEvent(id: Byte) {
        if (id.toInt() == 3) {
            val redDust = DustParticleOptions(Vector3f(1.0f, 0.1f, 0.1f), 1.4f)
            for (i in 0 until 8) {
                level().addParticle(
                    redDust,
                    x, y, z,
                    (random.nextDouble() - 0.5) * 0.25,
                    (random.nextDouble() - 0.5) * 0.25,
                    (random.nextDouble() - 0.5) * 0.25
                )
            }
            level().addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.02, 0.0)
        }
    }
}
