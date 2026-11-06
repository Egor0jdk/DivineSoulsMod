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
                output.accept(ModItems.END_WOOD_STICK.get())
                output.accept(ModItems.END_CRYSTAL_GEM.get())
                output.accept(ModItems.NETHER_ESSENCE.get())
                output.accept(ModItems.ANCIENT_EYE.get())
                output.accept(ModItems.ANCIENT_EYE_SHARD.get())
                output.accept(ModItems.WARDEN_SOUL.get())
                output.accept(ModItems.DRAGON_SOUL_SHARD.get())
                output.accept(ModItems.INFERNAL_CORE.get())
            }
            .build()
    })

    val DIVINE_WEAPONS: Supplier<CreativeModeTab> = REGISTRY.register("divine_weapons_tab", Supplier<CreativeModeTab> {
        CreativeModeTab.builder()
            .icon { ItemStack(ModWeapons.END_CRYSTAL_SWORD.get()) }
            .title(Component.translatable("itemGroup.divinesouls.weapons"))
            .displayItems { _, output ->
                output.accept(ModItems.FIRE_BOW.get())
                output.accept(ModWeapons.END_CRYSTAL_SWORD.get())
                output.accept(ModWeapons.END_CRYSTAL_PICKAXE.get())
                output.accept(ModWeapons.END_CRYSTAL_AXE.get())
                output.accept(ModWeapons.END_CRYSTAL_SHOVEL.get())
                output.accept(ModWeapons.END_CRYSTAL_HOE.get())
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
            }
            .build()
    })

    val DIVINE_MOBS: Supplier<CreativeModeTab> = REGISTRY.register("divine_mobs_tab", Supplier<CreativeModeTab> {
        CreativeModeTab.builder()
            .icon { ItemStack(Items.BAT_SPAWN_EGG) }
            .title(Component.translatable("itemGroup.divinesouls.mobs"))
            .displayItems { _, output ->
                output.accept(Items.BAT_SPAWN_EGG) // Временная ванильная заглушка, чтобы вкладка не скрывалась
            }
            .build()
    })

    val DIVINE_BLOCKS: Supplier<CreativeModeTab> = REGISTRY.register("divine_blocks_tab", Supplier<CreativeModeTab> {
        CreativeModeTab.builder()
            .icon { ItemStack(ModBlocks.END_GRASS_BLOCK.get()) }
            .title(Component.translatable("itemGroup.divinesouls.blocks"))
            .displayItems { _, output ->
                output.accept(ModBlocks.END_CRYSTAL_ORE.get())
                output.accept(ModBlocks.END_GRASS_BLOCK.get())
                output.accept(ModBlocks.END_BUSH_GRASS.get())
                output.accept(ModBlocks.WITHERED_END_BUSH.get())
                output.accept(ModBlocks.END_WOOD_LOG.get())
                output.accept(ModBlocks.END_WOOD_PLANKS.get())
                output.accept(ModBlocks.END_WOOD_LEAVES.get())
                output.accept(ModBlocks.END_SAPLING.get())
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
            }
            .build()
    })

    fun register(eventBus: IEventBus) {
        REGISTRY.register(eventBus)
    }
}
