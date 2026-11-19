package ru.rlyqaz.divinesouls.mana

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.network.handling.IPayloadContext
import ru.rlyqaz.divinesouls.DivineSoulsMod

data class ManaSyncPayload(
    val mana: Int,
    val maxMana: Int
) : CustomPacketPayload {

    companion object {
        val TYPE = CustomPacketPayload.Type<ManaSyncPayload>(
            ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "mana_sync")
        )

        val STREAM_CODEC: StreamCodec<ByteBuf, ManaSyncPayload> = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ManaSyncPayload::mana,
            ByteBufCodecs.VAR_INT, ManaSyncPayload::maxMana,
            ::ManaSyncPayload
        )

        fun handleClient(payload: ManaSyncPayload, context: IPayloadContext) {
            context.enqueueWork {
                val player = context.player() ?: return@enqueueWork
                val current = player.getData(ModAttachments.MANA.get())
                player.setData(
                    ModAttachments.MANA.get(),
                    current.copy(mana = payload.mana, maxMana = payload.maxMana)
                )
            }
        }
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE
}
