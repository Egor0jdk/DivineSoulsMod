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
            maxBonusHealthCap = 20.0f,
            requiredPreviousBonus = 10.0f,
            isEdibleFruit = true
        )
    }

    val DIVINE_HEART: Supplier<Item> = registerItem("divine_heart") {
        ru.rlyqaz.divinesouls.item.custom.HealthUpgradeItem(
            Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.EPIC),
            healthGain = 4.0f,
            maxBonusHealthCap = 40.0f,
            requiredPreviousBonus = 20.0f
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
            maxManaCap = 200,
            requiredPreviousMana = 150
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

    val VOID_DRAGON_SPAWN_EGG: Supplier<Item> = registerItem("void_dragon_spawn_egg") {
        net.neoforged.neoforge.common.DeferredSpawnEggItem(
            ModEntities.VOID_DRAGON,
            0x140E1C,
            0x9B30FF,
            Item.Properties()
        )
    }

    // Сенсорный клык (дроп со Скалкового ползуна -> центр Резонирующего ядра)
    val SENSOR_FANG: Supplier<Item> = registerItem("sensor_fang") {
        Item(Item.Properties().rarity(Rarity.UNCOMMON))
    }

    // Скалковый хитин (дроп со Скалкового охотника -> углы Резонирующего ядра)
    val SCULK_CHITIN: Supplier<Item> = registerItem("sculk_chitin") {
        Item(Item.Properties().rarity(Rarity.UNCOMMON))
    }

    // Резонирующий узел (угловой компонент активатора портала: Сенсорный клык + Эхо-осколки + Скалковый хитин)
    val RESONANT_NODE: Supplier<Item> = registerItem("resonant_node") {
        Item(Item.Properties().rarity(Rarity.RARE))
    }

    // Сердце Бездны (финальный активатор портала Древнего города)
    val ABYSSAL_HEART: Supplier<Item> = registerItem("abyssal_heart") {
        Item(Item.Properties().stacksTo(1).rarity(Rarity.EPIC))
    }

    val SCULK_CRAWLER_SPAWN_EGG: Supplier<Item> = registerItem("sculk_crawler_spawn_egg") {
        net.neoforged.neoforge.common.DeferredSpawnEggItem(
            ModEntities.SCULK_CRAWLER,
            0x0B1E26,
            0x29DFEB,
            Item.Properties()
        )
    }

    val SCULK_STALKER_SPAWN_EGG: Supplier<Item> = registerItem("sculk_stalker_spawn_egg") {
        net.neoforged.neoforge.common.DeferredSpawnEggItem(
            ModEntities.SCULK_STALKER,
            0x091318,
            0x18B5C4,
            Item.Properties()
        )
    }

    val UNICORN_HORN: Supplier<Item> = registerItem("unicorn_horn") {
        Item(Item.Properties().rarity(Rarity.RARE))
    }

    val UNICORN_SPAWN_EGG: Supplier<Item> = registerItem("unicorn_spawn_egg") {
        net.neoforged.neoforge.common.DeferredSpawnEggItem(
            ModEntities.UNICORN,
            0xF8FAFC,
            0x93C5FD,
            Item.Properties()
        )
    }

    val ASTRAL_WANDERER_SPAWN_EGG: Supplier<Item> = registerItem("astral_wanderer_spawn_egg") {
        net.neoforged.neoforge.common.DeferredSpawnEggItem(
            ModEntities.ASTRAL_WANDERER,
            0x38BDF8,
            0xFDE047,
            Item.Properties()
        )
    }

    val LAVA_GOLEM_SPAWN_EGG: Supplier<Item> = registerItem("lava_golem_spawn_egg") {
        net.neoforged.neoforge.common.DeferredSpawnEggItem(
            ModEntities.LAVA_GOLEM,
            0x27272A,
            0xF97316,
            Item.Properties()
        )
    }

    val PURPUR_GUARDIAN_SPAWN_EGG: Supplier<Item> = registerItem("purpur_guardian_spawn_egg") {
        net.neoforged.neoforge.common.DeferredSpawnEggItem(
            ModEntities.PURPUR_GUARDIAN,
            0xA855F7,
            0xE879F9,
            Item.Properties()
        )
    }

    // Астральная пыль (дроп с Астрального скитальца -> крафт Кристалла маны)
    val ASTRAL_DUST: Supplier<Item> = registerItem("astral_dust") {
        Item(Item.Properties().rarity(Rarity.UNCOMMON))
    }

    // Осколок кристалла жизни (добывается из красных кристаллов на островах Энда -> крафт Кристалла жизни)
    val LIFE_CRYSTAL_SHARD: Supplier<Item> = registerItem("life_crystal_shard") {
        Item(Item.Properties().rarity(Rarity.UNCOMMON))
    }

    // Яйцо призыва ванильного Иллюзора (добавляется в ванильную вкладку Яиц призыва)
    val ILLUSIONER_SPAWN_EGG: Supplier<Item> = registerItem("illusioner_spawn_egg") {
        net.neoforged.neoforge.common.DeferredSpawnEggItem(
            { net.minecraft.world.entity.EntityType.ILLUSIONER },
            0x1E3A8A,
            0x94A3B8,
            Item.Properties()
        )
    }

    // Кузнечный шаблон улучшения незеритовой брони в броню Самоцвета Энда
    val END_CRYSTAL_UPGRADE_SMITHING_TEMPLATE: Supplier<Item> = registerItem("end_crystal_upgrade_smithing_template") {
        net.minecraft.world.item.SmithingTemplateItem(
            net.minecraft.network.chat.Component.translatable("item.divinesouls.smithing_template.end_crystal_upgrade.applies_to")
                .withStyle(net.minecraft.ChatFormatting.BLUE),
            net.minecraft.network.chat.Component.translatable("item.divinesouls.smithing_template.end_crystal_upgrade.ingredients")
                .withStyle(net.minecraft.ChatFormatting.BLUE),
            net.minecraft.network.chat.Component.translatable("upgrade.divinesouls.end_crystal_upgrade")
                .withStyle(net.minecraft.ChatFormatting.GRAY),
            net.minecraft.network.chat.Component.translatable("item.divinesouls.smithing_template.end_crystal_upgrade.base_slot_description"),
            net.minecraft.network.chat.Component.translatable("item.divinesouls.smithing_template.end_crystal_upgrade.additions_slot_description"),
            listOf(
                net.minecraft.resources.ResourceLocation.withDefaultNamespace("item/empty_armor_slot_helmet"),
                net.minecraft.resources.ResourceLocation.withDefaultNamespace("item/empty_armor_slot_chestplate"),
                net.minecraft.resources.ResourceLocation.withDefaultNamespace("item/empty_armor_slot_leggings"),
                net.minecraft.resources.ResourceLocation.withDefaultNamespace("item/empty_armor_slot_boots")
            ),
            listOf(
                net.minecraft.resources.ResourceLocation.withDefaultNamespace("item/empty_slot_diamond")
            )
        )
    }

    // 1. Базовый корпус искателя (как в Calamity) — остаётся до 64
    val LOCATOR_CORE: Supplier<Item> = registerItem("locator_core") {
        Item(Item.Properties().rarity(Rarity.COMMON))
    }

    // 2. Искатель Древнего города (Скалк вокруг ядра) — стак до 16
    val ANCIENT_CITY_LOCATOR: Supplier<Item> = registerItem("ancient_city_locator") {
        ru.rlyqaz.divinesouls.item.custom.SingleUseLocatorItem(
            properties = Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON),
            tooltipKey = "tooltip.divinesouls.ancient_city_locator",
            structureTag = net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.STRUCTURE,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "ancient_city_locator")
            )
        )
    }

    // 3. Искатель Болота (Эссенция Незера + грязь вокруг ядра) — стак до 16
    val SWAMP_LOCATOR: Supplier<Item> = registerItem("swamp_locator") {
        ru.rlyqaz.divinesouls.item.custom.SingleUseLocatorItem(
            properties = Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON),
            tooltipKey = "tooltip.divinesouls.swamp_locator",
            biomeTag = net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.BIOME,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "swamp_locator")
            )
        )
    }

    // 4. Искатель Города Энда (8 Самоцветов Энда вокруг ядра) — стак до 16
    val END_CITY_LOCATOR: Supplier<Item> = registerItem("end_city_locator") {
        ru.rlyqaz.divinesouls.item.custom.SingleUseLocatorItem(
            properties = Item.Properties().stacksTo(16).rarity(Rarity.RARE),
            tooltipKey = "tooltip.divinesouls.end_city_locator",
            structureTag = net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.STRUCTURE,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "end_city_locator")
            )
        )
    }

    // 5. Искатель камер испытаний (Светокамень крестом + Медные блоки по углам вокруг ядра -> после входа в Незер)
    val TRIAL_CHAMBERS_LOCATOR: Supplier<Item> = registerItem("trial_chambers_locator") {
        ru.rlyqaz.divinesouls.item.custom.SingleUseLocatorItem(
            properties = Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON),
            tooltipKey = "tooltip.divinesouls.trial_chambers_locator",
            structureTag = net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.STRUCTURE,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "trial_chambers_locator")
            )
        )
    }

    // 6. Искатель адской крепости (Почва душ крестом + Адские кирпичи по углам вокруг ядра)
    val FORTRESS_LOCATOR: Supplier<Item> = registerItem("fortress_locator") {
        ru.rlyqaz.divinesouls.item.custom.SingleUseLocatorItem(
            properties = Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON),
            tooltipKey = "tooltip.divinesouls.fortress_locator",
            structureTag = net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.STRUCTURE,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "fortress_locator")
            )
        )
    }

    // 7. Искатель бастиона (8 незеритовых ломов вокруг ядра)
    val BASTION_LOCATOR: Supplier<Item> = registerItem("bastion_locator") {
        ru.rlyqaz.divinesouls.item.custom.SingleUseLocatorItem(
            properties = Item.Properties().stacksTo(16).rarity(Rarity.RARE),
            tooltipKey = "tooltip.divinesouls.bastion_locator",
            structureTag = net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.STRUCTURE,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "bastion_locator")
            )
        )
    }

    // Рудный абиссалит (добывается из Руды Абиссалита, переплавляется в Слиток Абиссалита)
    val RAW_ABYSSAL_ORE: Supplier<Item> = registerItem("raw_abyssal_ore") {
        Item(Item.Properties().rarity(Rarity.UNCOMMON))
    }

    // Слиток Абиссалита (переплавка Рудного абиссалита -> Блок Абиссалита)
    val ABYSSAL_INGOT: Supplier<Item> = registerItem("abyssal_ingot") {
        Item(Item.Properties().rarity(Rarity.RARE))
    }

    // Эссенция Бездны (дроп с Тени Бездны -> крафт Механического ядра Иссушения)
    val ABYSS_ESSENCE: Supplier<Item> = registerItem("abyss_essence") {
        Item(Item.Properties().rarity(Rarity.RARE))
    }

    // Яйцо призыва Тени Бездны
    val ABYSSAL_SHADOW_SPAWN_EGG: Supplier<Item> = registerItem("abyssal_shadow_spawn_egg") {
        net.neoforged.neoforge.common.DeferredSpawnEggItem(
            ModEntities.ABYSSAL_SHADOW,
            0x0B132B,
            0x3A86FF,
            Item.Properties()
        )
    }

    // Спора Бездны (дроп со Споровика Бездны -> крафт апгрейда Живой коры)
    val ABYSSAL_SPORE: Supplier<Item> = registerItem("abyssal_spore") {
        Item(Item.Properties().rarity(Rarity.UNCOMMON))
    }

    // Яйцо призыва Споровика Бездны
    val ABYSSAL_SPORELING_SPAWN_EGG: Supplier<Item> = registerItem("abyssal_sporeling_spawn_egg") {
        net.neoforged.neoforge.common.DeferredSpawnEggItem(
            ModEntities.ABYSSAL_SPORELING,
            0x4A1D6E,
            0xE040FB,
            Item.Properties()
        )
    }

    // Живой мицелий (дроп с Грибного скитальца -> крафт апгрейда Живой коры)
    val LIVING_MYCELIUM: Supplier<Item> = registerItem("living_mycelium") {
        Item(Item.Properties().rarity(Rarity.UNCOMMON))
    }

    // Яйцо призыва Грибного скитальца
    val ABYSSAL_WALKER_SPAWN_EGG: Supplier<Item> = registerItem("abyssal_walker_spawn_egg") {
        net.neoforged.neoforge.common.DeferredSpawnEggItem(
            ModEntities.ABYSSAL_WALKER,
            0x2B193D,
            0x9C27B0,
            Item.Properties()
        )
    }

    val MYCELIAL_BARK: Supplier<Item> = registerItem("mycelial_bark") {
        ru.rlyqaz.divinesouls.item.custom.MycelialBarkItem(
            Item.Properties().stacksTo(1).rarity(Rarity.RARE)
        )
    }

    // Мембрана Бездны (будущий дроп с Парящего ската Бездны)
    val ABYSSAL_MEMBRANE: Supplier<Item> = registerItem("abyssal_membrane") {
        Item(Item.Properties().rarity(Rarity.UNCOMMON))
    }

    fun register(eventBus: IEventBus) {
        // Оставляем пустым, вызов просто триггерит загрузку статических полей
    }
}
