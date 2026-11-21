package ru.rlyqaz.divinesouls.entity.client

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LeavesBlock
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.ForestEntEntity

class ForestEntRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<ForestEntEntity, ForestEntModel>(
        context,
        ForestEntModel(context.bakeLayer(ForestEntModel.LAYER_LOCATION)),
        0.9f
    ) {

    override fun render(
        entity: ForestEntEntity,
        entityYaw: Float,
        partialTicks: Float,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int
    ) {
        // Если энт спит — рисуем настоящее дубовое дерево из блоков вместо 3D-модели!
        if (!entity.isAwake) {
            renderTreeDisguise(poseStack, buffer, packedLight)
            return
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight)
    }

    private fun renderTreeDisguise(poseStack: PoseStack, buffer: MultiBufferSource, packedLight: Int) {
        val blockRenderer = Minecraft.getInstance().blockRenderer
        val oakLog = Blocks.OAK_LOG.defaultBlockState()
        val oakLeaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true)

        poseStack.pushPose()
        poseStack.translate(-0.5, 0.0, -0.5)

        // Ствол 4 блока высотой (Y = 0..3)
        for (y in 0..3) {
            poseStack.pushPose()
            poseStack.translate(0.0, y.toDouble(), 0.0)
            blockRenderer.renderSingleBlock(oakLog, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY)
            poseStack.popPose()
        }

        // Нижний ярус кроны 3x3 на высоте Y = 3 (совпадает с шапкой пробудившегося энта!)
        for (dx in -1..1) {
            for (dz in -1..1) {
                if (dx == 0 && dz == 0) continue
                poseStack.pushPose()
                poseStack.translate(dx.toDouble(), 3.0, dz.toDouble())
                blockRenderer.renderSingleBlock(oakLeaves, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY)
                poseStack.popPose()
            }
        }

        // Верхний ярус кроны крестом на высоте Y = 4
        val topOffsets = listOf(0 to 0, 1 to 0, -1 to 0, 0 to 1, 0 to -1)
        for ((dx, dz) in topOffsets) {
            poseStack.pushPose()
            poseStack.translate(dx.toDouble(), 4.0, dz.toDouble())
            blockRenderer.renderSingleBlock(oakLeaves, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY)
            poseStack.popPose()
        }

        poseStack.popPose()
    }

    override fun scale(entity: ForestEntEntity, poseStack: PoseStack, partialTickTime: Float) {
        poseStack.scale(1.25f, 1.25f, 1.25f)
    }

    override fun getTextureLocation(entity: ForestEntEntity): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "textures/entity/forest_ent.png")
    }
}
