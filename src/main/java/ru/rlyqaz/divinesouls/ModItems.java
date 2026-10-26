package ru.rlyqaz.divinesouls;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.16.5 Forge)
 * КЛАСС: ModItems (Ресурсы, Активаторы прогресса и Блочные предметы)
 *
 * ИИ-КОНТЕКСТ ДЛЯ БУДУЩИХ МОДЕЛЕЙ:
 * - Этот файл содержит только сырые материалы, предметы-активаторы и BlockItems.
 * - Оружие и инструменты перенесены в ModWeapons.
 * - Броня перенесена в ModArmor.
 * - Все предметы регистрируются в глобальную шину DivineSoulsMod.ITEMS.
 * ====================================================================
 */
public class ModItems {

    // --- АКТИВАТОРЫ ПРОГРЕССИИ (ЭРЫ СЮЖЕТА) ---
    // Древняя руна: Открывает проход в Незер при клике по Обсидиану (AncientRuneItem)
    public static final RegistryObject<Item> ANCIENT_RUNE = DivineSoulsMod.ITEMS.register("ancient_rune",
            () -> new AncientRuneItem(new Item.Properties().tab(ModTabs.DIVINE_SUMMONS).stacksTo(1)));

    // Ядро Визера: Вручную зажигает портал Энда 3х3 после проверки всех 12 глаз (WitherCoreItem)
    public static final RegistryObject<Item> WITHER_CORE = DivineSoulsMod.ITEMS.register("wither_core",
            () -> new WitherCoreItem(new Item.Properties().tab(ModTabs.DIVINE_SUMMONS).stacksTo(1)));

    // --- СЫРЫЕ МАТЕРИАЛЫ И КРАФТ-КОМПОНЕНТЫ ---
    // Кристальный гем Энда: Главный материал для крафта тира брони и инструментов End Crystal
    public static final RegistryObject<Item> END_CRYSTAL_GEM = DivineSoulsMod.ITEMS.register("end_crystal_gem",
            () -> new Item(new Item.Properties().tab(ModTabs.DIVINE_ITEMS)));

    // Палка из дерева Края: Основа для кастомных инструментов Ксен-биомов
    public static final RegistryObject<Item> END_WOOD_STICK = DivineSoulsMod.ITEMS.register("end_wood_stick",
            () -> new Item(new Item.Properties().tab(ModTabs.DIVINE_ITEMS)));

    // --- ТЕХНИЧЕСКИЕ ПРЕДМЕТЫ БЛОКОВ (BLOCK ITEMS) ---
    public static final RegistryObject<Item> END_CRYSTAL_ORE_ITEM = DivineSoulsMod.ITEMS.register("end_crystal_ore",
            () -> new BlockItem(ModBlocks.END_CRYSTAL_ORE.get(), new Item.Properties().tab(ModTabs.DIVINE_BLOCKS)));

    public static final RegistryObject<Item> XEN_GRASS_BLOCK_ITEM = DivineSoulsMod.ITEMS.register("xen_grass_block",
            () -> new BlockItem(ModBlocks.XEN_GRASS_BLOCK.get(), new Item.Properties().tab(ModTabs.DIVINE_BLOCKS)));

    public static final RegistryObject<Item> XEN_BUSH_GRASS_ITEM = DivineSoulsMod.ITEMS.register("xen_bush_grass",
            () -> new BlockItem(ModBlocks.XEN_BUSH_GRASS.get(), new Item.Properties().tab(ModTabs.DIVINE_BLOCKS)));

    public static final RegistryObject<Item> WITHERED_END_BUSH_ITEM = DivineSoulsMod.ITEMS.register("wither_end_bush",
            () -> new BlockItem(ModBlocks.WITHERED_END_BUSH.get(), new Item.Properties().tab(ModTabs.DIVINE_BLOCKS)));

    public static final RegistryObject<Item> END_WOOD_LOG_ITEM = DivineSoulsMod.ITEMS.register("end_wood_log",
            () -> new BlockItem(ModBlocks.END_WOOD_LOG.get(), new Item.Properties().tab(ModTabs.DIVINE_BLOCKS)));

    public static final RegistryObject<Item> END_WOOD_PLANKS_ITEM = DivineSoulsMod.ITEMS.register("end_wood_planks",
            () -> new BlockItem(ModBlocks.END_WOOD_PLANKS.get(), new Item.Properties().tab(ModTabs.DIVINE_BLOCKS)));

    public static final RegistryObject<Item> END_WOOD_LEAVES_ITEM = DivineSoulsMod.ITEMS.register("end_wood_leaves",
            () -> new BlockItem(ModBlocks.END_WOOD_LEAVES.get(), new Item.Properties().tab(ModTabs.DIVINE_BLOCKS)));

    public static final RegistryObject<Item> END_SAPLING_ITEM = DivineSoulsMod.ITEMS.register("end_sapling",
            () -> new BlockItem(ModBlocks.END_SAPLING.get(), new Item.Properties().tab(ModTabs.DIVINE_BLOCKS)));

    // Метод-заглушка для вызова инициализации класса в главном моде
    public static void register() {
        // Оставляем пустым, вызов просто триггерит загрузку статических полей
    }
}
