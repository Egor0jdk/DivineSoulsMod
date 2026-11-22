package ru.rlyqaz.divinesouls.item.custom

import net.minecraft.core.particles.ParticleTypes
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.SwordItem
import net.minecraft.world.item.Tiers
import net.minecraft.world.item.UseAnim
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.item.ModItems

class LightSpearItem(properties: Properties) : SwordItem(
    Tiers.DIAMOND,
    properties.attributes(createSpearAttributes())
) {

    companion object {
        private val REACH_MODIFIER_ID: ResourceLocation = ResourceLocation.fromNamespaceAndPath(
            DivineSoulsMod.MOD_ID,
            "light_spear_reach"
        )

        fun createSpearAttributes(): ItemAttributeModifiers {
            return ItemAttributeModifiers.builder()
                .add(
                    Attributes.ATTACK_DAMAGE,
                    AttributeModifier(
                        BASE_ATTACK_DAMAGE_ID,
                        8.5,
                        AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND
                )
                .add(
                    Attributes.ATTACK_SPEED,
                    AttributeModifier(
                        BASE_ATTACK_SPEED_ID,
                        -2.6,
                        AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND
                )
                .add(
                    Attributes.ENTITY_INTERACTION_RANGE,
                    AttributeModifier(
                        REACH_MODIFIER_ID,
                        1.5,
                        AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND
                )
                .build()
        }
    }

    override fun getUseDuration(stack: ItemStack, entity: LivingEntity): Int = 72000

    override fun getUseAnimation(stack: ItemStack): UseAnim = UseAnim.NONE

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)

        // Включаем состояние выпада (копьё мгновенно выставляется остриём вперёд)
        player.startUsingItem(hand)
        player.cooldowns.addCooldown(this, 50)

        val look = player.lookAngle
        val dashBoost = Vec3(look.x * 0.95, if (player.onGround()) 0.18 else look.y * 0.4 + 0.1, look.z * 0.95)
        player.deltaMovement = dashBoost
        player.hurtMarked = true

        if (!level.isClientSide) {
            val serverLevel = level as ServerLevel
            val startPos = player.eyePosition.subtract(0.0, 0.3, 0.0)

            for (step in 1..9) {
                val dist = step * 0.5
                val p = startPos.add(look.scale(dist))
                serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    p.x, p.y, p.z,
                    2, 0.12, 0.12, 0.12, 0.01
                )
            }

            val lungeBox = player.boundingBox.expandTowards(look.scale(4.5)).inflate(0.75)
            val targets = serverLevel.getEntitiesOfClass(LivingEntity::class.java, lungeBox) { e ->
                e != player && e.isAlive
            }

            for (target in targets) {
                target.hurt(serverLevel.damageSources().playerAttack(player), 11.0f)
                target.knockback(0.55, -look.x, -look.z)
                target.addEffect(MobEffectInstance(MobEffects.GLOWING, 80, 0), player)
            }

            serverLevel.playSound(
                null, player.x, player.y, player.z,
                SoundEvents.TRIDENT_RIPTIDE_1.value(), SoundSource.PLAYERS,
                0.9f, 1.35f
            )

            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand))
        }

        player.awardStat(Stats.ITEM_USED.get(this))
        return InteractionResultHolder.consume(stack)
    }

    override fun onUseTick(level: Level, livingEntity: LivingEntity, stack: ItemStack, remainingUseDuration: Int) {
        val elapsed = getUseDuration(stack, livingEntity) - remainingUseDuration
        // Через 8 тиков (0.4 сек, конец рывка) мгновенно возвращаем копьё в обычное положение
        if (elapsed >= 16) {
            livingEntity.stopUsingItem()
        }
    }

    override fun isValidRepairItem(toRepair: ItemStack, repair: ItemStack): Boolean {
        return repair.`is`(ModItems.UNICORN_HORN.get())
    }
}
