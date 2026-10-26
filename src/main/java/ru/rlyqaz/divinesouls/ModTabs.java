package ru.rlyqaz.divinesouls;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ModTabs {

    // 1. Вкладка: Предметы (иконка — Древняя руна)
    public static final ItemGroup DIVINE_ITEMS = new ItemGroup("divine_items_tab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.ANCIENT_RUNE.get());
        }
    };

    // 2. Вкладка: Оружие (иконка — ванильный алмазный меч)
    public static final ItemGroup DIVINE_WEAPONS = new ItemGroup("divine_weapons_tab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Items.DIAMOND_SWORD);
        }
    };

    // 3. Вкладка: Мобы (иконка — ванильное яйцо призыва)
    public static final ItemGroup DIVINE_MOBS = new ItemGroup("divine_mobs_tab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Items.BAT_SPAWN_EGG);
        }
    };

    // 4. Вкладка: Блоки (иконка — обсидиан)
    public static final ItemGroup DIVINE_BLOCKS = new ItemGroup("divine_blocks_tab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Blocks.OBSIDIAN);
        }
    };

    // 5. Вкладка: Призывалки и активаторы (иконка — Ядро Визера)
    public static final ItemGroup DIVINE_SUMMONS = new ItemGroup("divine_summons_tab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.WITHER_CORE.get());
        }
    };
}
