package ru.rlyqaz.divinesouls.client

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.ReceivingLevelScreen
import net.minecraft.client.renderer.DimensionSpecialEffects
import net.minecraft.client.renderer.FogRenderer
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent
import net.neoforged.neoforge.client.event.ScreenEvent
import net.neoforged.neoforge.client.event.ViewportEvent
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.block.ModBlocks
import ru.rlyqaz.divinesouls.block.custom.AbyssPortalBlock

object AbyssClientEffects {

    private val PORTAL_TEXTURE = ResourceLocation.fromNamespaceAndPath(
        DivineSoulsMod.MOD_ID,
        "textures/block/abyss_portal.png"
    )

    // 1. Визуальные эффекты измерения The Abyss
    @EventBusSubscriber(modid = DivineSoulsMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = [Dist.CLIENT])
    object ModBusEvents {
        @SubscribeEvent
        fun onRegisterDimensionEffects(event: RegisterDimensionSpecialEffectsEvent) {
            val abyssEffects = object : DimensionSpecialEffects(
                Float.NaN,
                false,
                SkyType.NONE,
                false,
                true
            ) {
                override fun getBrightnessDependentFogColor(fogColor: Vec3, brightness: Float): Vec3 {
                    return Vec3(0.012, 0.014, 0.032)
                }

                override fun isFoggyAt(x: Int, y: Int): Boolean = true
            }

            event.register(
                ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "the_abyss"),
                abyssEffects
            )
        }
    }

    // 2. Сгущение тумана в Бездне и замена фона "Loading terrain..." на текстуру портала Бездны
    @EventBusSubscriber(modid = DivineSoulsMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = [Dist.CLIENT])
    object GameBusEvents {

        private var abyssTransitionActive = false

        @SubscribeEvent
        fun onClientTick(event: net.neoforged.neoforge.client.event.ClientTickEvent.Post) {
            val mc = Minecraft.getInstance()
            val player = mc.player
            val level = mc.level

            if (player != null && level != null) {
                val inAbyss = level.dimension() == AbyssPortalBlock.THE_ABYSS_KEY
                val insidePortal = level.getBlockState(player.blockPosition()).`is`(ModBlocks.ABYSS_PORTAL.get()) ||
                        level.getBlockState(player.blockPosition().above()).`is`(ModBlocks.ABYSS_PORTAL.get())

                if (inAbyss || insidePortal) {
                    abyssTransitionActive = true
                } else if (mc.screen == null) {
                    abyssTransitionActive = false
                }
            }
        }

        @SubscribeEvent
        fun onRenderFog(event: ViewportEvent.RenderFog) {
            val level = Minecraft.getInstance().level ?: return
            if (level.dimension() != AbyssPortalBlock.THE_ABYSS_KEY) return
            if (event.mode != FogRenderer.FogMode.FOG_TERRAIN && event.mode != FogRenderer.FogMode.FOG_SKY) return

            event.nearPlaneDistance = 14.0f
            event.farPlaneDistance = 78.0f
            event.isCanceled = true
        }

        @SubscribeEvent
        fun onComputeFogColor(event: ViewportEvent.ComputeFogColor) {
            val level = Minecraft.getInstance().level ?: return
            if (level.dimension() != AbyssPortalBlock.THE_ABYSS_KEY) return

            event.red = 0.012f
            event.green = 0.014f
            event.blue = 0.032f
        }

        @SubscribeEvent
        fun onReceivingLevelRenderPost(event: ScreenEvent.Render.Post) {
            if (event.screen !is ReceivingLevelScreen) return
            val mc = Minecraft.getInstance()
            val level = mc.level

            val isAbyssScreen = abyssTransitionActive || level?.dimension() == AbyssPortalBlock.THE_ABYSS_KEY
            if (!isAbyssScreen) return

            val gui = event.guiGraphics
            val width = gui.guiWidth()
            val height = gui.guiHeight()

            RenderSystem.disableDepthTest()
            RenderSystem.depthMask(false)
            gui.setColor(0.55f, 0.78f, 0.95f, 1.0f)
            var x = 0
            while (x < width) {
                var y = 0
                while (y < height) {
                    gui.blit(PORTAL_TEXTURE, x, y, 32, 32, 0.0f, 0.0f, 16, 16, 16, 64)
                    y += 32
                }
                x += 32
            }
            gui.setColor(1.0f, 1.0f, 1.0f, 1.0f)
            RenderSystem.depthMask(true)
            RenderSystem.enableDepthTest()

            gui.drawCenteredString(
                mc.font,
                net.minecraft.network.chat.Component.translatable("multiplayer.downloadingTerrain"),
                width / 2,
                height / 2 - 50,
                0xFFFFFF
            )
        }
    }
}
