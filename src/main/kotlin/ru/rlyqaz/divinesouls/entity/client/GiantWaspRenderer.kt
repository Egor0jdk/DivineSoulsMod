package ru.rlyqaz.divinesouls.entity.client

import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.resources.ResourceLocation
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.GiantWaspEntity
import net.minecraft.world.phys.Vec3

class GiantWaspRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<GiantWaspEntity, GiantWaspModel<GiantWaspEntity>>(
        context,
        GiantWaspModel(context.bakeLayer(GiantWaspModel.LAYER_LOCATION)),
        0.45f
    ) {

    override fun getTextureLocation(entity: GiantWaspEntity): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "textures/entity/giant_wasp.png")
    }

    override fun getRenderOffset(entity: GiantWaspEntity, partialTicks: Float): Vec3 {
        return Vec3(0.0, -0.25, 0.0)
    }
}
