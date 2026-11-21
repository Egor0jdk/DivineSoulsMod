package ru.rlyqaz.divinesouls.entity.client

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.resources.ResourceLocation
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.SculkStalkerEntity

class SculkStalkerRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<SculkStalkerEntity, SculkStalkerModel>(
        context,
        SculkStalkerModel(context.bakeLayer(SculkStalkerModel.LAYER_LOCATION)),
        0.65f
    ) {

    override fun scale(entity: SculkStalkerEntity, poseStack: PoseStack, partialTickTime: Float) {
        poseStack.scale(1.1f, 1.1f, 1.1f)
    }

    override fun getTextureLocation(entity: SculkStalkerEntity): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "textures/entity/sculk_stalker.png")
    }
}
