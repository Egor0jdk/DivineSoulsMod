package ru.rlyqaz.divinesouls;

import net.minecraft.item.*;
import net.minecraftforge.fml.RegistryObject;

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.16.5 Forge)
 * КЛАСС: ModWeapons (Оружие, Боевые инструменты и Магия)
 *
 * ИИ-КОНТЕКСТ ДЛЯ БУДУЩИХ МОДЕЛЕЙ:
 * - Этот файл хранит всю боевую и утилитарную экипировку (мечи, инструменты, посохи).
 * - Использует характеристики материала из перечисления ModItemTier.
 * - Все кастомные магические снаряды и оружия дальнего боя Террария-стайла дописывать сюда!
 * ====================================================================
 */
public class ModWeapons {

    // --- КРИСТАЛЬНЫЙ ТИР ЭНДА (END CRYSTAL TIER) ---

    // Кристальный меч: 1 (рука) + 7 (базовый урон материала) + 3 (модификатор меча) = 11 Единиц Урона!
    public static final RegistryObject<Item> END_CRYSTAL_SWORD = DivineSoulsMod.ITEMS.register("end_crystal_sword",
            () -> new SwordItem(ModItemTier.END_CRYSTAL, 3, -2.4F, new Item.Properties().tab(ModTabs.DIVINE_WEAPONS)));

    // Кристальная кирка: Уровень добычи 4 (копает всё), прочность 3000, урон 9
    public static final RegistryObject<Item> END_CRYSTAL_PICKAXE = DivineSoulsMod.ITEMS.register("end_crystal_pickaxe",
            () -> new PickaxeItem(ModItemTier.END_CRYSTAL, 1, -2.8F, new Item.Properties().tab(ModTabs.DIVINE_WEAPONS)));

    // Кристальный топор: Медленный, но сокрушительный урон (13 единиц!). Идеально для рубки плотной древесины Края
    public static final RegistryObject<Item> END_CRYSTAL_AXE = DivineSoulsMod.ITEMS.register("end_crystal_axe",
            () -> new AxeItem(ModItemTier.END_CRYSTAL, 5, -3.0F, new Item.Properties().tab(ModTabs.DIVINE_WEAPONS)));

    // Кристальная лопата: Скорость атаки -3.0F, урон 9.5
    public static final RegistryObject<Item> END_CRYSTAL_SHOVEL = DivineSoulsMod.ITEMS.register("end_crystal_shovel",
            () -> new ShovelItem(ModItemTier.END_CRYSTAL, 1.5F, -3.0F, new Item.Properties().tab(ModTabs.DIVINE_WEAPONS)));

    // Кристальная мотыга: Декоративный боевой урон, высокая скорость вскапывания дёрна Ксена
    public static final RegistryObject<Item> END_CRYSTAL_HOE = DivineSoulsMod.ITEMS.register("end_crystal_hoe",
            () -> new HoeItem(ModItemTier.END_CRYSTAL, -3, 0.0F, new Item.Properties().tab(ModTabs.DIVINE_WEAPONS)));

    // Метод-заглушка для вызова инициализации класса в главном моде
    public static void register() {
        // Оставляем пустым
    }
}
