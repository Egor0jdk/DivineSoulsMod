package ru.rlyqaz.divinesouls.entity.client

import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.SlimeRenderer
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.monster.Slime
import ru.rlyqaz.divinesouls.DivineSoulsMod

class MeadowSlimeRenderer(
    context: EntityRendererProvider.Context
) : SlimeRenderer(context) {

    private val texture = ResourceLocation.fromNamespaceAndPath(
        DivineSoulsMod.MOD_ID,
        "textures/entity/meadow_slime.png"
    )

    override fun getTextureLocation(entity: Slime): ResourceLocation {
        return texture
    }
}
