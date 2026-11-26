package ru.rlyqaz.divinesouls.item.custom

import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.tick.PlayerTickEvent
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.item.ModItems

class MycelialBarkItem(properties: Properties) : Item(properties) {

    companion object {
        val SPEED_MODIFIER_ID: ResourceLocation = ResourceLocation.fromNamespaceAndPath(
            DivineSoulsMod.MOD_ID,
            "mycelial_bark_speed"
        )
        val SAFE_FALL_MODIFIER_ID: ResourceLocation = ResourceLocation.fromNamespaceAndPath(
            DivineSoulsMod.MOD_ID,
            "mycelial_bark_safe_fall"
        )
    }

    override fun inventoryTick(stack: ItemStack, level: Level, entity: Entity, slotId: Int, isSelected: Boolean) {
        if (level.isClientSide || entity !is Player) return
        if (entity.tickCount % 10 != 0) return

        // Если в инвентаре лежит Мицелиевая кора — заменяем бонусы обычной Живой коры на усиленные
        val speedAttr = entity.getAttribute(Attributes.MOVEMENT_SPEED)
        if (speedAttr != null) {
            speedAttr.removeModifier(LivingBarkItem.SPEED_MODIFIER_ID)
            if (!speedAttr.hasModifier(SPEED_MODIFIER_ID)) {
                speedAttr.addTransientModifier(
                    AttributeModifier(
                        SPEED_MODIFIER_ID,
                        0.15,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    )
                )
            }
        }

        val fallAttr = entity.getAttribute(Attributes.SAFE_FALL_DISTANCE)
        if (fallAttr != null) {
            fallAttr.removeModifier(LivingBarkItem.SAFE_FALL_MODIFIER_ID)
            if (!fallAttr.hasModifier(SAFE_FALL_MODIFIER_ID)) {
                fallAttr.addTransientModifier(
                    AttributeModifier(
                        SAFE_FALL_MODIFIER_ID,
                        4.0,
                        AttributeModifier.Operation.ADD_VALUE
                    )
                )
            }
        }
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltipComponents: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        tooltipComponents.add(Component.empty())
        tooltipComponents.add(
            Component.translatable("tooltip.divinesouls.when_in_inventory")
                .withStyle(ChatFormatting.GRAY)
        )
        tooltipComponents.add(
            Component.translatable("tooltip.divinesouls.mycelial_bark_1")
                .withStyle(ChatFormatting.LIGHT_PURPLE)
        )
        tooltipComponents.add(
            Component.translatable("tooltip.divinesouls.mycelial_bark_2")
                .withStyle(ChatFormatting.DARK_PURPLE)
        )
        tooltipComponents.add(
            Component.translatable("tooltip.divinesouls.mycelial_bark_3")
                .withStyle(ChatFormatting.AQUA)
        )
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
    }

    @EventBusSubscriber(modid = DivineSoulsMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = [Dist.CLIENT])
    object ClientSideDashHandler {
        private var wasLeftDown = false
        private var lastLeftTapTime = 0L
        private var wasRightDown = false
        private var lastRightTapTime = 0L
        private var sideDashCooldownTicks = 0

        @SubscribeEvent
        fun onClientPlayerTick(event: PlayerTickEvent.Post) {
            val player = event.entity
            if (!player.level().isClientSide) return

            if (sideDashCooldownTicks > 0) {
                sideDashCooldownTicks--
            }

            val options = Minecraft.getInstance().options
            val hasBark = player.inventory.contains { it.`is`(ModItems.MYCELIAL_BARK.get()) }

            if (!hasBark) {
                wasLeftDown = options.keyLeft.isDown
                wasRightDown = options.keyRight.isDown
                return
            }

            val isLeftDown = options.keyLeft.isDown
            if (isLeftDown && !wasLeftDown) {
                val now = System.currentTimeMillis()
                if (now - lastLeftTapTime < 300L && sideDashCooldownTicks == 0) {
                    performSideDash(player, isRight = false)
                    sideDashCooldownTicks = 30
                    lastLeftTapTime = 0L
                } else {
                    lastLeftTapTime = now
                }
            }
            wasLeftDown = isLeftDown

            val isRightDown = options.keyRight.isDown
            if (isRightDown && !wasRightDown) {
                val now = System.currentTimeMillis()
                if (now - lastRightTapTime < 300L && sideDashCooldownTicks == 0) {
                    performSideDash(player, isRight = true)
                    sideDashCooldownTicks = 30
                    lastRightTapTime = 0L
                } else {
                    lastRightTapTime = now
                }
            }
            wasRightDown = isRightDown
        }

        private fun performSideDash(player: Player, isRight: Boolean) {
            val look = player.lookAngle
            val horizForward = Vec3(look.x, 0.0, look.z)
            val normForward = if (horizForward.lengthSqr() > 0.0001) horizForward.normalize() else Vec3(0.0, 0.0, 1.0)

            val sideVec = if (isRight) {
                Vec3(-normForward.z, 0.0, normForward.x)
            } else {
                Vec3(normForward.z, 0.0, -normForward.x)
            }

            val verticalBoost = if (player.onGround()) 0.14 else 0.06
            player.deltaMovement = Vec3(sideVec.x * 1.05, verticalBoost, sideVec.z * 1.05)

            val level = player.level()
            for (i in 0 until 10) {
                level.addParticle(
                    ParticleTypes.WARPED_SPORE,
                    player.x + (level.random.nextDouble() - 0.5) * 0.6,
                    player.y + 0.2 + level.random.nextDouble() * 1.0,
                    player.z + (level.random.nextDouble() - 0.5) * 0.6,
                    -sideVec.x * 0.15, 0.02, -sideVec.z * 0.15
                )
            }

            level.playLocalSound(
                player.x, player.y, player.z,
                SoundEvents.BREEZE_JUMP, SoundSource.PLAYERS,
                0.75f, 1.45f, false
            )
        }
    }
}
