package ru.rlyqaz.divinesouls.entity.client

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.LavaGolemEntity

class LavaGolemRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<LavaGolemEntity, LavaGolemModel>(
        context,
        LavaGolemModel(context.bakeLayer(LavaGolemModel.LAYER_LOCATION)),
        1.25f
    ) {

    override fun scale(entity: LavaGolemEntity, poseStack: PoseStack, partialTickTime: Float) {
        poseStack.scale(1.45f, 1.45f, 1.45f)
    }

    override fun getBlockLightLevel(entity: LavaGolemEntity, pos: BlockPos): Int = 10

    override fun getTextureLocation(entity: LavaGolemEntity): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "textures/entity/lava_golem.png")
    }
}
