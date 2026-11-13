package ru.rlyqaz.divinesouls.item

import net.minecraft.world.item.Item
import net.minecraft.world.item.Rarity
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.item.custom.AncientRuneItem
import ru.rlyqaz.divinesouls.item.custom.WitherCoreItem
import ru.rlyqaz.divinesouls.entity.ModEntities
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

    // Огненный Лук из DivineRPG
    val FIRE_BOW: Supplier<Item> = registerItem("fire_bow") {
        ru.rlyqaz.divinesouls.item.custom.FireBowItem(Item.Properties().durability(584).rarity(net.minecraft.world.item.Rarity.RARE))
    }

    // Артефакт-батарейка (Ядро Адского Пламени)
    val INFERNAL_CORE: Supplier<Item> = registerItem("infernal_core") {
        Item(Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC))
    }

    // Осколок Души Дракона (из Яйца)
    val DRAGON_SOUL_SHARD: Supplier<Item> = registerItem("dragon_soul_shard") {
        Item(Item.Properties().stacksTo(4).rarity(net.minecraft.world.item.Rarity.EPIC))
    }

    val BURNING_SOUL: Supplier<Item> = registerItem("burning_soul") {
        Item(Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON))
    }

    // Яйцо спавна для Духа Скорби (Светло-серая основа савана, бирюзовые крапинки огня душ!)
    val SORROW_WRAITH_SPAWN_EGG: Supplier<Item> = registerItem("sorrow_wraith_spawn_egg") {
        net.neoforged.neoforge.common.DeferredSpawnEggItem(
            ModEntities.SORROW_WRAITH,
            0xD3D3D3,                  // Светло-серый цвет (Light Gray)
            0x00FFFF,                  // Ярко-бирюзовый/голубой (Cyan / Soul Fire)
            Item.Properties()
        )
    }

    // Душа полёта остаётся как была
    val SOUL_OF_FLIGHT: java.util.function.Supplier<Item> = registerItem("soul_of_flight") {
        Item(Item.Properties())
    }

    // БАГФИКС: Создаём крылья как кастомный объект ICurioItem, чтобы Curios мгновенно открыл слот на спине!
    val WINGS_OF_WIND: java.util.function.Supplier<Item> = registerItem("wings_of_wind") {
        object : Item(Item.Properties().stacksTo(1)), top.theillusivec4.curios.api.type.capability.ICurioItem {
            // Код Curios автоматически понимает, что этот предмет принадлежит слоту 'back'
        }
    }

    // Кристальный щит Энда (для тарана при рывке на Элитрах)
    val END_CRYSTAL_SHIELD: Supplier<Item> = registerItem("end_crystal_shield") {
        object : net.minecraft.world.item.ShieldItem(Item.Properties().durability(1008).rarity(Rarity.RARE)) {
            override fun isValidRepairItem(toRepair: net.minecraft.world.item.ItemStack, repair: net.minecraft.world.item.ItemStack): Boolean {
                return repair.`is`(END_CRYSTAL_GEM.get())
            }
        }
    }

    // Печать Иссушения (выпадает с Двуглавого Визер-скелета, нужна для крафта блока призыва)
    val WITHER_SEAL: Supplier<Item> = registerItem("wither_seal") {
        Item(Item.Properties().stacksTo(1).rarity(Rarity.RARE))
    }

    // Иссушённая кость (падает с обычных визер-скелетов с шансом 25%, нужна для Блока Печати)
    val WITHER_BONE: Supplier<Item> = registerItem("wither_bone") {
        Item(Item.Properties().rarity(Rarity.UNCOMMON))
    }

    // Яйцо призыва Близнецов Иссушения
    val TWIN_WITHER_SKELETON_SPAWN_EGG: Supplier<Item> = registerItem("twin_wither_skeleton_spawn_egg") {
        net.neoforged.neoforge.common.DeferredSpawnEggItem(
            ru.rlyqaz.divinesouls.entity.ModEntities.TWIN_WITHER_SKELETON,
            0x141414,
            0x5A1A1A,
            Item.Properties()
        )
    }

    fun register(eventBus: IEventBus) {
        // Оставляем пустым, вызов просто триггерит загрузку статических полей
    }
}
