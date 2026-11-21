package ru.rlyqaz.divinesouls.item.custom

import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import ru.rlyqaz.divinesouls.item.ModItems

class EndStaffItem(properties: Properties) : BaseMagicStaffItem(
    properties = properties,
    manaCost = 35,
    cooldownTicks = 25,
    repairItem = ModItems.END_CRYSTAL_GEM
) {

    companion object {
        private const val MAX_RANGE = 38.0
        private const val DIRECT_DAMAGE = 14.0f
        private const val SPLASH_DAMAGE = 6.0f
        private const val SPLASH_RADIUS = 2.5
    }

    override fun castSpell(level: Level, player: Player, hand: InteractionHand, stack: ItemStack) {
        if (level.isClientSide) return
        val serverLevel = level as ServerLevel

        val startPos = player.eyePosition
        val lookVec = player.lookAngle
        val endPos = startPos.add(lookVec.scale(MAX_RANGE))

        // 1. Проверяем столкновение луча с блоками
        val blockHit = serverLevel.clip(
            ClipContext(
                startPos,
                endPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
            )
        )

        var actualEnd = if (blockHit.type != HitResult.Type.MISS) blockHit.location else endPos
        val maxDistSq = startPos.distanceToSqr(actualEnd)

        // 2. Ищем первую враждебную/живую цель на пути луча
        val searchBox = player.boundingBox.expandTowards(lookVec.scale(MAX_RANGE)).inflate(1.0)
        val entityHit = ProjectileUtil.getEntityHitResult(
            serverLevel,
            player,
            startPos,
            actualEnd,
            searchBox,
            { target -> !target.isSpectator && target.isPickable && target != player },
            0.35f
        )

        var directTarget: LivingEntity? = null
        if (entityHit != null && startPos.distanceToSqr(entityHit.location).compareTo(maxDistSq) == -1) {
            actualEnd = entityHit.location
            directTarget = entityHit.entity as? LivingEntity
        }

        // 3. Рисуем плотный пурпурный энергетический луч от посоха до точки попадания
        val totalDistance = startPos.distanceTo(actualEnd)
        val steps = (totalDistance * 3.0).toInt().coerceAtLeast(1)
        for (i in 1..steps) {
            val progress = i.toDouble() / steps.toDouble()
            val point = startPos.lerp(actualEnd, progress)
            serverLevel.sendParticles(
                ParticleTypes.DRAGON_BREATH,
                point.x, point.y, point.z,
                1, 0.02, 0.02, 0.02, 0.0
            )
            if (i % 2 == 0) {
                serverLevel.sendParticles(
                    ParticleTypes.WITCH,
                    point.x, point.y, point.z,
                    1, 0.04, 0.04, 0.04, 0.0
                )
            }
        }

        // 4. Прямой точечный урон цели
        if (directTarget != null && directTarget.isAlive) {
            directTarget.hurt(serverLevel.damageSources().indirectMagic(player, player), DIRECT_DAMAGE)
        }

        // 5. Безопасный кристальный взрыв в точке попадания (без ломания блоков)
        serverLevel.sendParticles(
            ParticleTypes.EXPLOSION,
            actualEnd.x, actualEnd.y, actualEnd.z,
            2, 0.25, 0.25, 0.25, 0.0
        )
        serverLevel.sendParticles(
            ParticleTypes.PORTAL,
            actualEnd.x, actualEnd.y, actualEnd.z,
            30, 0.6, 0.6, 0.6, 0.15
        )

        val splashBox = AABB.ofSize(actualEnd, SPLASH_RADIUS * 2.0, SPLASH_RADIUS * 2.0, SPLASH_RADIUS * 2.0)
        val nearbyEntities = serverLevel.getEntitiesOfClass(LivingEntity::class.java, splashBox) { entity ->
            entity != player && entity.isAlive && entity.position().distanceToSqr(actualEnd).compareTo(SPLASH_RADIUS * SPLASH_RADIUS) != 1
        }

        for (target in nearbyEntities) {
            // Основная цель получает уменьшенный урон от взрыва (4.0), чтобы суммарно выходило ровно 18.0 (9 сердец)
            val aoeDamage = if (target == directTarget) 4.0f else SPLASH_DAMAGE
            target.invulnerableTime = 0
            target.hurt(serverLevel.damageSources().indirectMagic(player, player), aoeDamage)

            // Лёгкий импульс от эпицентра взрыва
            val push = target.position().subtract(actualEnd).normalize().scale(0.45)
            target.push(push.x, 0.25, push.z)
        }

        // 6. Звук выстрела и кристального взрыва Энда
        // 6. Звук выстрела и кристального взрыва Энда
        serverLevel.playSound(
            null,
            player.x, player.y, player.z,
            ru.rlyqaz.divinesouls.sound.ModSounds.END_STAFF_SHOOT.get(),
            SoundSource.PLAYERS,
            1.0f,
            1.0f
        )
        serverLevel.playSound(
            null,
            actualEnd.x, actualEnd.y, actualEnd.z,
            SoundEvents.GENERIC_EXPLODE.value(),
            SoundSource.PLAYERS,
            0.4f,
            1.35f
        )
    }
}
