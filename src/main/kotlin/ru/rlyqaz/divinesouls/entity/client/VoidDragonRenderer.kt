package ru.rlyqaz.divinesouls.entity.client

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.resources.ResourceLocation
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.VoidDragonEntity

class VoidDragonRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<VoidDragonEntity, VoidDragonModel>(
        context,
        VoidDragonModel(context.bakeLayer(VoidDragonModel.LAYER_LOCATION)),
        0.85f
    ) {

    override fun scale(entity: VoidDragonEntity, poseStack: PoseStack, partialTickTime: Float) {
        poseStack.scale(1.15f, 1.15f, 1.15f)
    }

    override fun getTextureLocation(entity: VoidDragonEntity): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "textures/entity/void_dragon.png")
    }
}
