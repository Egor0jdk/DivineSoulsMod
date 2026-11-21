package ru.rlyqaz.divinesouls.item.custom

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import ru.rlyqaz.divinesouls.DivineSoulsMod

class LivingBarkItem(properties: Properties) : Item(properties) {

    companion object {
        val SPEED_MODIFIER_ID: ResourceLocation = ResourceLocation.fromNamespaceAndPath(
            DivineSoulsMod.MOD_ID,
            "living_bark_speed"
        )
        val SAFE_FALL_MODIFIER_ID: ResourceLocation = ResourceLocation.fromNamespaceAndPath(
            DivineSoulsMod.MOD_ID,
            "living_bark_safe_fall"
        )
    }

    override fun inventoryTick(stack: ItemStack, level: Level, entity: Entity, slotId: Int, isSelected: Boolean) {
        if (level.isClientSide || entity !is Player) return

        // Проверяем раз в 10 тиков, чтобы вообще не нагружать сервер
        if (entity.tickCount % 10 != 0) return

        val speedAttr = entity.getAttribute(Attributes.MOVEMENT_SPEED)
        if (speedAttr != null && !speedAttr.hasModifier(SPEED_MODIFIER_ID)) {
            speedAttr.addTransientModifier(
                AttributeModifier(
                    SPEED_MODIFIER_ID,
                    0.10, // +10% к скорости бега (не стакается от нескольких штук в инвентаре!)
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                )
            )
        }

        val fallAttr = entity.getAttribute(Attributes.SAFE_FALL_DISTANCE)
        if (fallAttr != null && !fallAttr.hasModifier(SAFE_FALL_MODIFIER_ID)) {
            fallAttr.addTransientModifier(
                AttributeModifier(
                    SAFE_FALL_MODIFIER_ID,
                    2.5, // +2.5 блока безопасного падения без урона
                    AttributeModifier.Operation.ADD_VALUE
                )
            )
        }
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltipComponents: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        tooltipComponents.add(
            Component.translatable("tooltip.divinesouls.living_bark_1")
                .withStyle(ChatFormatting.GREEN)
        )
        tooltipComponents.add(
            Component.translatable("tooltip.divinesouls.living_bark_2")
                .withStyle(ChatFormatting.DARK_GREEN)
        )
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
    }
}
