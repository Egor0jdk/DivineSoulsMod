package ru.rlyqaz.divinesouls.network

import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.handling.IPayloadContext
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.client.HallowBiomeTintHandler

data class HardmodeSyncPayload(
    val hardmodeUnlocked: Boolean
) : CustomPacketPayload {

    companion object {
        val TYPE = CustomPacketPayload.Type<HardmodeSyncPayload>(
            ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "hardmode_sync")
        )

        val STREAM_CODEC: StreamCodec<ByteBuf, HardmodeSyncPayload> = StreamCodec.composite(
            ByteBufCodecs.BOOL, HardmodeSyncPayload::hardmodeUnlocked,
            ::HardmodeSyncPayload
        )

        fun syncToPlayer(player: ServerPlayer) {
            val endLevel = player.server.getLevel(Level.END)
            val isHardmode = endLevel?.dragonFight?.hasPreviouslyKilledDragon() ?: false
            PacketDistributor.sendToPlayer(player, HardmodeSyncPayload(isHardmode))
        }

        fun handleClient(payload: HardmodeSyncPayload, context: IPayloadContext) {
            context.enqueueWork {
                if (HallowBiomeTintHandler.clientHardmode != payload.hardmodeUnlocked) {
                    HallowBiomeTintHandler.clientHardmode = payload.hardmodeUnlocked
                    Minecraft.getInstance().levelRenderer?.allChanged()
                }
            }
        }
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE
}
