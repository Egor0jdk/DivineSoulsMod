package ru.rlyqaz.divinesouls

import net.minecraft.core.registries.Registries
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.EndPortalFrameBlock
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import net.neoforged.neoforge.event.level.BlockEvent
import net.neoforged.neoforge.registries.DeferredRegister
import ru.rlyqaz.divinesouls.block.ModBlocks
import ru.rlyqaz.divinesouls.entity.ModEntities
import ru.rlyqaz.divinesouls.entity.custom.VoidDragonEntity
import ru.rlyqaz.divinesouls.item.ModCreativeModeTabs
import ru.rlyqaz.divinesouls.item.ModItems
import ru.rlyqaz.divinesouls.item.ModWeapons
import ru.rlyqaz.divinesouls.item.ModArmorMaterials
import ru.rlyqaz.divinesouls.item.ModArmor
import ru.rlyqaz.divinesouls.worldgen.EndOasisFeature
import ru.rlyqaz.divinesouls.worldgen.EndTreeFeature
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import java.util.function.Supplier

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.21.1 NeoForge)
 * ГЛАВНЫЙ КЛАСС: DivineSoulsMod (Центральное ядро)
 * ====================================================================
 */
@Mod(DivineSoulsMod.MOD_ID)
object DivineSoulsMod {
    const val MOD_ID = "divinesouls"
    val LOGGER: org.apache.logging.log4j.Logger = org.apache.logging.log4j.LogManager.getLogger()

    val BLOCKS: DeferredRegister.Blocks = DeferredRegister.createBlocks(MOD_ID)
    val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(MOD_ID)
    val FEATURES: DeferredRegister<Feature<*>> = DeferredRegister.create(Registries.FEATURE, MOD_ID)

    // Фича покрытия поляны
    val END_OASIS_FEATURE: Supplier<Feature<NoneFeatureConfiguration>> = FEATURES.register("end_oasis", Supplier<Feature<*>> {
        EndOasisFeature(NoneFeatureConfiguration.CODEC)
    }) as Supplier<Feature<NoneFeatureConfiguration>>

    // Новая фича отдельно для самого Дерева Края!
    val END_TREE_FEATURE: Supplier<Feature<NoneFeatureConfiguration>> = FEATURES.register("end_tree", Supplier<Feature<*>> {
        EndTreeFeature(NoneFeatureConfiguration.CODEC)
    }) as Supplier<Feature<NoneFeatureConfiguration>>

    init {
        val modEventBus: IEventBus = MOD_BUS

        BLOCKS.register(modEventBus)
        ITEMS.register(modEventBus)
        FEATURES.register(modEventBus)

        ModWeapons.register(modEventBus)
        ModBlocks.register(modEventBus)
        ModItems.register(modEventBus)
        ModEntities.register(modEventBus)
        ModCreativeModeTabs.register(modEventBus)
        ModArmorMaterials.register(modEventBus) // РЕГИСТРИРУЕМ МАТЕРИАЛЫ СТРОГО ДО КЛАССА БРОНИ!
        ModArmor.register(modEventBus)

        // БАГФИКС: Исправил опечатку event.add и сделал вызов чистым благодаря импортам сверху!
        modEventBus.addListener { event: net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent ->
            event.put(ModEntities.VOID_DRAGON.get(), VoidDragonEntity.createAttributes().build())
        }

        NeoForge.EVENT_BUS.register(ru.rlyqaz.divinesouls.item.custom.ArmorEffectsHandler)
        NeoForge.EVENT_BUS.register(ru.rlyqaz.divinesouls.event.WardenBalanceHandler)

        NeoForge.EVENT_BUS.addListener { event: net.neoforged.neoforge.event.server.ServerStoppedEvent ->
            ru.rlyqaz.divinesouls.event.WardenBalanceHandler.clearAllBars()
        }
        NeoForge.EVENT_BUS.register(ru.rlyqaz.divinesouls.event.EndAuraHandler)
        NeoForge.EVENT_BUS.register(this)

        // Клиентский фикс анимации 2D-лука (ItemProperties) под 1.21.1
        modEventBus.addListener { event: net.neoforged.fml.event.lifecycle.FMLClientSetupEvent ->
            event.enqueueWork {
                net.minecraft.client.renderer.item.ItemProperties.register(
                    ModItems.FIRE_BOW.get(),
                    net.minecraft.resources.ResourceLocation.withDefaultNamespace("pull")
                ) { stack, _, entity, _ ->
                    if (entity == null) 0.0f
                    else if (entity.useItem != stack) 0.0f
                    // ФИКС: В 1.21.1 используется метод getUseDuration(entity)
                    else (stack.getUseDuration(entity) - entity.useItemRemainingTicks).toFloat() / 20.0f
                }

                net.minecraft.client.renderer.item.ItemProperties.register(
                    ModItems.FIRE_BOW.get(),
                    net.minecraft.resources.ResourceLocation.withDefaultNamespace("pulling")
                ) { stack, _, entity, _ ->
                    if (entity != null && entity.isUsingItem && entity.useItem == stack) 1.0f else 0.0f
                }
            }
        }

        LOGGER.info("Divine Souls: The main class has been successfully initialized under the name DivineSoulsMod!")
    }

    @SubscribeEvent
    fun onPortalSpawn(event: BlockEvent.PortalSpawnEvent) {
        event.isCanceled = true
    }

    @SubscribeEvent
    fun onEndPortalActivate(event: PlayerInteractEvent.RightClickBlock) {
        val level = event.level
        val pos = event.pos

        if (!level.isClientSide && event.itemStack.`is`(Items.ENDER_EYE)) {
            val state = level.getBlockState(pos)

            if (state.`is`(Blocks.END_PORTAL_FRAME) && !state.getValue(EndPortalFrameBlock.HAS_EYE)) {
                var eyeCount = 0

                for (x in -5..5) {
                    for (z in -5..5) {
                        val checkPos = pos.offset(x, 0, z)
                        if (level.getBlockState(checkPos).`is`(Blocks.END_PORTAL_FRAME)) {
                            if (level.getBlockState(checkPos).getValue(EndPortalFrameBlock.HAS_EYE)) {
                                eyeCount++
                            }
                        }
                    }
                }

                if (eyeCount >= 11) {
                    event.isCanceled = true

                    level.setBlock(pos, state.setValue(EndPortalFrameBlock.HAS_EYE, true), 2)
                    level.levelEvent(null, 1038, pos, 0)

                    val player = event.entity
                    if (player != null && !player.isCreative) {
                        event.itemStack.shrink(1)
                    }

                    LOGGER.info("Divine Souls: Vanilla End portal generation is disabled!")
                }
            }
        }
    }
}
