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

object ModArmorMaterials {
    val REGISTRY: DeferredRegister<ArmorMaterial> = DeferredRegister.create(Registries.ARMOR_MATERIAL, DivineSoulsMod.MOD_ID)

    val END_CRYSTAL: Holder<ArmorMaterial> = REGISTRY.register("end_crystal", Supplier<ArmorMaterial> {
        val defenseMap = EnumMap<ArmorItem.Type, Int>(ArmorItem.Type::class.java).apply {
            put(ArmorItem.Type.BOOTS, 3)
            put(ArmorItem.Type.LEGGINGS, 6)
            put(ArmorItem.Type.CHESTPLATE, 8)
            put(ArmorItem.Type.HELMET, 3)
        }

        val layers = listOf(
            ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "end_crystal"), "", true)
        )

        ArmorMaterial(
            defenseMap,
            22,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            Supplier<Ingredient> { Ingredient.of(ModItems.END_CRYSTAL_GEM.get()) },
            layers,
            4.0f,
            0.15f
        )
    })

    fun register(eventBus: IEventBus) {
        REGISTRY.register(eventBus)
    }
}
