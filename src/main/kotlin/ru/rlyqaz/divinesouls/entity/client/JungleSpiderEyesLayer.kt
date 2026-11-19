package ru.rlyqaz.divinesouls.entity.client

import net.minecraft.client.model.SpiderModel
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.RenderLayerParent
import net.minecraft.client.renderer.entity.layers.EyesLayer
import net.minecraft.resources.ResourceLocation
import ru.rlyqaz.divinesouls.entity.custom.JungleSpiderEntity

class JungleSpiderEyesLayer(
    parent: RenderLayerParent<JungleSpiderEntity, SpiderModel<JungleSpiderEntity>>
) : EyesLayer<JungleSpiderEntity, SpiderModel<JungleSpiderEntity>>(parent) {

    companion object {
        private val SPIDER_EYES = RenderType.eyes(
            ResourceLocation.withDefaultNamespace("textures/entity/spider_eyes.png")
        )
    }

    override fun renderType(): RenderType = SPIDER_EYES
}
