package ru.rlyqaz.divinesouls.entity.client

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemDisplayContext
import ru.rlyqaz.divinesouls.entity.custom.IceShardEntity

class IceShardRenderer(context: EntityRendererProvider.Context) :
    EntityRenderer<IceShardEntity>(context) {

    private val itemRenderer: ItemRenderer = context.itemRenderer

    override fun render(
        entity: IceShardEntity,
        entityYaw: Float,
        partialTicks: Float,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int
    ) {
        poseStack.pushPose()

        // 1. Поворачиваем осколок строго по вектору полёта (yRot и xRot)
        val yaw = Mth.lerp(partialTicks, entity.yRotO, entity.yRot)
        val pitch = Mth.lerp(partialTicks, entity.xRotO, entity.xRot)

        poseStack.mulPose(Axis.YP.rotationDegrees(yaw - 90.0f))
        poseStack.mulPose(Axis.ZP.rotationDegrees(pitch))

        // 2. Разворачиваем диагональный спрайт предмета остриём вперёд (если смотрит в обратную сторону — поменяй -45.0f на 135.0f)
        poseStack.mulPose(Axis.ZP.rotationDegrees(-45.0f))
        poseStack.scale(0.95f, 0.95f, 0.95f)

        // 3. Рисуем с полным морозным свечением (LightTexture.FULL_BRIGHT)
        itemRenderer.renderStatic(
            entity.item,
            ItemDisplayContext.NONE,
            LightTexture.FULL_BRIGHT,
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
    override fun getTextureLocation(entity: IceShardEntity): ResourceLocation {
        return TextureAtlas.LOCATION_BLOCKS
    }
}
