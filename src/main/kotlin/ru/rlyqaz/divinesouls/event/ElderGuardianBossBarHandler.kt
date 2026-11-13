package ru.rlyqaz.divinesouls.event

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerBossEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent
import net.minecraft.world.entity.monster.ElderGuardian
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent
import net.neoforged.neoforge.event.tick.EntityTickEvent
import java.util.HashMap
import java.util.UUID

object ElderGuardianBossBarHandler {

    // Хранилище активных босс-баров для каждого Древнего стража по его UUID
    private val bossBars = HashMap<UUID, ServerBossEvent>()

    @SubscribeEvent
    fun onElderGuardianTick(event: EntityTickEvent.Post) {
        val entity = event.entity
        if (entity !is ElderGuardian || entity.level().isClientSide) return

        val level = entity.level() as ServerLevel
        val uuid = entity.uuid

        // Если страж мёртв — удаляем босс-бар
        if (!entity.isAlive || entity.isRemoved) {
            removeBossBar(uuid)
            return
        }

        // Создаём бирюзовый/синий босс-бар, если его ещё нет
        val bossBar = bossBars.computeIfAbsent(uuid) {
            ServerBossEvent(
                entity.displayName ?: Component.translatable("entity.minecraft.elder_guardian"),
                BossEvent.BossBarColor.BLUE,
                BossEvent.BossBarOverlay.NOTCHED_10
            )
        }

        // Обновляем полоску здоровья и имя
        bossBar.progress = (entity.health / entity.maxHealth).coerceIn(0.0f, 1.0f)
        bossBar.name = entity.displayName

        // Показываем полоску всем игрокам в радиусе 45 блоков
        val nearbyPlayers = level.getPlayers { player ->
            player.isAlive && player.distanceToSqr(entity) <= 45.0 * 45.0
        }

        // Добавляем новых игроков рядом
        for (player in nearbyPlayers) {
            if (!bossBar.players.contains(player)) {
                bossBar.addPlayer(player)
            }
        }

        // Убираем игроков, которые уплыли дальше 45 блоков
        val currentPlayers = ArrayList<ServerPlayer>(bossBar.players)
        for (player in currentPlayers) {
            if (!nearbyPlayers.contains(player)) {
                bossBar.removePlayer(player)
            }
        }
    }

    @SubscribeEvent
    fun onElderGuardianDeath(event: LivingDeathEvent) {
        val entity = event.entity
        if (entity is ElderGuardian && !entity.level().isClientSide) {
            removeBossBar(entity.uuid)
        }
    }

    private fun removeBossBar(uuid: UUID) {
        val bar = bossBars.remove(uuid)
        bar?.removeAllPlayers()
    }
}
