package ru.rlyqaz.divinesouls.item

import net.minecraft.tags.BlockTags
import net.minecraft.world.item.crafting.Ingredient
import net.neoforged.neoforge.common.SimpleTier

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.21.1 NeoForge)
 * КЛАСС: ModItemTier (Материалы инструментов)
 * ЛОГИКА: Характеристики перенесены строго из оригинального Java-Enum
 * ====================================================================
 */
object ModItemTier {

    // Твой оригинальный Кристальный Тир Энда (Сильнее и быстрее Незерита!)
    val END_CRYSTAL: SimpleTier = SimpleTier(
        BlockTags.INCORRECT_FOR_NETHERITE_TOOL, // Уровень добычи 4 (копает абсолютно всё в игре)
        3000,        // Прочность (maxUses)
        12.0f,       // Скорость копания (efficiency) — у ванильного незерита всего 9.0!
        7.0f,        // Бонус урона материала (attackDamage)
        22,          // Зачаровываемость (enchantability)
        { Ingredient.of(ModItems.END_CRYSTAL_GEM.get()) } // Материал ремонта на наковальне
    )
}
