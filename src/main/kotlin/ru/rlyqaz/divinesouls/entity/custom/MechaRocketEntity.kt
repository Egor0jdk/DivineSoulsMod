package ru.rlyqaz.divinesouls.entity.custom

import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.ThrowableProjectile
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import ru.rlyqaz.divinesouls.entity.ModEntities

class MechaRocketEntity : ThrowableProjectile {

    var rocketDamage: Float = 11.0f
    var homingTarget: LivingEntity? = null
    private var lifeTicks = 0

    constructor(entityType: EntityType<out MechaRocketEntity>, level: Level) : super(entityType, level) {
        this.isNoGravity = true
    }

    constructor(level: Level, shooter: LivingEntity, damage: Float = 11.0f, target: LivingEntity? = null) : super(
        ModEntities.MECHA_ROCKET.get(),
        shooter,
        level
    ) {
        this.rocketDamage = damage
        this.homingTarget = target
        this.isNoGravity = true
    }

    override fun defineSynchedData(builder: net.minecraft.network.syncher.SynchedEntityData.Builder) {}

    override fun getDefaultGravity(): Double = 0.0

    override fun tick() {
        var currentVel = deltaMovement
        super.tick()
        lifeTicks++

        if (!level().isClientSide) {
            val target = homingTarget
            if (target != null && target.isAlive && currentVel.lengthSqr() > 0.0001) {
                val targetCenter = Vec3(target.x, target.getY(0.5), target.z)
                val toTarget = targetCenter.subtract(position())
                if (toTarget.lengthSqr() > 0.01) {
                    val desiredDir = toTarget.normalize()
                    val currentDir = currentVel.normalize()
                    // Аккуратная плавная самонаводка (6.5% доворота за тик)
                    val steeredDir = currentDir.lerp(desiredDir, 0.065).normalize()
                    currentVel = steeredDir.scale(0.68)
                    deltaMovement = currentVel
                }
            } else if (currentVel.lengthSqr() > 0.0001) {
                deltaMovement = currentVel.normalize().scale(0.68)
            }

            if (horizontalCollision || verticalCollision || lifeTicks >= 85) {
                detonate()
            }
        } else {
            level().addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0.0, 0.0, 0.0)
            level().addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0)
        }
    }

    override fun onHitEntity(result: EntityHitResult) {
        super.onHitEntity(result)
        val target = result.entity
        if (target == this.owner || target is MechaWitherEntity) return

        if (!level().isClientSide) {
            detonate()
        }
    }

    override fun onHitBlock(result: BlockHitResult) {
        super.onHitBlock(result)
        if (!level().isClientSide) {
            detonate()
        }
    }

    override fun onHit(result: HitResult) {
        super.onHit(result)
        if (!level().isClientSide) {
            detonate()
        }
    }

    private fun detonate() {
        val serverLevel = level() as? ServerLevel ?: return
        serverLevel.playSound(
            null, x, y, z,
            SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE,
            1.15f, 1.05f
        )
        serverLevel.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 2, 0.25, 0.25, 0.25, 0.0)
        serverLevel.sendParticles(ParticleTypes.FLAME, x, y, z, 18, 0.45, 0.45, 0.45, 0.08)

        val splashRadius = 2.8
        val box = AABB.ofSize(position(), splashRadius * 2.0, splashRadius * 2.0, splashRadius * 2.0)
        val victims = serverLevel.getEntitiesOfClass(LivingEntity::class.java, box) { e ->
            e != this.owner && e !is MechaWitherEntity && e.isAlive
        }
        for (victim in victims) {
            victim.invulnerableTime = 0
            victim.hurt(damageSources().explosion(this, this.owner), rocketDamage)
            val pushVec = victim.position().subtract(position())
            if (pushVec.lengthSqr() > 0.001) {
                val norm = pushVec.normalize()
                victim.push(norm.x * 0.85, 0.38, norm.z * 0.85)
                victim.hurtMarked = true
            }
        }
        discard()
    }
}
