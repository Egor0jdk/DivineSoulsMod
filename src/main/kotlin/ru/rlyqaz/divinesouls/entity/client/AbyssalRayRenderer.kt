package ru.rlyqaz.divinesouls.entity.client

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.AbyssalRayEntity

class AbyssalRayRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<AbyssalRayEntity, AbyssalRayModel>(
        context,
        AbyssalRayModel(context.bakeLayer(AbyssalRayModel.LAYER_LOCATION)),
        1.15f
    ) {

    override fun scale(entity: AbyssalRayEntity, poseStack: PoseStack, partialTickTime: Float) {
        poseStack.scale(1.45f, 1.45f, 1.45f)
    }

    override fun getBlockLightLevel(entity: AbyssalRayEntity, pos: BlockPos): Int = 10

    override fun getTextureLocation(entity: AbyssalRayEntity): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "textures/entity/abyssal_ray.png")
    }
}
