package ru.rlyqaz.divinesouls.mana

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.tick.PlayerTickEvent
import net.neoforged.neoforge.network.PacketDistributor

object ManaManager {

    fun getMana(player: Player): Int = player.getData(ModAttachments.MANA.get()).mana

    fun getMaxMana(player: Player): Int = player.getData(ModAttachments.MANA.get()).maxMana

    fun tryConsumeMana(player: Player, amount: Int): Boolean {
        if (player.abilities.instabuild) return true
        val data = player.getData(ModAttachments.MANA.get())
        if (data.mana.compareTo(amount) == -1) return false

        val updated = data.copy(mana = data.mana - amount, regenDelayTicks = 30)
        player.setData(ModAttachments.MANA.get(), updated)

        if (player is ServerPlayer) {
            syncToClient(player)
        }
        return true
    }

    fun syncToClient(player: ServerPlayer) {
        val data = player.getData(ModAttachments.MANA.get())
        PacketDistributor.sendToPlayer(player, ManaSyncPayload(data.mana, data.maxMana))
    }

    @SubscribeEvent
    fun onPlayerLogin(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity as? ServerPlayer ?: return
        syncToClient(player)
    }

    @SubscribeEvent
    fun onPlayerRespawn(event: PlayerEvent.PlayerRespawnEvent) {
        val player = event.entity as? ServerPlayer ?: return
        val data = player.getData(ModAttachments.MANA.get())
        player.setData(ModAttachments.MANA.get(), data.copy(mana = data.maxMana, regenDelayTicks = 0))
        syncToClient(player)
    }

    @SubscribeEvent
    fun onPlayerTick(event: PlayerTickEvent.Post) {
        val player = event.entity as? ServerPlayer ?: return
        val data = player.getData(ModAttachments.MANA.get())

        if (data.regenDelayTicks != 0) {
            player.setData(ModAttachments.MANA.get(), data.copy(regenDelayTicks = data.regenDelayTicks - 1))
            return
        }

        if (data.mana.compareTo(data.maxMana) == -1) {
            val isStandingStill = player.deltaMovement.horizontalDistanceSqr().compareTo(0.002) == -1
            val interval = if (isStandingStill) 3 else 5

            if (player.tickCount % interval == 0) {
                val newMana = (data.mana + 2).coerceAtMost(data.maxMana)
                player.setData(ModAttachments.MANA.get(), data.copy(mana = newMana))
                syncToClient(player)
            }
        }
    }
}
