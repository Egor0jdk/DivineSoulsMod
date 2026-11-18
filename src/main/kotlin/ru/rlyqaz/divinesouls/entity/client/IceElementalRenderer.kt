package ru.rlyqaz.divinesouls.entity.client

import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.IceElementalEntity

class IceElementalRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<IceElementalEntity, IceElementalModel<IceElementalEntity>>(
        context,
        IceElementalModel(context.bakeLayer(IceElementalModel.LAYER_LOCATION)),
        0.5f
    ) {

    // Мягкое морозное свечение ледяного ядра в темноте
    override fun getBlockLightLevel(entity: IceElementalEntity, pos: BlockPos): Int = 11

    override fun getTextureLocation(entity: IceElementalEntity): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "textures/entity/ice_elemental.png")
    }
}
