package ru.rlyqaz.divinesouls.entity.client

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer
import net.minecraft.resources.ResourceLocation
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.TwinWitherSkeletonEntity

class TwinWitherSkeletonRenderer(
    context: EntityRendererProvider.Context
) : MobRenderer<TwinWitherSkeletonEntity, TwinWitherSkeletonModel>(
    context,
    TwinWitherSkeletonModel(context.bakeLayer(TwinWitherSkeletonModel.LAYER_LOCATION)),
    0.7f
) {
    private val texture = ResourceLocation.fromNamespaceAndPath(
        DivineSoulsMod.MOD_ID,
        "textures/entity/twin_wither_skeleton.png"
    )

    init {
        this.addLayer(ItemInHandLayer(this, context.itemInHandRenderer))
    }

    override fun scale(entity: TwinWitherSkeletonEntity, poseStack: PoseStack, partialTick: Float) {
        poseStack.scale(1.35f, 1.35f, 1.35f)
    }

    override fun getTextureLocation(entity: TwinWitherSkeletonEntity): ResourceLocation {
        return texture
    }
}
