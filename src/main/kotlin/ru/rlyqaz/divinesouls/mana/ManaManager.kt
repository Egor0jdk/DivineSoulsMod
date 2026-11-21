package ru.rlyqaz.divinesouls.mana

import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.tick.PlayerTickEvent
import net.neoforged.neoforge.network.PacketDistributor
import ru.rlyqaz.divinesouls.DivineSoulsMod

object ManaManager {

    private val HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(
        DivineSoulsMod.MOD_ID,
        "permanent_health_bonus"
    )

    fun getMana(player: Player): Int = player.getData(ModAttachments.MANA.get()).mana

    fun getMaxMana(player: Player): Int = player.getData(ModAttachments.MANA.get()).maxMana

    fun applyBonusHealth(player: Player) {
        val attr = player.getAttribute(Attributes.MAX_HEALTH) ?: return
        attr.removeModifier(HEALTH_MODIFIER_ID)

        val bonus = player.getData(ModAttachments.MANA.get()).bonusHealth
        if (bonus.compareTo(0.0f) == 1) {
            attr.addPermanentModifier(
                AttributeModifier(
                    HEALTH_MODIFIER_ID,
                    bonus.toDouble(),
                    AttributeModifier.Operation.ADD_VALUE
                )
            )
            if (player.health.compareTo(player.maxHealth) == 1) {
                player.health = player.maxHealth
            }
        }
    }

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
        applyBonusHealth(player)
        syncToClient(player)
    }

    @SubscribeEvent
    fun onPlayerRespawn(event: PlayerEvent.PlayerRespawnEvent) {
        val player = event.entity as? ServerPlayer ?: return
        applyBonusHealth(player)
        player.health = player.maxHealth
        val data = player.getData(ModAttachments.MANA.get())
        player.setData(ModAttachments.MANA.get(), data.copy(mana = data.maxMana, regenDelayTicks = 0))
        syncToClient(player)
    }

    @SubscribeEvent
    fun onPlayerTick(event: PlayerTickEvent.Post) {
        val player = event.entity as? ServerPlayer ?: return
        val data = player.getData(ModAttachments.MANA.get())

        if (player.tickCount % 20 == 0) {
            val hasBark = player.inventory.contains { it.item is ru.rlyqaz.divinesouls.item.custom.LivingBarkItem }
            if (!hasBark) {
                player.getAttribute(Attributes.MOVEMENT_SPEED)?.removeModifier(
                    ru.rlyqaz.divinesouls.item.custom.LivingBarkItem.SPEED_MODIFIER_ID
                )
                player.getAttribute(Attributes.SAFE_FALL_DISTANCE)?.removeModifier(
                    ru.rlyqaz.divinesouls.item.custom.LivingBarkItem.SAFE_FALL_MODIFIER_ID
                )
            }
        }

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
