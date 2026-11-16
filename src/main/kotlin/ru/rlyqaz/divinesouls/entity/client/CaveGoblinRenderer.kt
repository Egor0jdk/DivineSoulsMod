package ru.rlyqaz.divinesouls.entity.client

import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer
import net.minecraft.resources.ResourceLocation
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.CaveGoblinEntity

class CaveGoblinRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<CaveGoblinEntity, CaveGoblinModel<CaveGoblinEntity>>(
        context,
        CaveGoblinModel(context.bakeLayer(CaveGoblinModel.LAYER_LOCATION)),
        0.45f
    ) {

    init {
        addLayer(ItemInHandLayer(this, context.itemInHandRenderer))
    }

    override fun getTextureLocation(entity: CaveGoblinEntity): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "textures/entity/cave_goblin.png")
    }
}
