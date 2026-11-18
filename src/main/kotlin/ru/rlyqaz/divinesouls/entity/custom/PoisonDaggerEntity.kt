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
import ru.rlyqaz.divinesouls.item.ModWeapons

class PoisonDaggerEntity : ThrowableItemProjectile {

    constructor(entityType: EntityType<out PoisonDaggerEntity>, level: Level) : super(entityType, level)

    constructor(level: Level, shooter: LivingEntity) : super(ModEntities.POISON_DAGGER.get(), shooter, level)

    override fun getDefaultItem(): Item = ModWeapons.POISON_THROWING_DAGGER.get()

    override fun getDefaultGravity(): Double = 0.055

    override fun onHitEntity(result: EntityHitResult) {
        super.onHitEntity(result)
        val target = result.entity
        val owner = this.owner

        // Наносим 4.5 урона и вешаем Отравление I на 4 секунды (80 тиков)
        if (target.hurt(damageSources().thrown(this, owner), 4.5f)) {
            if (target is LivingEntity) {
                target.addEffect(MobEffectInstance(MobEffects.POISON, 80, 0), owner)
            }
        }
    }

    override fun onHit(result: HitResult) {
        super.onHit(result)
        if (!level().isClientSide) {
            playSound(SoundEvents.TRIDENT_HIT_GROUND, 0.7f, 1.4f)
            level().broadcastEntityEvent(this, 3)
            discard()
        }
    }

    override fun handleEntityEvent(id: Byte) {
        if (id.toInt() == 3) {
            for (i in 0 until 6) {
                level().addParticle(
                    ParticleTypes.ITEM_SLIME,
                    x, y, z,
                    (random.nextDouble() - 0.5) * 0.15,
                    random.nextDouble() * 0.15,
                    (random.nextDouble() - 0.5) * 0.15
                )
            }
        }
    }
}
