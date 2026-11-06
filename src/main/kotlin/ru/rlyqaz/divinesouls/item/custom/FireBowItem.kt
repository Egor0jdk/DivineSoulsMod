package ru.rlyqaz.divinesouls.item.custom

import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.AbstractArrow
import net.minecraft.world.entity.projectile.Arrow
import net.minecraft.world.item.BowItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import ru.rlyqaz.divinesouls.item.ModItems
import java.util.function.Predicate

class FireBowItem(properties: Properties) : BowItem(properties) {

    override fun getAllSupportedProjectiles(): Predicate<ItemStack> {
        return Predicate { stack -> stack.`is`(ModItems.INFERNAL_CORE.get()) }
    }

    override fun releaseUsing(stack: ItemStack, level: Level, entity: LivingEntity, timeCharged: Int) {
        if (entity is Player) {
            val hasAmmo = !entity.getProjectile(stack).isEmpty

            if (entity.isCreative || hasAmmo) {
                val chargeTicks = this.getUseDuration(stack, entity) - timeCharged
                val velocity = getPowerForTime(chargeTicks)

                if (velocity >= 0.1f) {
                    if (!level.isClientSide) {
                        val arrow = Arrow(level, entity, ItemStack(net.minecraft.world.item.Items.ARROW), null)

                        arrow.shootFromRotation(entity, entity.xRot, entity.yRot, 0.0f, velocity * 3.0f, 1.0f)

                        if (velocity == 1.0f) {
                            arrow.isCritArrow = true
                        }

                        // ТОЧЕЧНЫЙ ФИКС УРОНА: Поднимаем базовый урон стрелы до 4.5!
                        // Теперь при полном натяжении лук будет сносить около 20 единиц (10 сердец) за выстрел!
                        arrow.baseDamage = 4.5

                        arrow.remainingFireTicks = 2000
                        arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY

                        level.addFreshEntity(arrow)
                    }

                    // Оставляем сочный звук выстрела Ифрита для крутой RPG-атмосферы!
                    level.playSound(
                        null, entity.x, entity.y, entity.z,
                        SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS,
                        1.0f, 1.0f / (level.getRandom().nextFloat() * 0.4f + 1.2f) + velocity * 0.5f
                    )

                    if (!entity.isCreative) {
                        stack.hurtAndBreak(1, entity, LivingEntity.getSlotForHand(entity.usedItemHand))
                    }

                    entity.awardStat(Stats.ITEM_USED.get(this))
                }
            }
        }
    }

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)
        val hasAmmo = !player.getProjectile(stack).isEmpty

        if (!player.isCreative && !hasAmmo) {
            return InteractionResultHolder.fail(stack)
        } else {
            player.startUsingItem(hand)
            return InteractionResultHolder.consume(stack)
        }
    }
}
