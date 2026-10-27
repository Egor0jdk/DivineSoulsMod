package ru.rlyqaz.divinesouls;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.16.5 Forge)
 * КЛАСС: ModArmor (Комплекты Защитной Брони)
 *
 * ИИ-КОНТЕКСТ ДЛЯ БУДУЩИХ МОДЕЛЕЙ:
 * - Этот файл управляет регистрацией элементов кастомных сетов брони.
 * - Кристальный сет Энда привязан к EndCrystalArmorItem для обработки тик-эффектов сета.
 * - Характеристики прочности и защиты берутся напрямую из перечисления ModArmorMaterial.
 * ====================================================================
 */
public class ModArmor {

    public static final RegistryObject<Item> END_CRYSTAL_HELMET = DivineSoulsMod.ITEMS.register("end_crystal_helmet",
            () -> new EndCrystalArmorItem(ModArmorMaterial.END_CRYSTAL, EquipmentSlotType.HEAD, new Item.Properties().tab(ModTabs.DIVINE_ARMOR)));

    public static final RegistryObject<Item> END_CRYSTAL_CHESTPLATE = DivineSoulsMod.ITEMS.register("end_crystal_chestplate",
            () -> new EndCrystalArmorItem(ModArmorMaterial.END_CRYSTAL, EquipmentSlotType.CHEST, new Item.Properties().tab(ModTabs.DIVINE_ARMOR)));

    public static final RegistryObject<Item> END_CRYSTAL_LEGGINGS = DivineSoulsMod.ITEMS.register("end_crystal_leggings",
            () -> new EndCrystalArmorItem(ModArmorMaterial.END_CRYSTAL, EquipmentSlotType.LEGS, new Item.Properties().tab(ModTabs.DIVINE_ARMOR)));

    public static final RegistryObject<Item> END_CRYSTAL_BOOTS = DivineSoulsMod.ITEMS.register("end_crystal_boots",
            () -> new ArmorItem(ModArmorMaterial.END_CRYSTAL, EquipmentSlotType.FEET, new Item.Properties().tab(ModTabs.DIVINE_ARMOR)));

    public static void register() {
        // Заглушка
    }
}
