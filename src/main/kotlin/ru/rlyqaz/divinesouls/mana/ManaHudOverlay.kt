package ru.rlyqaz.divinesouls.mana

import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent
import net.neoforged.neoforge.client.gui.VanillaGuiLayers
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.item.custom.BaseMagicStaffItem

object ManaHudOverlay {

    private val STAR_EMPTY = ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "textures/gui/mana_star_empty.png")
    private val STAR_HALF = ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "textures/gui/mana_star_half.png")
    private val STAR_FULL = ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "textures/gui/mana_star_full.png")

    fun registerGuiLayer(event: RegisterGuiLayersEvent) {
        event.registerAbove(
            VanillaGuiLayers.FOOD_LEVEL,
            ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "mana_bar"),
            ::render
        )
    }

    private fun render(guiGraphics: GuiGraphics, @Suppress("UNUSED_PARAMETER") deltaTracker: DeltaTracker) {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        if (mc.options.hideGui || player.isSpectator) return

        val data = player.getData(ModAttachments.MANA.get())
        val holdsMagicWeapon = player.mainHandItem.item is BaseMagicStaffItem ||
                player.offhandItem.item is BaseMagicStaffItem

        if (!holdsMagicWeapon && data.mana == data.maxMana) return

        val totalStars = (data.maxMana / 10).coerceAtLeast(1)
        val screenWidth = guiGraphics.guiWidth()
        val screenHeight = guiGraphics.guiHeight()

        val rightMargin = screenWidth - 10
        val baseY = screenHeight - 18

        for (i in 0 until totalStars) {
            val col = i % 10
            val row = i / 10
            val x = rightMargin - (col + 1) * 9
            val y = baseY - row * 10
            val starThreshold = i * 10

            guiGraphics.blit(STAR_EMPTY, x, y, 0f, 0f, 9, 9, 9, 9)

            val diff = data.mana - starThreshold
            if (diff.compareTo(10) != -1) {
                guiGraphics.blit(STAR_FULL, x, y, 0f, 0f, 9, 9, 9, 9)
            } else if (diff.compareTo(5) != -1) {
                guiGraphics.blit(STAR_HALF, x, y, 0f, 0f, 9, 9, 9, 9)
            }
        }
    }
}
