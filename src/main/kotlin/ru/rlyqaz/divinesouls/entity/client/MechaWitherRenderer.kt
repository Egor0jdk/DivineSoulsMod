package ru.rlyqaz.divinesouls.entity.client

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.model.EntityModel
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.renderer.entity.RenderLayerParent
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.MechaWitherEntity

class MechaWitherRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<MechaWitherEntity, MechaWitherModel>(
        context,
        MechaWitherModel(context.bakeLayer(MechaWitherModel.LAYER_LOCATION)),
        1.0f
    ) {

    init {
        this.addLayer(MechaWitherArmorLayer(this, context))
    }

    override fun scale(entity: MechaWitherEntity, poseStack: PoseStack, partialTickTime: Float) {
        poseStack.scale(1.45f, 1.45f, 1.45f)
    }

    override fun getBlockLightLevel(entity: MechaWitherEntity, pos: BlockPos): Int = 12

    override fun getTextureLocation(entity: MechaWitherEntity): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "textures/entity/mecha_wither.png")
    }

    private class MechaWitherArmorLayer(
        parent: RenderLayerParent<MechaWitherEntity, MechaWitherModel>,
        context: EntityRendererProvider.Context
    ) : EnergySwirlLayer<MechaWitherEntity, MechaWitherModel>(parent) {

        private val model = MechaWitherModel(context.bakeLayer(MechaWitherModel.LAYER_LOCATION))
        private val armorTexture = ResourceLocation.fromNamespaceAndPath(
            DivineSoulsMod.MOD_ID,
            "textures/entity/mecha_wither_armor.png"
        )

        override fun xOffset(ticks: Float): Float = Mth.cos(ticks * 0.02f) * 3.0f

        override fun getTextureLocation(): ResourceLocation = armorTexture

        override fun model(): EntityModel<MechaWitherEntity> = model
    }
}
