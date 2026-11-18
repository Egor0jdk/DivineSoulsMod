package ru.rlyqaz.divinesouls.entity.client

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemDisplayContext
import ru.rlyqaz.divinesouls.entity.custom.PoisonDaggerEntity

class PoisonDaggerRenderer(context: EntityRendererProvider.Context) :
    EntityRenderer<PoisonDaggerEntity>(context) {

    private val itemRenderer: ItemRenderer = context.itemRenderer

    override fun render(
        entity: PoisonDaggerEntity,
        entityYaw: Float,
        partialTicks: Float,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int
    ) {
        poseStack.pushPose()

        // 1. Поворачиваем кинжал по горизонтальному (yRot) и вертикальному (xRot) курсу полёта
        val yaw = Mth.lerp(partialTicks, entity.yRotO, entity.yRot)
        val pitch = Mth.lerp(partialTicks, entity.xRotO, entity.xRot)

        poseStack.mulPose(Axis.YP.rotationDegrees(yaw - 90.0f))
        poseStack.mulPose(Axis.ZP.rotationDegrees(pitch))

        // 2. Разворачиваем диагональную текстуру предмета на -45°, чтобы остриё смотрело строго вперёд
        poseStack.mulPose(Axis.ZP.rotationDegrees(-45.0f))
        poseStack.scale(0.85f, 0.85f, 0.85f)

        // 3. Отрисовываем саму модель предмета с 3D-толщиной (ItemDisplayContext.NONE)
        itemRenderer.renderStatic(
            entity.item,
            ItemDisplayContext.NONE,
            packedLight,
            OverlayTexture.NO_OVERLAY,
            poseStack,
            buffer,
            entity.level(),
            entity.id
        )

        poseStack.popPose()
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight)
    }

    @Suppress("DEPRECATION")
    override fun getTextureLocation(entity: PoisonDaggerEntity): ResourceLocation {
        return TextureAtlas.LOCATION_BLOCKS
    }
}
