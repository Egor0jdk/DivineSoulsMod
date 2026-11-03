package ru.rlyqaz.divinesouls.item

import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.Item
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.item.custom.EndCrystalArmorItem
import java.util.function.Supplier

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.21.1 NeoForge)
 * КЛАСС: ModArmor (Комплект Кристальной Брони Энда)
 * ====================================================================
 */
object ModArmor {
    private val REGISTRY: DeferredRegister.Items = DivineSoulsMod.ITEMS

    // Шлем
    val END_CRYSTAL_HELMET: Supplier<Item> = registerArmor("end_crystal_helmet") {
        EndCrystalArmorItem(ModArmorMaterials.END_CRYSTAL, ArmorItem.Type.HELMET, Item.Properties())
    }

    // Нагрудник
    val END_CRYSTAL_CHESTPLATE: Supplier<Item> = registerArmor("end_crystal_chestplate") {
        EndCrystalArmorItem(ModArmorMaterials.END_CRYSTAL, ArmorItem.Type.CHESTPLATE, Item.Properties())
    }

    // Поножи
    val END_CRYSTAL_LEGGINGS: Supplier<Item> = registerArmor("end_crystal_leggings") {
        EndCrystalArmorItem(ModArmorMaterials.END_CRYSTAL, ArmorItem.Type.LEGGINGS, Item.Properties())
    }

    // Ботинки (Зафиксировал имя поля под ID предмета)
    val END_CRYSTAL_BOOTS: Supplier<Item> = registerArmor("end_crystal_boots") {
        EndCrystalArmorItem(ModArmorMaterials.END_CRYSTAL, ArmorItem.Type.BOOTS, Item.Properties())
    }

    private fun <T : Item> registerArmor(name: String, itemSupplier: () -> T): Supplier<T> {
        return REGISTRY.register(name, itemSupplier)
    }

    fun register(eventBus: IEventBus) {
        // Оставляем пустым для триггера статических полей
    }
}
