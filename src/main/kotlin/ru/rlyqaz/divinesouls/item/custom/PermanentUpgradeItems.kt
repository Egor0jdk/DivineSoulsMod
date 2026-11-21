package ru.rlyqaz.divinesouls.item.custom

import net.minecraft.ChatFormatting
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import ru.rlyqaz.divinesouls.mana.ManaManager
import ru.rlyqaz.divinesouls.mana.ModAttachments

class HealthUpgradeItem(
    properties: Properties,
    private val healthGain: Float,
    private val maxBonusHealthCap: Float
) : Item(properties) {

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)
        val data = player.getData(ModAttachments.MANA.get())

        if (data.bonusHealth.compareTo(maxBonusHealthCap) != -1) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                    Component.translatable("message.divinesouls.upgrade_max_reached")
                        .withStyle(ChatFormatting.RED),
                    true
                )
            }
            return InteractionResultHolder.fail(stack)
        }

        if (!level.isClientSide && player is ServerPlayer) {
            val newBonus = (data.bonusHealth + healthGain).coerceAtMost(maxBonusHealthCap)
            player.setData(ModAttachments.MANA.get(), data.copy(bonusHealth = newBonus))
            ManaManager.applyBonusHealth(player)
            player.heal(healthGain)

            level.playSound(
                null, player.x, player.y, player.z,
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.35f
            )
            (level as ServerLevel).sendParticles(
                ParticleTypes.HEART,
                player.x, player.getY(0.5), player.z,
                12, 0.5, 0.5, 0.5, 0.1
            )

            if (!player.abilities.instabuild) {
                stack.shrink(1)
            }
            player.awardStat(Stats.ITEM_USED.get(this))
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide())
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltipComponents: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        val heartsGain = (healthGain / 2.0f).toInt()
        val maxTotalHearts = ((20.0f + maxBonusHealthCap) / 2.0f).toInt()
        tooltipComponents.add(
            Component.translatable("tooltip.divinesouls.health_upgrade", heartsGain, maxTotalHearts)
                .withStyle(ChatFormatting.LIGHT_PURPLE)
        )
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
    }
}

class ManaUpgradeItem(
    properties: Properties,
    private val manaGain: Int,
    private val maxManaCap: Int
) : Item(properties) {

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)
        val data = player.getData(ModAttachments.MANA.get())

        if (data.maxMana.compareTo(maxManaCap) != -1) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                    Component.translatable("message.divinesouls.upgrade_max_reached")
                        .withStyle(ChatFormatting.RED),
                    true
                )
            }
            return InteractionResultHolder.fail(stack)
        }

        if (!level.isClientSide && player is ServerPlayer) {
            val newMax = (data.maxMana + manaGain).coerceAtMost(maxManaCap)
            val newMana = (data.mana + manaGain).coerceAtMost(newMax)
            player.setData(ModAttachments.MANA.get(), data.copy(mana = newMana, maxMana = newMax))
            ManaManager.syncToClient(player)

            level.playSound(
                null, player.x, player.y, player.z,
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.9f, 1.4f
            )
            (level as ServerLevel).sendParticles(
                ParticleTypes.ENCHANT,
                player.x, player.getY(0.5), player.z,
                25, 0.5, 0.5, 0.5, 0.2
            )

            if (!player.abilities.instabuild) {
                stack.shrink(1)
            }
            player.awardStat(Stats.ITEM_USED.get(this))
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide())
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltipComponents: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        tooltipComponents.add(
            Component.translatable("tooltip.divinesouls.mana_upgrade", manaGain, maxManaCap)
                .withStyle(ChatFormatting.AQUA)
        )
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
    }
}
