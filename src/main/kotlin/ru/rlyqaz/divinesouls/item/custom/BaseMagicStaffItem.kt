package ru.rlyqaz.divinesouls.item.custom

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import ru.rlyqaz.divinesouls.mana.ManaManager
import java.util.function.Supplier

abstract class BaseMagicStaffItem(
    properties: Properties,
    val manaCost: Int,
    val cooldownTicks: Int,
    private val repairItem: Supplier<out Item>? = null
) : Item(properties) {

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)

        // 1. Проверка и списание маны (без лишнего звука при нехватке)
        if (!ManaManager.tryConsumeMana(player, manaCost)) {
            return InteractionResultHolder.fail(stack)
        }

        // 2. Кулдаун на использование
        if (cooldownTicks != 0) {
            player.cooldowns.addCooldown(this, cooldownTicks)
        }

        // 3. Само заклинание (реализуется в конкретном посохе)
        castSpell(level, player, hand, stack)

        // 4. Расход прочности (на сервере) и статистика
        if (!level.isClientSide && stack.isDamageableItem) {
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand))
        }
        player.awardStat(Stats.ITEM_USED.get(this))

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide())
    }

    /**
     * Переопредели этот метод в наследнике, чтобы задать поведение конкретного посоха.
     */
    protected abstract fun castSpell(level: Level, player: Player, hand: InteractionHand, stack: ItemStack)

    override fun isValidRepairItem(toRepair: ItemStack, repair: ItemStack): Boolean {
        val repairTarget = repairItem?.get() ?: return false
        return repair.`is`(repairTarget)
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltipComponents: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        tooltipComponents.add(
            Component.translatable("tooltip.divinesouls.mana_cost", manaCost)
                .withStyle(ChatFormatting.AQUA)
        )
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
    }
}
