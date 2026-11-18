package ru.rlyqaz.divinesouls.item.custom

import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import ru.rlyqaz.divinesouls.entity.custom.PoisonDaggerEntity

class PoisonThrowingDaggerItem(properties: Properties) : Item(properties) {

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)

        level.playSound(
            null,
            player.x,
            player.y,
            player.z,
            SoundEvents.SNOWBALL_THROW,
            SoundSource.NEUTRAL,
            0.6f,
            1.35f / (level.getRandom().nextFloat() * 0.3f + 0.85f)
        )

        // Небольшой кулдаун (8 тиков = 0.4 сек), чтобы метание чувствовалось динамичным, но без спама
        player.cooldowns.addCooldown(this, 8)

        if (!level.isClientSide) {
            val dagger = PoisonDaggerEntity(level, player)
            dagger.item = stack
            dagger.shootFromRotation(player, player.xRot, player.yRot, 0.0f, 1.25f, 1.0f)
            level.addFreshEntity(dagger)
        }

        player.awardStat(Stats.ITEM_USED.get(this))
        if (!player.abilities.instabuild) {
            stack.shrink(1)
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide())
    }
}
