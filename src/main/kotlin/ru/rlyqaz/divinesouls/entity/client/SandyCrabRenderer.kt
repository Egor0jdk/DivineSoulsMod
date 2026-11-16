package ru.rlyqaz.divinesouls.entity.client

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.resources.ResourceLocation
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.SandyCrabEntity

class SandyCrabRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<SandyCrabEntity, SandyCrabModel>(
        context,
        SandyCrabModel(context.bakeLayer(SandyCrabModel.LAYER_LOCATION)),
        0.55f
    ) {

    companion object {
        private val TEXTURE = ResourceLocation.fromNamespaceAndPath(
            DivineSoulsMod.MOD_ID,
            "textures/entity/sandy_crab.png"
        )
    }

    override fun getTextureLocation(entity: SandyCrabEntity): ResourceLocation = TEXTURE

    override fun scale(entity: SandyCrabEntity, poseStack: PoseStack, partialTickTime: Float) {
        if (entity.isBurrowed) {
            // Зарытый краб уходит в грунт так, что сверху торчат только верхушка панциря и глаза
            poseStack.translate(0.0, 0.24, 0.0)
        }
    }
}
