package ru.rlyqaz.divinesouls;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

public class EndCrystalArmorItem extends ArmorItem {

    public EndCrystalArmorItem(IArmorMaterial material, EquipmentSlotType slot, Properties properties) {
        super(material, slot, properties);
    }

    @Override
    public void onArmorTick(ItemStack stack, World world, PlayerEntity player) {
        // Проверяем только на сервере и только для шлема
        if (!world.isClientSide() && this.slot == EquipmentSlotType.HEAD) {

            if (isFullSetEquipped(player)) {
                // 1. Пассивный бонус защиты: только Сопротивление урону I (никакого плавного падения!)
                player.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, 240, 0, false, false));

                // 2. ПОЛНЫЙ ИММУНИТЕТ К ЛЕВИТАЦИИ: намертво срезаем эффект, если он появился
                if (player.hasEffect(Effects.LEVITATION)) {
                    player.removeEffect(Effects.LEVITATION);
                }
            }
        }
    }

    private boolean isFullSetEquipped(PlayerEntity player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlotType.HEAD);
        ItemStack chestplate = player.getItemBySlot(EquipmentSlotType.CHEST);
        ItemStack leggings = player.getItemBySlot(EquipmentSlotType.LEGS);
        ItemStack boots = player.getItemBySlot(EquipmentSlotType.FEET);

        if (helmet.isEmpty() || chestplate.isEmpty() || leggings.isEmpty() || boots.isEmpty()) {
            return false;
        }

        return (helmet.getItem() instanceof ArmorItem && ((ArmorItem) helmet.getItem()).getMaterial() == ModArmorMaterial.END_CRYSTAL) &&
                (chestplate.getItem() instanceof ArmorItem && ((ArmorItem) chestplate.getItem()).getMaterial() == ModArmorMaterial.END_CRYSTAL) &&
                (leggings.getItem() instanceof ArmorItem && ((ArmorItem) leggings.getItem()).getMaterial() == ModArmorMaterial.END_CRYSTAL) &&
                (boots.getItem() instanceof ArmorItem && ((ArmorItem) boots.getItem()).getMaterial() == ModArmorMaterial.END_CRYSTAL);
    }
}
