package ru.rlyqaz.divinesouls.item

import net.minecraft.world.item.Item
import net.minecraft.world.item.Rarity
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.item.custom.AncientRuneItem
import ru.rlyqaz.divinesouls.item.custom.WitherCoreItem
import java.util.function.Supplier

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.21.1 NeoForge)
 * КЛАСС: ModItems (Ресурсы и Активаторы прогресса)
 * ====================================================================
 */
object ModItems {
    private val REGISTRY: DeferredRegister.Items = DivineSoulsMod.ITEMS

    // --- АКТИВАТОРЫ ПРОГРЕССИИ (ЭРЫ СЮЖЕТА) ---
    // Древняя руна: Открывает проход в Незер при клике по Обсидиану (AncientRuneItem)
    val ANCIENT_RUNE: Supplier<Item> = registerItem("ancient_rune") {
        AncientRuneItem(Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON))
    }

    // Ядро Визера: Вручную зажигает портал Энда 3х3 после проверки всех 12 глаз (WitherCoreItem)
    val WITHER_CORE: Supplier<Item> = registerItem("wither_core") {
        WitherCoreItem(Item.Properties().stacksTo(1).rarity(Rarity.EPIC))
    }

    // --- СЫРЫЕ МАТЕРИАЛЫ И КРАФТ-КОМПОНЕНТЫ ---
    // Кристальный гем Энда: Главный материал для крафта тира брони и инструментов End Crystal
    val END_CRYSTAL_GEM: Supplier<Item> = registerItem("end_crystal_gem") {
        Item(Item.Properties())
    }

    // Палка из дерева Края: Основа для кастомных инструментов Ксен-биомов
    val END_WOOD_STICK: Supplier<Item> = registerItem("end_wood_stick") {
        Item(Item.Properties())
    }

    // Душа Вардена (RPG Материал)
    val WARDEN_SOUL: Supplier<Item> = registerItem("warden_soul") {
        Item(Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC))
    }

    // Древнее Око (Дроп с Древнего Стража)
    val ANCIENT_EYE: Supplier<Item> = registerItem("ancient_eye") {
        Item(Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE))
    }

    // Осколок Древнего Ока (Материал крафта руны)
    val ANCIENT_EYE_SHARD: Supplier<Item> = registerItem("ancient_eye_shard") {
        Item(Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE))
    }

    // Вспомогательная функция регистрации
    private fun <T : Item> registerItem(name: String, itemSupplier: () -> T): Supplier<T> {
        return REGISTRY.register(name, itemSupplier)
    }

    // Эссенция Нижнего Мира (Промежуточный материал)
    val NETHER_ESSENCE: Supplier<Item> = registerItem("nether_essence") {
        Item(Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE))
    }

    fun register(eventBus: IEventBus) {
        // Оставляем пустым, вызов просто триггерит загрузку статических полей
    }
}
