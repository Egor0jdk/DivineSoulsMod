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

    val SPIDER_NEST_FEATURE: Supplier<Feature<NoneFeatureConfiguration>> = FEATURES.register("spider_nest", Supplier<Feature<*>> {
        ru.rlyqaz.divinesouls.worldgen.SpiderNestFeature(NoneFeatureConfiguration.CODEC)
    }) as Supplier<Feature<NoneFeatureConfiguration>>

    init {
        val modEventBus: IEventBus = MOD_BUS

        BLOCKS.register(modEventBus)
        ITEMS.register(modEventBus)
        FEATURES.register(modEventBus)

        // Прописываем регистрацию кастомных звуков перед предметами
        ru.rlyqaz.divinesouls.sound.ModSounds.register(modEventBus)
        ModWeapons.register(modEventBus)
        ModBlocks.register(modEventBus)
        ModItems.register(modEventBus)
        ModEntities.register(modEventBus)
        ru.rlyqaz.divinesouls.entity.ModEntityEvents.register(modEventBus)
        ModCreativeModeTabs.register(modEventBus)
        ModArmorMaterials.register(modEventBus) // РЕГИСТРИРУЕМ МАТЕРИАЛЫ СТРОГО ДО КЛАССА БРОНИ!
        ModArmor.register(modEventBus)
        ru.rlyqaz.divinesouls.mana.ModAttachments.register(modEventBus)
        NeoForge.EVENT_BUS.register(ru.rlyqaz.divinesouls.mana.ManaManager)
        NeoForge.EVENT_BUS.register(ru.rlyqaz.divinesouls.item.custom.FrostStaffItem.Companion)
        ru.rlyqaz.divinesouls.potion.ModPotions.register(modEventBus)
        NeoForge.EVENT_BUS.register(ru.rlyqaz.divinesouls.potion.ModPotions)

        NeoForge.EVENT_BUS.register(ru.rlyqaz.divinesouls.item.custom.EndCrystalEquipmentHandler)
        NeoForge.EVENT_BUS.register(ru.rlyqaz.divinesouls.event.WardenBalanceHandler)

        NeoForge.EVENT_BUS.addListener { event: net.neoforged.neoforge.event.server.ServerStoppedEvent ->
            ru.rlyqaz.divinesouls.event.WardenBalanceHandler.clearAllBars()
            ru.rlyqaz.divinesouls.worldgen.FortressTwinsInjector.clearQueue()
        }

        NeoForge.EVENT_BUS.register(ru.rlyqaz.divinesouls.event.EndAuraHandler)
        NeoForge.EVENT_BUS.register(this)

        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient) {
            modEventBus.addListener(ru.rlyqaz.divinesouls.mana.ManaHudOverlay::registerGuiLayer)
        }

        modEventBus.addListener { event: net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent ->
            if (event.tabKey.location().namespace == "minecraft") {
                val allStacks = event.parentEntries + event.searchEntries
                val toRemove = allStacks.filter { stack ->
                    val contents = stack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS)
                    val potionHolder = contents?.potion()?.orElse(null)
                    potionHolder == ru.rlyqaz.divinesouls.potion.ModPotions.MANA_POTION ||
                            potionHolder == ru.rlyqaz.divinesouls.potion.ModPotions.STRONG_MANA_POTION
                }
                toRemove.forEach {
                    event.remove(it, net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_TAB_ONLY)
                }
            }
        }

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

                net.minecraft.client.renderer.item.ItemProperties.register(
                    ModWeapons.END_CRYSTAL_SHIELD.get(),
                    net.minecraft.resources.ResourceLocation.withDefaultNamespace("blocking")
                ) { stack, _, entity, _ ->
                    if (entity != null && entity.isUsingItem && entity.useItem == stack) 1.0f else 0.0f
                }

                net.minecraft.client.renderer.item.ItemProperties.register(
                    ModWeapons.LIGHT_SPEAR.get(),
                    net.minecraft.resources.ResourceLocation.withDefaultNamespace("thrusting")
                ) { stack, _, entity, _ ->
                    if (entity != null && entity.isUsingItem && entity.useItem == stack) 1.0f else 0.0f
                }
            }
        }

        modEventBus.addListener { event: net.neoforged.fml.event.lifecycle.FMLClientSetupEvent ->
            event.enqueueWork {
                top.theillusivec4.curios.api.client.CuriosRendererRegistry.register(
                    ru.rlyqaz.divinesouls.item.ModItems.WINGS_OF_WIND.get()
                ) {
                    ru.rlyqaz.divinesouls.client.renderer.WingsCurioRenderer("wings_of_wind")
                }

                // Регистрируем 3D-модель для ванильных Элитр в слоте Curios
                top.theillusivec4.curios.api.client.CuriosRendererRegistry.register(
                    net.minecraft.world.item.Items.ELYTRA
                ) {
                    ru.rlyqaz.divinesouls.client.renderer.WingsCurioRenderer("elytra", isVanillaTexture = true)
                }
            }
        }

        // Подключаем кастомную текстуру 64x64 для Кристального щита Края
        modEventBus.addListener { event: net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent ->
            event.registerItem(
                object : net.neoforged.neoforge.client.extensions.common.IClientItemExtensions {
                    private var renderer: ru.rlyqaz.divinesouls.client.renderer.CrystalShieldRenderer? = null

                    override fun getCustomRenderer(): net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer {
                        if (renderer == null) {
                            renderer = ru.rlyqaz.divinesouls.client.renderer.CrystalShieldRenderer()
                        }
                        return renderer!!
                    }
                },
                ModWeapons.END_CRYSTAL_SHIELD.get()
            )
            event.registerItem(
                object : net.neoforged.neoforge.client.extensions.common.IClientItemExtensions {
                    override fun getArmPose(
                        entityLiving: net.minecraft.world.entity.LivingEntity,
                        hand: net.minecraft.world.InteractionHand,
                        itemStack: net.minecraft.world.item.ItemStack
                    ): net.minecraft.client.model.HumanoidModel.ArmPose? {
                        if (entityLiving.isUsingItem && entityLiving.usedItemHand == hand) {
                            return net.minecraft.client.model.HumanoidModel.ArmPose.CROSSBOW_HOLD
                        }
                        return null
                    }
                },
                ModWeapons.LIGHT_SPEAR.get()
            )
        }

        // Регистрируем сетевой пакет для тарана Кристальным щитом при рывке
        modEventBus.addListener { event: net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent ->
            val registrar = event.registrar(MOD_ID)
            registrar.playToServer(
                ru.rlyqaz.divinesouls.network.DashAttackPayload.TYPE,
                ru.rlyqaz.divinesouls.network.DashAttackPayload.STREAM_CODEC,
                ru.rlyqaz.divinesouls.network.DashAttackPayload::handleServer
            )
            registrar.playToClient(
                ru.rlyqaz.divinesouls.mana.ManaSyncPayload.TYPE,
                ru.rlyqaz.divinesouls.mana.ManaSyncPayload.STREAM_CODEC,
                ru.rlyqaz.divinesouls.mana.ManaSyncPayload::handleClient
            )
        }

        // Регистрируем наш кастомный строитель обсидианового пьедестала в Энде
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(ru.rlyqaz.divinesouls.event.EndPlatformModifier)
        // Регистрируем наш новый класс общих событий (дроп с гастов и плачущий обсидиан)
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(ru.rlyqaz.divinesouls.event.ModCommonEvents)
        // Регистрируем обработчик физики полёта Крыльев Ветра
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(ru.rlyqaz.divinesouls.event.WingsFlightHandler)

        NeoForge.EVENT_BUS.register(ru.rlyqaz.divinesouls.event.WitherSummonHandler)
        NeoForge.EVENT_BUS.register(ru.rlyqaz.divinesouls.event.ElderGuardianBossBarHandler)
        NeoForge.EVENT_BUS.register(ru.rlyqaz.divinesouls.worldgen.FortressTwinsInjector)
        NeoForge.EVENT_BUS.register(ru.rlyqaz.divinesouls.worldgen.SculkInfectionManager)

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
