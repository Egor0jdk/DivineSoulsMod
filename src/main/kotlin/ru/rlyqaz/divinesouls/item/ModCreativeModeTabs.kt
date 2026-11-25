package ru.rlyqaz.divinesouls.item

import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.block.ModBlocks
import java.util.function.Supplier

object ModCreativeModeTabs {
    private val REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DivineSoulsMod.MOD_ID)

    val DIVINE_ITEMS: Supplier<CreativeModeTab> = REGISTRY.register("divine_items_tab", Supplier<CreativeModeTab> {
        CreativeModeTab.builder()
            .icon { ItemStack(ModItems.END_CRYSTAL_GEM.get()) }
            .title(Component.translatable("itemGroup.divinesouls.items"))
            .displayItems { _, output ->
                output.accept(ModItems.WASP_STINGER.get())
                output.accept(ModItems.ICE_CORE.get())
                output.accept(ModItems.LOCATOR_CORE.get())
                output.accept(ModItems.ANCIENT_CITY_LOCATOR.get())
                output.accept(ModItems.END_CITY_LOCATOR.get())
                output.accept(ModItems.SWAMP_LOCATOR.get())
                output.accept(ModItems.LIVING_BARK.get())
                output.accept(ModItems.WITHER_BONE.get())
                output.accept(ModItems.NETHER_ESSENCE.get())
                output.accept(ModItems.END_WOOD_STICK.get())
                output.accept(ModItems.END_CRYSTAL_GEM.get())
                output.accept(ModItems.END_CRYSTAL_UPGRADE_SMITHING_TEMPLATE.get())
                output.accept(ModItems.DRAGON_SOUL_SHARD.get())
                output.accept(ModItems.LIFE_CRYSTAL_SHARD.get())
                output.accept(ModItems.ECTOPLASM.get())
                output.accept(ModItems.UNICORN_HORN.get())
                output.accept(ModItems.ASTRAL_DUST.get())
                output.accept(ModItems.SENSOR_FANG.get())
                output.accept(ModItems.SCULK_CHITIN.get())
                output.accept(ModItems.RESONANT_NODE.get())
                output.accept(ModItems.ANCIENT_EYE.get())
                output.accept(ModItems.ANCIENT_EYE_SHARD.get())
                output.accept(ModItems.WARDEN_SOUL.get())
                output.accept(ModItems.SOUL_OF_FLIGHT.get())
                output.accept(ModItems.BURNING_SOUL.get())
                output.accept(ModItems.WITHER_SEAL.get())
                output.accept(ModItems.AWOKEN_WARDEN_SOUL.get())
            }
            .build()
    })

    val DIVINE_WEAPONS: Supplier<CreativeModeTab> = REGISTRY.register("divine_weapons_tab", Supplier<CreativeModeTab> {
        CreativeModeTab.builder()
            .icon { ItemStack(ModWeapons.END_CRYSTAL_SWORD.get()) }
            .title(Component.translatable("itemGroup.divinesouls.weapons"))
            .displayItems { _, output ->
                output.accept(ModWeapons.SPIKED_STONE_CLUB.get())
                output.accept(ModWeapons.WITHER_MACE.get())
                output.accept(ModWeapons.LIGHT_SPEAR.get())
                output.accept(ModWeapons.END_CRYSTAL_SWORD.get())
                output.accept(ModWeapons.END_CRYSTAL_PICKAXE.get())
                output.accept(ModWeapons.END_CRYSTAL_AXE.get())
                output.accept(ModWeapons.END_CRYSTAL_SHOVEL.get())
                output.accept(ModWeapons.END_CRYSTAL_HOE.get())
                output.accept(ModWeapons.END_CRYSTAL_SHIELD.get())
                output.accept(ModWeapons.FROST_STAFF.get())
                output.accept(ModWeapons.END_STAFF.get())
                output.accept(ModWeapons.POISON_THROWING_DAGGER.get())
                output.accept(ModItems.FIRE_BOW.get())
                output.accept(ModItems.INFERNAL_CORE.get())
                // Стрелы маны (I и II)
                output.accept(net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                    net.minecraft.world.item.Items.TIPPED_ARROW,
                    ru.rlyqaz.divinesouls.potion.ModPotions.MANA_POTION
                ))
                output.accept(net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                    net.minecraft.world.item.Items.TIPPED_ARROW,
                    ru.rlyqaz.divinesouls.potion.ModPotions.STRONG_MANA_POTION
                ))
            }
            .build()
    })

    val DIVINE_ARMOR: Supplier<CreativeModeTab> = REGISTRY.register("divine_armor_tab", Supplier<CreativeModeTab> {
        CreativeModeTab.builder()
            .icon { ItemStack(ModArmor.END_CRYSTAL_CHESTPLATE.get()) }
            .title(Component.translatable("itemGroup.divinesouls.armor"))
            .displayItems { _, output ->
                output.accept(ModArmor.END_CRYSTAL_HELMET.get())
                output.accept(ModArmor.END_CRYSTAL_CHESTPLATE.get())
                output.accept(ModArmor.END_CRYSTAL_LEGGINGS.get())
                output.accept(ModArmor.END_CRYSTAL_BOOTS.get())
                output.accept(ModItems.WINGS_OF_WIND.get())
            }
            .build()
    })

    val DIVINE_MOBS: Supplier<CreativeModeTab> = REGISTRY.register("divine_mobs_tab", Supplier<CreativeModeTab> {
        CreativeModeTab.builder()
            // Ставим наше новое каноничное серо-бирюзовое яйцо призыва на иконку всей вкладки!
            .icon { ItemStack(ModItems.SORROW_WRAITH_SPAWN_EGG.get()) }
            .title(Component.translatable("itemGroup.divinesouls.mobs"))
            .displayItems { _, output ->
                output.accept(ModItems.MEADOW_SLIME_SPAWN_EGG.get())
                output.accept(ModItems.SANDY_CRAB_SPAWN_EGG.get())
                output.accept(ModItems.CAVE_GOBLIN_SPAWN_EGG.get())
                output.accept(ModItems.GIANT_WASP_SPAWN_EGG.get())
                output.accept(ModItems.JUNGLE_SPIDER_SPAWN_EGG.get())
                output.accept(ModItems.ICE_ELEMENTAL_SPAWN_EGG.get())
                output.accept(ModItems.FOREST_ENT_SPAWN_EGG.get())
                output.accept(ModItems.SORROW_WRAITH_SPAWN_EGG.get())
                output.accept(ModItems.PURPUR_GUARDIAN_SPAWN_EGG.get())
                output.accept(ModItems.VOID_DRAGON_SPAWN_EGG.get())
                output.accept(ModItems.SWAMP_SPIRIT_SPAWN_EGG.get())
                output.accept(ModItems.UNICORN_SPAWN_EGG.get())
                output.accept(ModItems.ASTRAL_WANDERER_SPAWN_EGG.get())
                output.accept(ModItems.LAVA_GOLEM_SPAWN_EGG.get())
                output.accept(ModItems.SCULK_STALKER_SPAWN_EGG.get())
                output.accept(ModItems.SCULK_CRAWLER_SPAWN_EGG.get())
                output.accept(ModItems.TWIN_WITHER_SKELETON_SPAWN_EGG.get())
            }
            .build()
    })

    val DIVINE_BLOCKS: Supplier<CreativeModeTab> = REGISTRY.register("divine_blocks_tab", Supplier<CreativeModeTab> {
        CreativeModeTab.builder()
            .icon { ItemStack(ModBlocks.END_GRASS_BLOCK.get()) }
            .title(Component.translatable("itemGroup.divinesouls.blocks"))
            .displayItems { _, output ->
                output.accept(ModBlocks.END_GRASS_BLOCK.get())
                output.accept(ModBlocks.SCULK_GRASS_BLOCK.get())
                output.accept(ModBlocks.END_WOOD_LEAVES.get())
                output.accept(ModBlocks.SCULK_LEAVES.get())
                output.accept(ModBlocks.END_BUSH_GRASS.get())
                output.accept(ModBlocks.SCULK_GRASS.get())
                output.accept(ModBlocks.WITHERED_END_BUSH.get())
                output.accept(ModBlocks.SCULK_FLOWER.get())
                output.accept(ModBlocks.END_SAPLING.get())
                output.accept(ModBlocks.END_WOOD_LOG.get())
                output.accept(ModBlocks.END_WOOD_PLANKS.get())
                output.accept(ModBlocks.SCULK_LOG.get())
                output.accept(ModBlocks.SCULK_PLANKS.get())
                output.accept(ModBlocks.END_CRYSTAL_ORE.get())
                output.accept(ModBlocks.END_CRYSTAL_BLOCK.get())
                output.accept(ModBlocks.LIFE_CRYSTAL_CLUSTER.get())
                output.accept(ModBlocks.WITHER_SEAL_BLOCK.get())
                output.accept(ModBlocks.ABYSS_PEDESTAL.get())
            }
            .build()
    })

    val DIVINE_SUMMONS: Supplier<CreativeModeTab> = REGISTRY.register("divine_summons_tab", Supplier<CreativeModeTab> {
        CreativeModeTab.builder()
            .icon { ItemStack(ModItems.ANCIENT_RUNE.get()) }
            .title(Component.translatable("itemGroup.divinesouls.summons"))
            .displayItems { _, output ->
                output.accept(ModItems.ANCIENT_RUNE.get()) // Руна уехала сюда
                output.accept(ModItems.WITHER_CORE.get())
                output.accept(ModItems.ABYSSAL_HEART.get())
                output.accept(ModItems.LIFE_CRYSTAL.get())
                output.accept(ModItems.LIFE_FRUIT.get())
                output.accept(ModItems.DIVINE_HEART.get())
                output.accept(ModItems.MANA_CRYSTAL.get())
                output.accept(ModItems.ASTRAL_STAR.get())
            }
            .build()
    })

    val DIVINE_FOOD: Supplier<CreativeModeTab> = REGISTRY.register("divine_food_tab", Supplier<CreativeModeTab> {
        CreativeModeTab.builder()
            .icon { ItemStack(ModItems.COOKED_CRAB_MEAT.get()) }
            .title(Component.translatable("itemGroup.divinesouls.food"))
            .displayItems { _, output ->
                output.accept(ModItems.RAW_CRAB_MEAT.get())
                output.accept(ModItems.COOKED_CRAB_MEAT.get())
                // Обычные зелья маны (I и II)
                output.accept(net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                    net.minecraft.world.item.Items.POTION,
                    ru.rlyqaz.divinesouls.potion.ModPotions.MANA_POTION
                ))
                output.accept(net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                    net.minecraft.world.item.Items.POTION,
                    ru.rlyqaz.divinesouls.potion.ModPotions.STRONG_MANA_POTION
                ))
                // Взрывные зелья маны (I и II)
                output.accept(net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                    net.minecraft.world.item.Items.SPLASH_POTION,
                    ru.rlyqaz.divinesouls.potion.ModPotions.MANA_POTION
                ))
                output.accept(net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                    net.minecraft.world.item.Items.SPLASH_POTION,
                    ru.rlyqaz.divinesouls.potion.ModPotions.STRONG_MANA_POTION
                ))
                // Туманные зелья маны (I и II)
                output.accept(net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                    net.minecraft.world.item.Items.LINGERING_POTION,
                    ru.rlyqaz.divinesouls.potion.ModPotions.MANA_POTION
                ))
                output.accept(net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                    net.minecraft.world.item.Items.LINGERING_POTION,
                    ru.rlyqaz.divinesouls.potion.ModPotions.STRONG_MANA_POTION
                ))
            }
            .build()
    })

    fun register(eventBus: IEventBus) {
        REGISTRY.register(eventBus)
    }
}
