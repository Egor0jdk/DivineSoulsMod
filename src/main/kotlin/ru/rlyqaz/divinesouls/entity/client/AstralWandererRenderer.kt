package ru.rlyqaz.divinesouls.entity.client

import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.AstralWandererEntity

class AstralWandererRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<AstralWandererEntity, AstralWandererModel>(
        context,
        AstralWandererModel(context.bakeLayer(AstralWandererModel.LAYER_LOCATION)),
        0.55f
    ) {

    // Мягкое небесное свечение духа в ночной темноте
    override fun getBlockLightLevel(entity: AstralWandererEntity, pos: BlockPos): Int = 12

    override fun getTextureLocation(entity: AstralWandererEntity): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "textures/entity/astral_wanderer.png")
    }
}
