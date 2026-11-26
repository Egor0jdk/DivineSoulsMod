package ru.rlyqaz.divinesouls.entity.client

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.MechaRocketEntity

class MechaRocketRenderer(context: EntityRendererProvider.Context) :
    EntityRenderer<MechaRocketEntity>(context) {

    companion object {
        private val TEXTURE = ResourceLocation.fromNamespaceAndPath(
            DivineSoulsMod.MOD_ID,
            "textures/entity/mecha_rocket.png"
        )
    }

    private val model = MechaRocketModel(context.bakeLayer(MechaRocketModel.LAYER_LOCATION))

    override fun render(
        entity: MechaRocketEntity,
        entityYaw: Float,
        partialTicks: Float,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int
    ) {
        poseStack.pushPose()

        val yaw = Mth.lerp(partialTicks, entity.yRotO, entity.yRot)
        val pitch = Mth.lerp(partialTicks, entity.xRotO, entity.xRot)

        poseStack.mulPose(Axis.YP.rotationDegrees(yaw - 90.0f))
        poseStack.mulPose(Axis.ZP.rotationDegrees(pitch + 90.0f))
        poseStack.scale(1.35f, 1.35f, 1.35f)
        poseStack.translate(0.0, -1.25, 0.0)

        val consumer = buffer.getBuffer(model.renderType(TEXTURE))
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY)

        poseStack.popPose()
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight)
    }

    override fun getTextureLocation(entity: MechaRocketEntity): ResourceLocation = TEXTURE
}
