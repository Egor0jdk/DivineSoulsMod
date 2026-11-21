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

    val MEADOW_SLIME_SPAWN_EGG: Supplier<Item> = registerItem("meadow_slime_spawn_egg") {
        net.neoforged.neoforge.common.DeferredSpawnEggItem(
            ru.rlyqaz.divinesouls.entity.ModEntities.MEADOW_SLIME,
            0xD9A426, // Основной цвет (насыщенный янтарно-жёлтый)
            0xFFE66D, // Цвет пятен (светлый медово-жёлтый)
            Item.Properties()
        )
    }

    val SANDY_CRAB_SPAWN_EGG: Supplier<Item> = registerItem("sandy_crab_spawn_egg") {
        net.neoforged.neoforge.common.DeferredSpawnEggItem(
            ru.rlyqaz.divinesouls.entity.ModEntities.SANDY_CRAB,
            0xD8B47C, // Основной цвет яйца (песчаный панцирь)
            0x8C583A, // Цвет крапинок (тёмно-охристые клешни)
            Item.Properties()
        )
    }

    val RAW_CRAB_MEAT: Supplier<Item> = registerItem("raw_crab_meat") {
        Item(
            Item.Properties().food(
                net.minecraft.world.food.FoodProperties.Builder()
                    .nutrition(2)
                    .saturationModifier(0.2f)
                    .build()
            )
        )
    }

    val COOKED_CRAB_MEAT: Supplier<Item> = registerItem("cooked_crab_meat") {
        Item(
            Item.Properties().food(
                net.minecraft.world.food.FoodProperties.Builder()
                    .nutrition(5)
                    .saturationModifier(0.6f)
                    .build()
            )
        )
    }

    val CAVE_GOBLIN_SPAWN_EGG: Supplier<Item> = registerItem("cave_goblin_spawn_egg") {
        net.neoforged.neoforge.common.DeferredSpawnEggItem(
            ru.rlyqaz.divinesouls.entity.ModEntities.CAVE_GOBLIN,
            0x4A6B3E,
            0x2D332B,
            Item.Properties()
        )
    }

    // Эктоплазма — хардмодный трофей с Болотного духа для активации портала Древнего города
    val ECTOPLASM: Supplier<Item> = registerItem("ectoplasm") {
        Item(Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE))
    }

    val SWAMP_SPIRIT_SPAWN_EGG: Supplier<Item> = registerItem("swamp_spirit_spawn_egg") {
        net.neoforged.neoforge.common.DeferredSpawnEggItem(
            ru.rlyqaz.divinesouls.entity.ModEntities.SWAMP_SPIRIT,
            0x27C4D6,
            0x0F3D4C,
            Item.Properties()
        )
    }

    val AWOKEN_WARDEN_SOUL: Supplier<Item> = registerItem("awoken_warden_soul") {
        Item(Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC))
    }

    // Жало осы — выпадает с Гигантской осы, используется для крафта отравленных стрел
    val WASP_STINGER: Supplier<Item> = registerItem("wasp_stinger") {
        Item(Item.Properties())
    }

    val GIANT_WASP_SPAWN_EGG: Supplier<Item> = registerItem("giant_wasp_spawn_egg") {
        net.neoforged.neoforge.common.DeferredSpawnEggItem(
            ru.rlyqaz.divinesouls.entity.ModEntities.GIANT_WASP,
            0xE3B41C,
            0x1F1B18,
            Item.Properties()
        )
    }

    // Ледяное ядро — выпадает с Ледяного элементаля, идёт в крафт Ледяного посоха
    val ICE_CORE: Supplier<Item> = registerItem("ice_core") {
        Item(Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON))
    }

    // Визуальный предмет ледяного осколка для рендера снаряда (можно не добавлять в креатив-вкладку)
    val ICE_SHARD_ITEM: Supplier<Item> = registerItem("ice_shard_item") {
        Item(Item.Properties())
    }

    val ICE_ELEMENTAL_SPAWN_EGG: Supplier<Item> = registerItem("ice_elemental_spawn_egg") {
        net.neoforged.neoforge.common.DeferredSpawnEggItem(
            ru.rlyqaz.divinesouls.entity.ModEntities.ICE_ELEMENTAL,
            0x7CD7F7,
            0x2B6CB0,
            Item.Properties()
        )
    }

    val JUNGLE_SPIDER_SPAWN_EGG: Supplier<Item> = registerItem("jungle_spider_spawn_egg") {
        net.neoforged.neoforge.common.DeferredSpawnEggItem(
            ru.rlyqaz.divinesouls.entity.ModEntities.JUNGLE_SPIDER,
            0x2D6A2E,
            0xB8E986,
            Item.Properties()
        )
    }

    val LIFE_CRYSTAL: Supplier<Item> = registerItem("life_crystal") {
        ru.rlyqaz.divinesouls.item.custom.HealthUpgradeItem(
            Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.RARE),
            healthGain = 2.0f,
            maxBonusHealthCap = 10.0f
        )
    }

    val LIFE_FRUIT: Supplier<Item> = registerItem("life_fruit") {
        ru.rlyqaz.divinesouls.item.custom.HealthUpgradeItem(
            Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.RARE),
            healthGain = 2.0f,
            maxBonusHealthCap = 20.0f
        )
    }

    val DIVINE_HEART: Supplier<Item> = registerItem("divine_heart") {
        ru.rlyqaz.divinesouls.item.custom.HealthUpgradeItem(
            Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.EPIC),
            healthGain = 4.0f,
            maxBonusHealthCap = 40.0f
        )
    }

    val MANA_CRYSTAL: Supplier<Item> = registerItem("mana_crystal") {
        ru.rlyqaz.divinesouls.item.custom.ManaUpgradeItem(
            Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.RARE),
            manaGain = 10,
            maxManaCap = 150
        )
    }

    val ASTRAL_STAR: Supplier<Item> = registerItem("astral_star") {
        ru.rlyqaz.divinesouls.item.custom.ManaUpgradeItem(
            Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.EPIC),
            manaGain = 10,
            maxManaCap = 200
        )
    }

    val LIVING_BARK: Supplier<Item> = registerItem("living_bark") {
        ru.rlyqaz.divinesouls.item.custom.LivingBarkItem(
            Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.UNCOMMON)
        )
    }

    val FOREST_ENT_SPAWN_EGG: Supplier<Item> = registerItem("forest_ent_spawn_egg") {
        net.neoforged.neoforge.common.DeferredSpawnEggItem(
            ru.rlyqaz.divinesouls.entity.ModEntities.FOREST_ENT,
            0x4A3219,
            0x3B7A28,
            Item.Properties()
        )
    }

    fun register(eventBus: IEventBus) {
        // Оставляем пустым, вызов просто триггерит загрузку статических полей
    }
}
