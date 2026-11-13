package ru.rlyqaz.divinesouls.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.model.ShieldModel
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import ru.rlyqaz.divinesouls.DivineSoulsMod

class CrystalShieldRenderer : BlockEntityWithoutLevelRenderer(
    Minecraft.getInstance().blockEntityRenderDispatcher,
    Minecraft.getInstance().entityModels
) {
    private val texture = ResourceLocation.fromNamespaceAndPath(
        DivineSoulsMod.MOD_ID,
        "textures/entity/end_crystal_shield.png"
    )

    private val shieldModel: ShieldModel by lazy {
        ShieldModel(Minecraft.getInstance().entityModels.bakeLayer(ModelLayers.SHIELD))
    }

    override fun renderByItem(
        stack: ItemStack,
        displayContext: ItemDisplayContext,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int
    ) {
        poseStack.pushPose()
        poseStack.scale(1.0f, -1.0f, -1.0f)

        val vertexConsumer = ItemRenderer.getFoilBufferDirect(
            buffer,
            shieldModel.renderType(texture),
            false,
            stack.hasFoil()
        )

        shieldModel.handle().render(poseStack, vertexConsumer, packedLight, packedOverlay)
        shieldModel.plate().render(poseStack, vertexConsumer, packedLight, packedOverlay)

        poseStack.popPose()
    }
}
