package ru.rlyqaz.divinesouls.item.custom

import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ArmorItem
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.tick.PlayerTickEvent
import ru.rlyqaz.divinesouls.item.ModArmorMaterials

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.21.1 NeoForge)
 * КЛАСС: ArmorEffectsHandler (Обработчик пассивных эффектов RPG-сетов)
 * ЛОГИКА: Полная замена легаси-метода onArmorTick через PlayerTickEvent
 * ====================================================================
 */
object ArmorEffectsHandler {

    @SubscribeEvent
    fun onPlayerTick(event: PlayerTickEvent.Post) {
        val player: Player = event.entity
        val level = player.level()

        // Проверяем логику строго на серверной стороне раз в полсекунды (каждые 10 тиков) для оптимизации
        if (!level.isClientSide && player.tickCount % 10 == 0) {
            if (isFullEndCrystalSetEquipped(player)) {

                // 1. Твой оригинальный пассивный бонус защиты: Сопротивление урону I (Damage Resistance I)
                // Параметры: эффект, длительность (240 тиков), уровень (0 = I), эмбиент, видимость частиц
                player.addEffect(MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 0, false, false))

                // 2. ПОЛНЫЙ ИММУНИТЕТ К ЛЕВИТАЦИИ: если шалкер наложил эффект — намертво стираем его в ту же секунду
                if (player.hasEffect(MobEffects.LEVITATION)) {
                    player.removeEffect(MobEffects.LEVITATION)
                }
            }
        }
    }

    // Твой оригинальный алгоритм валидации материала брони на игроке
    private fun isFullEndCrystalSetEquipped(player: Player): Boolean {
        val helmet = player.getItemBySlot(EquipmentSlot.HEAD)
        val chestplate = player.getItemBySlot(EquipmentSlot.CHEST)
        val leggings = player.getItemBySlot(EquipmentSlot.LEGS)
        val boots = player.getItemBySlot(EquipmentSlot.FEET)

        if (helmet.isEmpty || chestplate.isEmpty || leggings.isEmpty || boots.isEmpty) {
            return false
        }

        val helmetItem = helmet.item
        val chestItem = chestplate.item
        val legsItem = leggings.item
        val bootsItem = boots.item

        // Проверяем, что все 4 вещи принадлежат нашему Кристальному Материалу Края
        return helmetItem is ArmorItem && helmetItem.material == ModArmorMaterials.END_CRYSTAL &&
                chestItem is ArmorItem && chestItem.material == ModArmorMaterials.END_CRYSTAL &&
                legsItem is ArmorItem && legsItem.material == ModArmorMaterials.END_CRYSTAL &&
                bootsItem is ArmorItem && bootsItem.material == ModArmorMaterials.END_CRYSTAL
    }
}
