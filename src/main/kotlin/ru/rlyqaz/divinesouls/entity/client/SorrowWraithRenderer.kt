package ru.rlyqaz.divinesouls.entity.client

import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.resources.ResourceLocation
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.SorrowWraithEntity

class SorrowWraithRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<SorrowWraithEntity, SorrowWraithModel<SorrowWraithEntity>>(
        context,
        SorrowWraithModel(context.bakeLayer(SorrowWraithModel.LAYER_LOCATION)),
        0.5f // Размер тени под парящим духом
    ) {

    // Жестко связываем рендерер с твоей текстурой-разметкой из Blockbench
    override fun getTextureLocation(entity: SorrowWraithEntity): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "textures/entity/sorrow_wraith.png")
    }
}
