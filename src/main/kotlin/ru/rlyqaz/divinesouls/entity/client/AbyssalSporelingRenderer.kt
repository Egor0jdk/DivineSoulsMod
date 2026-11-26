package ru.rlyqaz.divinesouls.entity.client

import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.AbyssalSporelingEntity

class AbyssalSporelingRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<AbyssalSporelingEntity, AbyssalSporelingModel>(
        context,
        AbyssalSporelingModel(context.bakeLayer(AbyssalSporelingModel.LAYER_LOCATION)),
        0.45f
    ) {

    override fun getBlockLightLevel(entity: AbyssalSporelingEntity, pos: BlockPos): Int = 9

    override fun getTextureLocation(entity: AbyssalSporelingEntity): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "textures/entity/abyssal_sporeling.png")
    }
}
