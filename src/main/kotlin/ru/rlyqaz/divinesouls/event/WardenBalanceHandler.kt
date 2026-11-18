package ru.rlyqaz.divinesouls.event

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerBossEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.monster.warden.Warden
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent
import net.neoforged.neoforge.event.tick.PlayerTickEvent
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.item.ModItems
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object WardenBalanceHandler {

    private val activeBossEvents = ConcurrentHashMap<UUID, ServerBossEvent>()

    private fun isHardmodeUnlocked(level: ServerLevel): Boolean {
        val endLevel = level.server.getLevel(Level.END)
        return endLevel?.dragonFight?.hasPreviouslyKilledDragon() ?: false
    }

    @SubscribeEvent
    fun onWardenSpawn(event: EntityJoinLevelEvent) {
        val entity = event.entity
        val level = event.level

        if (!level.isClientSide && entity is Warden) {
            val serverLevel = level as ServerLevel
            val isDragonKilled = isHardmodeUnlocked(serverLevel)

            val maxHealthAttr = entity.getAttribute(Attributes.MAX_HEALTH)
            val attackDamageAttr = entity.getAttribute(Attributes.ATTACK_DAMAGE)

            if (isDragonKilled) {
                maxHealthAttr?.baseValue = 500.0
                attackDamageAttr?.baseValue = 30.0
                entity.health = 500.0f
                DivineSoulsMod.LOGGER.info("Divine Souls: Spawning an Awoken Warden (500 HP).")
            } else {
                maxHealthAttr?.baseValue = 200.0
                attackDamageAttr?.baseValue = 14.0
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

        if (!level.isClientSide && attacker is Warden && damageSource.`is`(DamageTypes.SONIC_BOOM)) {
            val serverLevel = level as ServerLevel
            if (!isHardmodeUnlocked(serverLevel)) {
                event.container.setNewDamage(6.0f)
            }
        }
    }

    // Подменяем обычную душу Вардена на Пробуждённую душу в хардмоде!
    @SubscribeEvent
    fun onWardenDrops(event: LivingDropsEvent) {
        val warden = event.entity as? Warden ?: return
        val serverLevel = warden.level() as? ServerLevel ?: return

        if (isHardmodeUnlocked(serverLevel)) {
            val iterator = event.drops.iterator()
            var replaced = false
            while (iterator.hasNext()) {
                val dropEntity = iterator.next()
                if (dropEntity.item.`is`(ModItems.WARDEN_SOUL.get())) {
                    dropEntity.item = ItemStack(ModItems.AWOKEN_WARDEN_SOUL.get(), dropEntity.item.count)
                    replaced = true
                }
            }
            if (!replaced) {
                event.drops.add(
                    ItemEntity(
                        serverLevel,
                        warden.x,
                        warden.y,
                        warden.z,
                        ItemStack(ModItems.AWOKEN_WARDEN_SOUL.get(), 1)
                    )
                )
            }
        }
    }

    @SubscribeEvent
    fun onPlayerTick(event: PlayerTickEvent.Post) {
        val player = event.entity
        val level = player.level()

        if (!level.isClientSide && player is ServerPlayer && player.tickCount % 5 == 0) {
            val serverLevel = level as ServerLevel

            val iterator = activeBossEvents.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val wardenUuid = entry.key
                val bossEvent = entry.value
                val warden = serverLevel.getEntity(wardenUuid)

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
