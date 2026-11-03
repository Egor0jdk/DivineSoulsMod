package ru.rlyqaz.divinesouls.item

import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.ArmorMaterial
import net.minecraft.world.item.crafting.Ingredient
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister
import ru.rlyqaz.divinesouls.DivineSoulsMod
import java.util.EnumMap
import java.util.function.Supplier

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.21.1 NeoForge)
 * КЛАСС: ModArmorMaterials (Реестр кастомных материалов брони)
 * ====================================================================
 */
object ModArmorMaterials {
    val REGISTRY: DeferredRegister<ArmorMaterial> = DeferredRegister.create(Registries.ARMOR_MATERIAL, DivineSoulsMod.MOD_ID)

    // Твой хардкорный кристальный сет Края
    val END_CRYSTAL: Holder<ArmorMaterial> = REGISTRY.register("end_crystal", Supplier<ArmorMaterial> {
        // Создаем мапы прочности и защиты вручную без абстрактных хелперов, чтобы Котлин не терял типы
        val durabilityMap = EnumMap<ArmorItem.Type, Int>(ArmorItem.Type::class.java).apply {
            put(ArmorItem.Type.BOOTS, 13 * 45)
            put(ArmorItem.Type.LEGGINGS, 15 * 45)
            put(ArmorItem.Type.CHESTPLATE, 16 * 45)
            put(ArmorItem.Type.HELMET, 11 * 45)
        }

        val defenseMap = EnumMap<ArmorItem.Type, Int>(ArmorItem.Type::class.java).apply {
            put(ArmorItem.Type.BOOTS, 4)
            put(ArmorItem.Type.LEGGINGS, 7)
            put(ArmorItem.Type.CHESTPLATE, 9)
            put(ArmorItem.Type.HELMET, 4)
        }

        // Слои текстуры брони
        val layers = listOf(
            ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "end_crystal"))
        )

        // Строгий вызов конструктора 1.21.1: defense, enchantmentValue, equipSound, repairIngredient, layers, toughness, knockbackResistance
        ArmorMaterial(
            defenseMap,
            22,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            Supplier<Ingredient> { Ingredient.of(ModItems.END_CRYSTAL_GEM.get()) },
            layers,
            4.0f,
            0.2f // Твои 20% антиотбрасывания по канону
        )
    })

    fun register(eventBus: IEventBus) {
        REGISTRY.register(eventBus)
    }
}
