package ru.rlyqaz.divinesouls.entity.client

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.resources.ResourceLocation
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.PurpurGuardianEntity

class PurpurGuardianRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<PurpurGuardianEntity, PurpurGuardianModel>(
        context,
        PurpurGuardianModel(context.bakeLayer(PurpurGuardianModel.LAYER_LOCATION)),
        0.75f
    ) {

    override fun scale(entity: PurpurGuardianEntity, poseStack: PoseStack, partialTickTime: Float) {
        poseStack.scale(1.15f, 1.15f, 1.15f)
    }

    override fun getTextureLocation(entity: PurpurGuardianEntity): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "textures/entity/purpur_guardian.png")
    }
}
