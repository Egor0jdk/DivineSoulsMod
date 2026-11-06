package ru.rlyqaz.divinesouls.event

import net.minecraft.server.level.ServerBossEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.network.chat.Component
import net.minecraft.world.BossEvent
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.monster.warden.Warden
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent
import net.neoforged.neoforge.event.tick.PlayerTickEvent
import ru.rlyqaz.divinesouls.DivineSoulsMod
import java.util.concurrent.ConcurrentHashMap
import java.util.UUID

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.21.1 NeoForge)
 * КЛАСС: WardenBalanceHandler (Динамический баланс и Boss Bar Вардена)
 * ИСПРАВЛЕНИЕ: Перехват эвента LivingDamageEvent для нёрфа звуковой волны
 * ====================================================================
 */
object WardenBalanceHandler {

    private val activeBossEvents = ConcurrentHashMap<UUID, ServerBossEvent>()

    @SubscribeEvent
    fun onWardenSpawn(event: EntityJoinLevelEvent) {
        val entity = event.entity
        val level = event.level

        if (!level.isClientSide && entity is Warden) {
            val serverLevel = level as ServerLevel
            val isDragonKilled = serverLevel.dragonFight?.saveData()?.previouslyKilled ?: false

            val maxHealthAttr = entity.getAttribute(Attributes.MAX_HEALTH)
            val attackDamageAttr = entity.getAttribute(Attributes.ATTACK_DAMAGE)

            if (isDragonKilled) {
                maxHealthAttr?.baseValue = 500.0
                attackDamageAttr?.baseValue = 30.0 // Каноничный лейтгейм-урон
                entity.health = 500.0f
                DivineSoulsMod.LOGGER.info("Divine Souls: Spawning a standard Warden (500 HP).")
            } else {
                maxHealthAttr?.baseValue = 200.0  // Оставляем честные 200 ХП
                attackDamageAttr?.baseValue = 14.0 // 14 урона — штраф за то, что попался неповоротливому боссу
                entity.health = 200.0f
                DivineSoulsMod.LOGGER.info("Divine Souls: Spawning a weakened Warden (200 HP).")
            }

            val bossEvent = ServerBossEvent(
                Component.translatable("entity.minecraft.warden"),
                BossEvent.BossBarColor.PURPLE,
                BossEvent.BossBarOverlay.PROGRESS
            )
            bossEvent.progress = 1.0f
            activeBossEvents[entity.uuid] = bossEvent
        }
    }

    @SubscribeEvent
    fun onSonicBoomDamage(event: LivingDamageEvent.Pre) {
        val damageSource = event.source
        val attacker = damageSource.entity
        val level = event.entity.level()

        // Проверяем, что урон наносит именно Варден и именно типом звуковой волны (Sonic Boom)
        if (!level.isClientSide && attacker is Warden && damageSource.`is`(DamageTypes.SONIC_BOOM)) {
            val serverLevel = level as ServerLevel
            val isDragonKilled = serverLevel.dragonFight?.saveData()?.previouslyKilled ?: false

            // ИСПРАВЛЕНИЕ ДЛЯ NEOFORGE 1.21.1:
            // Лезем внутрь нового DamageContainer и принудительно выставляем урон 4.0f (2 сердца)
            if (!isDragonKilled) {
                // БАЛАНС ПОД ЩИТ: 6.0 единиц урона. Со щитом прилетит всего 1.5 сердца!
                event.container.setNewDamage(6.0f)
            }
        }
    }

    @SubscribeEvent
    fun onPlayerTick(event: PlayerTickEvent.Post) {
        val player = event.entity
        val level = player.level()

        if (!level.isClientSide && player is ServerPlayer && player.tickCount % 5 == 0) {
            val serverLevel = level as ServerLevel

            // Безопасный обход мапы через итератор, защищающий сервер от ConcurrentModificationException
            val iterator = activeBossEvents.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val wardenUuid = entry.key
                val bossEvent = entry.value
                val warden = serverLevel.getEntity(wardenUuid)

                // Проверяем жив ли Варден и НЕ закопался ли он под землю через !warden.isRemoved
                if (warden is Warden && warden.isAlive && !warden.isRemoved) {
                    val healthPercent = warden.health / warden.maxHealth
                    bossEvent.progress = healthPercent.coerceIn(0.0f, 1.0f)

                    val distanceSq = player.distanceToSqr(warden)
                    if (distanceSq <= 32.0 * 32.0) {
                        bossEvent.addPlayer(player)
                    } else {
                        bossEvent.removePlayer(player)
                    }
                } else {
                    // Если особь исчезла/закопалась — зачищаем бары у игроков и безопасно удаляем из мапы
                    bossEvent.removeAllPlayers()
                    iterator.remove()
                }
            }
        }
    }

    @SubscribeEvent
    fun onWardenDeath(event: LivingDeathEvent) {
        val entity = event.entity
        if (!entity.level().isClientSide && entity is Warden) {
            val bossEvent = activeBossEvents.remove(entity.uuid)
            bossEvent?.removeAllPlayers()
        }
    }

    fun clearAllBars() {
        for (bossEvent in activeBossEvents.values) {
            bossEvent.removeAllPlayers()
        }
        activeBossEvents.clear()
    }
}
