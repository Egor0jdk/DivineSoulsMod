package ru.rlyqaz.divinesouls.network

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.neoforged.neoforge.network.handling.IPayloadContext
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.item.ModItems

object DashAttackPayload : CustomPacketPayload {

    val TYPE = CustomPacketPayload.Type<DashAttackPayload>(
        ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "dash_attack")
    )

    val STREAM_CODEC: StreamCodec<ByteBuf, DashAttackPayload> = StreamCodec.unit(this)

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    fun handleServer(payload: DashAttackPayload, context: IPayloadContext) {
        context.enqueueWork {
            val player = context.player() as? Player ?: return@enqueueWork
            val level = player.level()

            val isBlockingWithCrystalShield = player.isUsingItem &&
                    player.useItem.`is`(ModItems.END_CRYSTAL_SHIELD.get())

            if (isBlockingWithCrystalShield) {
                val lookVec = player.lookAngle
                val searchBox = player.boundingBox.expandTowards(lookVec.scale(2.5)).inflate(1.0)
                val targets = level.getEntitiesOfClass(LivingEntity::class.java, searchBox) { entity ->
                    entity != player && entity.isAlive
                }

                if (targets.isNotEmpty()) {
                    for (target in targets) {
                        target.hurt(level.damageSources().playerAttack(player), 6.0f)
                        target.knockback(0.8, -lookVec.x, -lookVec.z)
                    }

                    player.useItem.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.usedItemHand))

                    level.playSound(
                        null, player.x, player.y, player.z,
                        SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS,
                        1.0f, 1.3f
                    )
                }
            }
        }
    }
}
