package ru.rlyqaz.divinesouls.entity.client

import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.AbyssalWalkerEntity

class AbyssalWalkerRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<AbyssalWalkerEntity, AbyssalWalkerModel>(
        context,
        AbyssalWalkerModel(context.bakeLayer(AbyssalWalkerModel.LAYER_LOCATION)),
        0.55f
    ) {

    override fun getBlockLightLevel(entity: AbyssalWalkerEntity, pos: BlockPos): Int = 8

    override fun getTextureLocation(entity: AbyssalWalkerEntity): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "textures/entity/abyssal_walker.png")
    }
}
