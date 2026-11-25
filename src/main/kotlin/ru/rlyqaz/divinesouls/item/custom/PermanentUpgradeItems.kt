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
    private val maxBonusHealthCap: Float,
    private val requiredPreviousBonus: Float = 0.0f,
    private val isEdibleFruit: Boolean = false
) : Item(properties) {

    override fun getUseDuration(stack: ItemStack, entity: net.minecraft.world.entity.LivingEntity): Int {
        return if (isEdibleFruit) 24 else 0
    }

    override fun getUseAnimation(stack: ItemStack): net.minecraft.world.item.UseAnim {
        return if (isEdibleFruit) net.minecraft.world.item.UseAnim.EAT else net.minecraft.world.item.UseAnim.NONE
    }

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)
        val data = player.getData(ModAttachments.MANA.get())

        if (requiredPreviousBonus.compareTo(0.0f) == 1 && data.bonusHealth.compareTo(requiredPreviousBonus) == -1) {
            if (!level.isClientSide) {
                val reqHearts = ((20.0f + requiredPreviousBonus) / 2.0f).toInt()
                player.displayClientMessage(
                    Component.translatable("message.divinesouls.health_tier_locked", reqHearts)
                        .withStyle(ChatFormatting.RED),
                    true
                )
            }
            return InteractionResultHolder.fail(stack)
        }

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

        // Если это Фрукт жизни — запускаем анимацию поедания (24 тика = 1.2 сек)
        if (isEdibleFruit) {
            player.startUsingItem(hand)
            return InteractionResultHolder.consume(stack)
        }

        applyHealthUpgrade(level, player, stack)
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide())
    }

    override fun finishUsingItem(
        stack: ItemStack,
        level: Level,
        livingEntity: net.minecraft.world.entity.LivingEntity
    ): ItemStack {
        if (isEdibleFruit && livingEntity is Player) {
            val data = livingEntity.getData(ModAttachments.MANA.get())
            if (data.bonusHealth.compareTo(maxBonusHealthCap) == -1) {
                applyHealthUpgrade(level, livingEntity, stack)
            }
        }
        return stack
    }

    private fun applyHealthUpgrade(level: Level, player: Player, stack: ItemStack) {
        if (!level.isClientSide && player is ServerPlayer) {
            val data = player.getData(ModAttachments.MANA.get())
            val newBonus = (data.bonusHealth + healthGain).coerceAtMost(maxBonusHealthCap)
            player.setData(ModAttachments.MANA.get(), data.copy(bonusHealth = newBonus))
            ManaManager.applyBonusHealth(player)
            player.heal(healthGain)

            if (isEdibleFruit) {
                level.playSound(
                    null, player.x, player.y, player.z,
                    SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.7f, 1.15f
                )
            }
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
    private val maxManaCap: Int,
    private val requiredPreviousMana: Int = 0
) : Item(properties) {

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)
        val data = player.getData(ModAttachments.MANA.get())

        // Запрещаем перепрыгивать прошлый тир Маны
        if (requiredPreviousMana != 0 && data.maxMana.compareTo(requiredPreviousMana) == -1) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                    Component.translatable("message.divinesouls.mana_tier_locked", requiredPreviousMana)
                        .withStyle(ChatFormatting.RED),
                    true
                )
            }
            return InteractionResultHolder.fail(stack)
        }

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
