package ru.rlyqaz.divinesouls.entity.client

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.SwampSpiritEntity

class SwampSpiritRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<SwampSpiritEntity, SwampSpiritModel<SwampSpiritEntity>>(
        context,
        SwampSpiritModel(context.bakeLayer(SwampSpiritModel.LAYER_LOCATION)),
        0.35f
    ) {

    override fun scale(entity: SwampSpiritEntity, poseStack: PoseStack, partialTickTime: Float) {
        // 6 пикселей * 1.33 = ровно 8 пикселей (половина блока), текстура не сбивается!
        poseStack.scale(1.33f, 1.33f, 1.33f)
    }

    override fun getBlockLightLevel(entity: SwampSpiritEntity, pos: BlockPos): Int = 15

    override fun getTextureLocation(entity: SwampSpiritEntity): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "textures/entity/swamp_spirit.png")
    }
}
