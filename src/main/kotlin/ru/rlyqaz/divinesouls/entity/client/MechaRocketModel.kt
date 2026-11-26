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
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.MechaRocketEntity

class MechaRocketModel(root: ModelPart) :
    EntityModel<MechaRocketEntity>(RenderType::entityCutoutNoCull) {

    companion object {
        val LAYER_LOCATION: ModelLayerLocation = ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "mecha_rocket"),
            "main"
        )

        fun createBodyLayer(): LayerDefinition {
            val meshdefinition = MeshDefinition()
            val partdefinition = meshdefinition.root

            partdefinition.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                    .texOffs(0, 0).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 0.0f, 3.0f, CubeDeformation(0.0f))
                    .texOffs(8, 3).addBox(-0.5f, -3.0f, -0.5f, 1.0f, 3.0f, 1.0f, CubeDeformation(0.0f))
                    .texOffs(0, 3).addBox(-1.0f, -7.0f, -1.0f, 2.0f, 4.0f, 2.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 24.0f, 0.0f)
            )

            return LayerDefinition.create(meshdefinition, 16, 16)
        }
    }

    private val body: ModelPart = root.getChild("body")

    override fun setupAnim(
        entity: MechaRocketEntity,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
    }

    override fun renderToBuffer(
        poseStack: PoseStack,
        vertexConsumer: VertexConsumer,
        packedLight: Int,
        packedOverlay: Int,
        color: Int
    ) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
    }
}
