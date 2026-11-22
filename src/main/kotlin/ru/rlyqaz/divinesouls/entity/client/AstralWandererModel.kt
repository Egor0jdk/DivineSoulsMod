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
import ru.rlyqaz.divinesouls.entity.custom.AstralWandererEntity

class AstralWandererModel(root: ModelPart) :
    EntityModel<AstralWandererEntity>(RenderType::entityTranslucent) {

    companion object {
        val LAYER_LOCATION: ModelLayerLocation = ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "astral_wanderer"),
            "main"
        )

        fun createBodyLayer(): LayerDefinition {
            val mesh = MeshDefinition()
            val root = mesh.root

            val head = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                    .texOffs(0, 0)
                    .addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, CubeDeformation(0.0f))
                    .texOffs(0, 16)
                    .addBox(-4.0f, -10.0f, -4.0f, 8.0f, 0.0f, 8.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 4.0f, 0.0f)
            )

            val body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                    .texOffs(0, 24)
                    .addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 4.0f, 0.0f)
            )

            body.addOrReplaceChild(
                "tail_robe",
                CubeListBuilder.create()
                    .texOffs(24, 24)
                    .addBox(-4.0f, 0.0f, -2.0f, 8.0f, 6.0f, 4.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 12.0f, 0.0f)
            )

            root.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create()
                    .texOffs(32, 0)
                    .addBox(0.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, CubeDeformation(0.0f)),
                PartPose.offset(4.0f, 6.0f, 0.0f)
            )

            root.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create()
                    .texOffs(32, 0).mirror()
                    .addBox(-4.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, CubeDeformation(0.0f)),
                PartPose.offset(-4.0f, 6.0f, 0.0f)
            )

            return LayerDefinition.create(mesh, 64, 64)
        }
    }

    private val head: ModelPart = root.getChild("head")
    private val body: ModelPart = root.getChild("body")
    private val tailRobe: ModelPart = body.getChild("tail_robe")
    private val leftArm: ModelPart = root.getChild("left_arm")
    private val rightArm: ModelPart = root.getChild("right_arm")

    override fun setupAnim(
        entity: AstralWandererEntity,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        val hover = Mth.sin(ageInTicks * 0.12f) * 1.1f
        head.y = 4.0f + hover
        body.y = 4.0f + hover
        leftArm.y = 6.0f + hover
        rightArm.y = 6.0f + hover

        head.yRot = netHeadYaw * (Math.PI.toFloat() / 180.0f)
        head.xRot = headPitch * (Math.PI.toFloat() / 180.0f)

        val wave = Mth.sin(ageInTicks * 0.14f) * 0.12f
        leftArm.zRot = -0.18f - wave
        rightArm.zRot = 0.18f + wave
        leftArm.xRot = -0.15f + Mth.cos(ageInTicks * 0.1f) * 0.08f
        rightArm.xRot = -0.15f + Mth.cos(ageInTicks * 0.1f) * 0.08f

        tailRobe.xRot = 0.18f + Mth.sin(ageInTicks * 0.18f) * 0.14f
    }

    override fun renderToBuffer(
        poseStack: PoseStack,
        vertexConsumer: VertexConsumer,
        packedLight: Int,
        packedOverlay: Int,
        color: Int
    ) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        leftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        rightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
    }
}
