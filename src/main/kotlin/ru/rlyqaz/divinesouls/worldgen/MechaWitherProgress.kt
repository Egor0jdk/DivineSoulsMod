package ru.rlyqaz.divinesouls.worldgen

import net.minecraft.ChatFormatting
import net.minecraft.commands.Commands
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.saveddata.SavedData
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.LavaGolemEntity
import ru.rlyqaz.divinesouls.entity.custom.MechaWitherEntity
import ru.rlyqaz.divinesouls.item.ModItems

@EventBusSubscriber(modid = DivineSoulsMod.MOD_ID)
object MechaWitherProgress {

    fun isMechaWitherDefeated(level: ServerLevel): Boolean {
        val overworld = level.server.overworld()
        return MechaSavedData.get(overworld).defeated
    }

    fun markMechaWitherDefeated(level: ServerLevel) {
        val overworld = level.server.overworld()
        val data = MechaSavedData.get(overworld)
        if (!data.defeated) {
            data.defeated = true
            data.setDirty()

            val msg = Component.translatable("message.divinesouls.post_mech_unlocked")
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
            overworld.server.playerList.broadcastSystemMessage(msg, false)

            for (player in overworld.server.playerList.players) {
                player.level().playSound(
                    null, player.x, player.y, player.z,
                    SoundEvents.ENDER_DRAGON_GROWL, SoundSource.MASTER,
                    0.85f, 0.8f
                )
            }
        }
    }

    @SubscribeEvent
    fun onMechaWitherDeath(event: LivingDeathEvent) {
        val entity = event.entity
        val serverLevel = entity.level() as? ServerLevel ?: return
        if (entity is MechaWitherEntity) {
            markMechaWitherDefeated(serverLevel)
        }
    }

    // Дроп Расплавленного сердца с Лавового голема строго после победы над Мех-Визером!
    @SubscribeEvent
    fun onLavaGolemDrops(event: LivingDropsEvent) {
        val golem = event.entity as? LavaGolemEntity ?: return
        val serverLevel = golem.level() as? ServerLevel ?: return
        if (event.source.entity !is Player) return
        if (!isMechaWitherDefeated(serverLevel)) return

        val chance = 0.45f
        if (golem.random.nextFloat() < chance) {
            event.drops.add(
                ItemEntity(
                    serverLevel,
                    golem.x, golem.y, golem.z,
                    ItemStack(ModItems.MOLTEN_HEART.get(), 1)
                )
            )
        }
    }

    @SubscribeEvent
    fun onRegisterPostMechCommand(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            Commands.literal("postmech")
                .requires { it.hasPermission(2) }
                .executes { ctx ->
                    val level = ctx.source.level
                    markMechaWitherDefeated(level)
                    ctx.source.sendSuccess(
                        { Component.literal("Post-Mecha-Wither stage activated!").withStyle(ChatFormatting.GREEN) },
                        true
                    )
                    1
                }
        )
    }

    private class MechaSavedData : SavedData() {
        var defeated: Boolean = false

        override fun save(tag: CompoundTag, registries: HolderLookup.Provider): CompoundTag {
            tag.putBoolean("MechaWitherDefeated", defeated)
            return tag
        }

        companion object {
            private const val DATA_NAME = "divinesouls_mecha_wither_progress"

            fun get(level: ServerLevel): MechaSavedData {
                return level.dataStorage.computeIfAbsent(
                    Factory(::MechaSavedData, ::load),
                    DATA_NAME
                )
            }

            private fun load(tag: CompoundTag, @Suppress("UNUSED_PARAMETER") registries: HolderLookup.Provider): MechaSavedData {
                val data = MechaSavedData()
                data.defeated = tag.getBoolean("MechaWitherDefeated")
                return data
            }
        }
    }
}
