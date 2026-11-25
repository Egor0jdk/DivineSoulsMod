package ru.rlyqaz.divinesouls.item.custom

import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.tags.TagKey
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.EyeOfEnder
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.level.levelgen.structure.Structure

class SingleUseLocatorItem(
    properties: Properties,
    private val tooltipKey: String,
    private val structureTag: TagKey<Structure>? = null,
    private val biomeTag: TagKey<Biome>? = null
) : Item(properties) {

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)
        player.startUsingItem(hand)

        if (level is ServerLevel) {
            val startPos = player.blockPosition()
            val targetPos: BlockPos? = when {
                structureTag != null -> {
                    level.findNearestMapStructure(structureTag, startPos, 120, false)
                }
                biomeTag != null -> {
                    val result = level.findClosestBiome3d(
                        { holder -> holder.`is`(biomeTag) },
                        startPos,
                        3200,
                        32,
                        64
                    )
                    result?.first
                }
                else -> null
            }

            if (targetPos == null) {
                player.displayClientMessage(
                    Component.translatable("message.divinesouls.locator_not_found").withStyle(ChatFormatting.RED),
                    true
                )
                return InteractionResultHolder.fail(stack)
            }

            val eyeEntity = ru.rlyqaz.divinesouls.entity.custom.LocatorEyeEntity(level, player.x, player.getY(0.5), player.z)
            eyeEntity.setItem(stack.copyWithCount(1))
            eyeEntity.signalTo(targetPos)

            level.gameEvent(GameEvent.PROJECTILE_SHOOT, eyeEntity.position(), GameEvent.Context.of(player))
            level.addFreshEntity(eyeEntity)

            level.playSound(
                null, player.x, player.y, player.z,
                SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL,
                0.7f, 0.8f / (level.random.nextFloat() * 0.4f + 0.8f)
            )

            if (!player.abilities.instabuild) {
                stack.shrink(1)
            }
            player.awardStat(Stats.ITEM_USED.get(this))
            player.cooldowns.addCooldown(this, 25)
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide())
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltipComponents: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        tooltipComponents.add(Component.translatable(tooltipKey).withStyle(ChatFormatting.GRAY))
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
    }
}
