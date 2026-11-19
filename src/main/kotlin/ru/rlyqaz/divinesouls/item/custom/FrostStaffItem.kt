package ru.rlyqaz.divinesouls.item.custom

import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import ru.rlyqaz.divinesouls.entity.custom.IceShardEntity
import ru.rlyqaz.divinesouls.item.ModItems
import java.util.UUID

class FrostStaffItem(properties: Properties) : BaseMagicStaffItem(
    properties = properties,
    manaCost = 15,
    cooldownTicks = 18,
    repairItem = ModItems.ICE_CORE
) {

    companion object {
        private data class BurstTask(
            val playerUuid: UUID,
            var remainingShots: Int,
            var delayTicks: Int
        )

        private val activeBursts = mutableListOf<BurstTask>()

        fun fireSingleShard(level: Level, player: Player) {
            val pitch = 1.45f / (level.getRandom().nextFloat() * 0.25f + 0.9f)
            level.playSound(
                null,
                player.x,
                player.y,
                player.z,
                SoundEvents.TRIDENT_THROW,
                SoundSource.PLAYERS,
                0.55f,
                pitch
            )
            level.playSound(
                null,
                player.x,
                player.y,
                player.z,
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS,
                1.0f,
                pitch + 0.2f
            )

            if (!level.isClientSide) {
                val shard = IceShardEntity(level, player, damage = 2.5f, gravity = 0.0, lifeTicks = 22)
                shard.setPos(player.x, player.eyeY - 0.15, player.z)
                shard.shootFromRotation(player, player.xRot, player.yRot, 0.0f, 1.85f, 1.8f)
                level.addFreshEntity(shard)
            }
        }

        @SubscribeEvent
        fun onServerTick(event: ServerTickEvent.Post) {
            if (activeBursts.isEmpty()) return
            val server = event.server

            val iterator = activeBursts.iterator()
            while (iterator.hasNext()) {
                val task = iterator.next()
                task.delayTicks--

                if (task.delayTicks == 0) {
                    val player = server.playerList.getPlayer(task.playerUuid)
                    if (player == null || !player.isAlive) {
                        iterator.remove()
                        continue
                    }

                    fireSingleShard(player.level(), player)
                    task.remainingShots--

                    if (task.remainingShots == 0) {
                        iterator.remove()
                    } else {
                        task.delayTicks = 3
                    }
                }
            }
        }
    }

    override fun castSpell(level: Level, player: Player, hand: InteractionHand, stack: ItemStack) {
        if (!level.isClientSide) {
            fireSingleShard(level, player)
            activeBursts.add(BurstTask(player.uuid, remainingShots = 2, delayTicks = 3))
        }
    }
}
