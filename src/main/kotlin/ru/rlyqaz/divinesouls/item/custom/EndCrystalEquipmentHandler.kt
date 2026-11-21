package ru.rlyqaz.divinesouls.item.custom

import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ArmorItem
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent
import net.neoforged.neoforge.event.entity.living.MobEffectEvent
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent
import ru.rlyqaz.divinesouls.item.ModArmorMaterials
import ru.rlyqaz.divinesouls.item.ModWeapons

object EndCrystalEquipmentHandler {

    // 1. Срабатывает ТОЛЬКО в момент надевания или снятия любой части брони (0 нагрузки на тик)
    @SubscribeEvent
    fun onArmorChange(event: LivingEquipmentChangeEvent) {
        val player = event.entity as? Player ?: return
        if (player.level().isClientSide) return

        // Реагируем только на слоты брони
        if (!event.slot.isArmor) return

        if (isFullEndCrystalSetEquipped(player)) {
            // Выдаём бесконечное Сопротивление урону I (в инвентаре рисуется ∞, без мигания 00:11)
            val current = player.getEffect(MobEffects.DAMAGE_RESISTANCE)
            if (current == null || (!current.isInfiniteDuration && current.amplifier == 0)) {
                player.addEffect(
                    MobEffectInstance(
                        MobEffects.DAMAGE_RESISTANCE,
                        MobEffectInstance.INFINITE_DURATION,
                        0,
                        false,
                        false,
                        true
                    )
                )
            }
            // Если на игроке уже висела Левитация — сразу снимаем
            player.removeEffect(MobEffects.LEVITATION)
        } else {
            // Как только сняли хотя бы 1 элемент Кристальной брони — мгновенно снимаем бесконечный бонус сета
            val current = player.getEffect(MobEffects.DAMAGE_RESISTANCE)
            if (current != null && current.isInfiniteDuration && current.amplifier == 0) {
                player.removeEffect(MobEffects.DAMAGE_RESISTANCE)
            }
        }
    }

    // 2. Честный иммунитет к Левитации без проверки каждый тик (блокирует само наложение эффекта от шалкеров)
    @SubscribeEvent
    fun onLevitationApplicable(event: MobEffectEvent.Applicable) {
        val player = event.entity as? Player ?: return
        if (event.effectInstance?.effect == MobEffects.LEVITATION && isFullEndCrystalSetEquipped(player)) {
            event.result = MobEffectEvent.Applicable.Result.DO_NOT_APPLY
        }
    }

    // 3. Пассивный бонус Кристального меча Энда (30% шанс на +5 магического урона)
    // 3. Пассивный бонус Кристального меча Энда (зависит от заполненности шкалы атаки)
    @SubscribeEvent
    fun onCrystalWeaponAttack(event: AttackEntityEvent) {
        val player = event.entity
        val target = event.target
        val level = player.level()

        if (!level.isClientSide && target is LivingEntity) {
            if (player.mainHandItem.`is`(ModWeapons.END_CRYSTAL_SWORD.get())) {
                // Получаем заполненность шкалы атаки в момент удара (от 0.0f до 1.0f)
                val attackStrength = player.getAttackStrengthScale(0.5f)

                // Защита от закликивания: при слабых спам-ударах (< 50% шкалы) кристальный прок не срабатывает
                if (attackStrength < 0.5f) return

                // Шанс и урон масштабируются от заполненности шкалы (на 100% шкалы = 30% шанс и 5.0 урона)
                val procChance = 0.35f * attackStrength
                if (player.random.nextFloat() < procChance) {
                    val serverLevel = level as ServerLevel
                    val pos = target.blockPosition()
                    val bonusDamage = 5.5f * attackStrength

                    target.hurt(serverLevel.damageSources().magic(), bonusDamage)

                    serverLevel.playSound(
                        null, pos,
                        SoundEvents.AMETHYST_CLUSTER_BREAK,
                        SoundSource.PLAYERS, 1.5f, 1.2f
                    )

                    serverLevel.sendParticles(
                        ParticleTypes.WITCH,
                        target.x, target.getY(0.5), target.z,
                        25, 0.3, 0.3, 0.3, 0.1
                    )
                }
            }
        }
    }

    private fun isFullEndCrystalSetEquipped(player: Player): Boolean {
        val helmet = player.getItemBySlot(EquipmentSlot.HEAD).item
        val chestplate = player.getItemBySlot(EquipmentSlot.CHEST).item
        val leggings = player.getItemBySlot(EquipmentSlot.LEGS).item
        val boots = player.getItemBySlot(EquipmentSlot.FEET).item

        return helmet is ArmorItem && helmet.material == ModArmorMaterials.END_CRYSTAL &&
                chestplate is ArmorItem && chestplate.material == ModArmorMaterials.END_CRYSTAL &&
                leggings is ArmorItem && leggings.material == ModArmorMaterials.END_CRYSTAL &&
                boots is ArmorItem && boots.material == ModArmorMaterials.END_CRYSTAL
    }
}
