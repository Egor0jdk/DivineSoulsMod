package ru.rlyqaz.divinesouls.entity.client

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.model.SpiderModel
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.resources.ResourceLocation
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.JungleSpiderEntity

class JungleSpiderRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<JungleSpiderEntity, SpiderModel<JungleSpiderEntity>>(
        context,
        SpiderModel(context.bakeLayer(ModelLayers.SPIDER)),
        0.7f
    ) {

    init {
        this.addLayer(JungleSpiderEyesLayer(this))
    }

    override fun scale(entity: JungleSpiderEntity, poseStack: PoseStack, partialTickTime: Float) {
        poseStack.scale(0.88f, 0.88f, 0.88f)
    }

    override fun getTextureLocation(entity: JungleSpiderEntity): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "textures/entity/jungle_spider.png")
    }
}
