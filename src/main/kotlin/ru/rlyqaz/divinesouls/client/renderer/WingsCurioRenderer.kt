package ru.rlyqaz.divinesouls.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.model.ElytraModel
import net.minecraft.client.model.EntityModel
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.client.renderer.entity.RenderLayerParent
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import ru.rlyqaz.divinesouls.DivineSoulsMod
import top.theillusivec4.curios.api.SlotContext
import top.theillusivec4.curios.api.client.ICurioRenderer

class WingsCurioRenderer(textureName: String, isVanillaTexture: Boolean = false) : ICurioRenderer {

    private val texture = ResourceLocation.fromNamespaceAndPath(
        if (isVanillaTexture) "minecraft" else DivineSoulsMod.MOD_ID,
        "textures/entity/$textureName.png"
    )

    private var elytraModel: ElytraModel<LivingEntity>? = null

    private fun getOrCreateModel(): ElytraModel<LivingEntity> {
        if (elytraModel == null) {
            elytraModel = ElytraModel(Minecraft.getInstance().entityModels.bakeLayer(ModelLayers.ELYTRA))
        }
        return elytraModel!!
    }

    override fun <T : LivingEntity, M : EntityModel<T>> render(
        stack: ItemStack,
        slotContext: SlotContext,
        matrixStack: PoseStack,
        renderLayerParent: RenderLayerParent<T, M>,
        renderTypeBuffer: MultiBufferSource,
        light: Int,
        limbSwing: Float,
        limbSwingAmount: Float,
        partialTicks: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        val entity = slotContext.entity ?: return
        val model = getOrCreateModel()

        matrixStack.pushPose()

        // 1. Привязка к приседу
        ICurioRenderer.translateIfSneaking(matrixStack, entity)
        ICurioRenderer.rotateIfSneaking(matrixStack, entity)

        // Поднимаем ещё на полпикселя вверх для полного слияния с ванильными элитрами
        matrixStack.translate(0.0, -1.5, 0.15)

        matrixStack.scale(2.0f, 2.0f, 2.0f)

        // Базовая анимация (сложенное состояние на земле)
        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch)

        // АНИМАЦИЯ В ВОЗДУХЕ: Раскрываем крылья и машем ими
        if (entity is Player && !entity.onGround() && !entity.isInWater && !entity.isInLava) {
            val isJumping = Minecraft.getInstance().options.keyJump.isDown
            val flap = if (isJumping) Mth.sin(ageInTicks * 0.65f) * 0.45f else 0.0f

            val wings = model.javaClass.declaredFields
                .filter { ModelPart::class.java.isAssignableFrom(it.type) }
                .mapNotNull { field ->
                    field.isAccessible = true
                    field.get(model) as? ModelPart
                }

            if (wings.size >= 2) {
                val wing1 = wings[0]
                val wing2 = wings[1]

                wing1.xRot = 0.35f
                wing1.yRot = 0.25f
                wing1.zRot = 0.85f - flap

                wing2.xRot = 0.35f
                wing2.yRot = -0.25f
                wing2.zRot = -0.85f + flap
            }
        }

        val vertexConsumer = ItemRenderer.getArmorFoilBuffer(
            renderTypeBuffer,
            RenderType.armorCutoutNoCull(texture),
            stack.hasFoil()
        )
        model.renderToBuffer(matrixStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY)

        matrixStack.popPose()
    }
}
