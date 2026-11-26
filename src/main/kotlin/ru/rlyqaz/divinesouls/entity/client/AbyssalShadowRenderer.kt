package ru.rlyqaz.divinesouls.entity.client

import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.AbyssalShadowEntity

class AbyssalShadowRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<AbyssalShadowEntity, SorrowWraithModel<AbyssalShadowEntity>>(
        context,
        SorrowWraithModel(context.bakeLayer(SorrowWraithModel.LAYER_LOCATION)),
        0.55f
    ) {

    override fun getBlockLightLevel(entity: AbyssalShadowEntity, pos: BlockPos): Int = 11

    override fun getTextureLocation(entity: AbyssalShadowEntity): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "textures/entity/abyssal_shadow.png")
    }
}
