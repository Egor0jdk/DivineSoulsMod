package ru.rlyqaz.divinesouls.entity.client

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.model.EntityModel
import net.minecraft.client.model.geom.ModelLayerLocation
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.PartPose
import net.minecraft.client.model.geom.builders.CubeDeformation
import net.minecraft.client.model.geom.builders.CubeListBuilder
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.model.geom.builders.MeshDefinition
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.SwampSpiritEntity

class SwampSpiritModel<T : SwampSpiritEntity>(root: ModelPart) :
    EntityModel<T>(RenderType::entityTranslucent) {

    companion object {
        val LAYER_LOCATION: ModelLayerLocation = ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "swamp_spirit"),
            "main"
        )

        fun createBodyLayer(): LayerDefinition {
            val meshdefinition = MeshDefinition()
            val partdefinition = meshdefinition.root

            partdefinition.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                    .texOffs(0, 0).addBox(-3.0f, -3.0f, -3.0f, 6.0f, 6.0f, 10.0f, CubeDeformation.NONE),
                PartPose.offset(0.0f, 21.0f, 0.0f)
            )

            return LayerDefinition.create(meshdefinition, 32, 32)
        }
    }

    private val head: ModelPart = root.getChild("head")

    override fun setupAnim(
        entity: T,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        head.yRot = netHeadYaw * (Math.PI.toFloat() / 180f)

        // Вычисляем наклон вверх/вниз прямо по реальному вектору скорости на клиенте!
        val vy = entity.deltaMovement.y
        head.xRot = when {
            vy > 0.05 -> (-Math.PI / 2.0).toFloat()  // Свечой вверх (-90°)
            vy < -0.05 -> (Math.PI / 2.0).toFloat()  // Пикирует вниз (+90°)
            else -> 0.0f                             // Летит горизонтально
        }

        // Лёгкое призрачное колыхание при парении
        head.zRot = Mth.sin(ageInTicks * 0.25f) * 0.08f
    }

    override fun renderToBuffer(
        poseStack: PoseStack,
        vertexConsumer: VertexConsumer,
        packedLight: Int,
        packedOverlay: Int,
        color: Int
    ) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
    }
}
