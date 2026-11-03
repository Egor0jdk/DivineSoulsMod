package ru.rlyqaz.divinesouls.item.custom

import net.minecraft.core.Holder
import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.ArmorMaterial

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.21.1 NeoForge)
 * КЛАСС: EndCrystalArmorItem (Кристальная броня Энда)
 * ЛОГИКА: Наследуется от ArmorItem, эффекты вынесены в ArmorEffectsHandler
 * ====================================================================
 */
class EndCrystalArmorItem(
    material: Holder<ArmorMaterial>,
    type: Type,
    properties: Properties
) : ArmorItem(material, type, properties)
