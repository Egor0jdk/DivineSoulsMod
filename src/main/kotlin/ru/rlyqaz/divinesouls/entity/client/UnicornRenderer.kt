package ru.rlyqaz.divinesouls.entity.client

import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.resources.ResourceLocation
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.UnicornEntity

class UnicornRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<UnicornEntity, UnicornModel>(
        context,
        UnicornModel(context.bakeLayer(UnicornModel.LAYER_LOCATION)),
        0.75f
    ) {

    override fun getTextureLocation(entity: UnicornEntity): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "textures/entity/unicorn.png")
    }
}
