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
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.SculkCrawlerEntity

class SculkCrawlerModel(root: ModelPart) : EntityModel<SculkCrawlerEntity>() {

    companion object {
        val LAYER_LOCATION: ModelLayerLocation = ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "sculk_crawler"),
            "main"
        )

        fun createBodyLayer(): LayerDefinition {
            val mesh = MeshDefinition()
            val root = mesh.root

            // Единый цельный корпус (10 x 6 x 14) + спинной гребень
            root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                    .texOffs(0, 0)
                    .addBox(-5.0f, -3.0f, -7.0f, 10.0f, 6.0f, 14.0f, CubeDeformation(0.0f))
                    .texOffs(0, 20)
                    .addBox(-2.0f, -5.0f, -5.0f, 4.0f, 2.0f, 10.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 17.0f, 1.0f)
            )

            // Голова (6 x 5 x 6) + два зеркальных сенсорных клыка (2 x 4 x 5)
            root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                    .texOffs(28, 20)
                    .addBox(-3.0f, -3.0f, -6.0f, 6.0f, 5.0f, 6.0f, CubeDeformation(0.0f))
                    .texOffs(28, 35)
                    .addBox(1.0f, 0.0f, -10.0f, 2.0f, 4.0f, 5.0f, CubeDeformation(0.0f))
                    .texOffs(28, 35).mirror()
                    .addBox(-3.0f, 0.0f, -10.0f, 2.0f, 4.0f, 5.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 17.0f, -6.0f)
            )

            val leftLegBuilder = CubeListBuilder.create()
                .texOffs(28, 31)
                .addBox(0.0f, -1.0f, -1.0f, 12.0f, 2.0f, 2.0f, CubeDeformation(0.0f))
            val rightLegBuilder = CubeListBuilder.create()
                .texOffs(28, 31).mirror()
                .addBox(-12.0f, -1.0f, -1.0f, 12.0f, 2.0f, 2.0f, CubeDeformation(0.0f))

            root.addOrReplaceChild("left_leg_1", leftLegBuilder, PartPose.offset(4.0f, 17.0f, -4.0f))
            root.addOrReplaceChild("left_leg_2", leftLegBuilder, PartPose.offset(4.0f, 17.0f, -1.0f))
            root.addOrReplaceChild("left_leg_3", leftLegBuilder, PartPose.offset(4.0f, 17.0f, 2.0f))
            root.addOrReplaceChild("left_leg_4", leftLegBuilder, PartPose.offset(4.0f, 17.0f, 5.0f))

            root.addOrReplaceChild("right_leg_1", rightLegBuilder, PartPose.offset(-4.0f, 17.0f, -4.0f))
            root.addOrReplaceChild("right_leg_2", rightLegBuilder, PartPose.offset(-4.0f, 17.0f, -1.0f))
            root.addOrReplaceChild("right_leg_3", rightLegBuilder, PartPose.offset(-4.0f, 17.0f, 2.0f))
            root.addOrReplaceChild("right_leg_4", rightLegBuilder, PartPose.offset(-4.0f, 17.0f, 5.0f))

            return LayerDefinition.create(mesh, 64, 64)
        }
    }

    private val body: ModelPart = root.getChild("body")
    private val head: ModelPart = root.getChild("head")
    private val leftLegs = listOf(
        root.getChild("left_leg_1"),
        root.getChild("left_leg_2"),
        root.getChild("left_leg_3"),
        root.getChild("left_leg_4")
    )
    private val rightLegs = listOf(
        root.getChild("right_leg_1"),
        root.getChild("right_leg_2"),
        root.getChild("right_leg_3"),
        root.getChild("right_leg_4")
    )

    override fun setupAnim(
        entity: SculkCrawlerEntity,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        head.yRot = netHeadYaw * (Math.PI.toFloat() / 180.0f) * 0.6f
        head.xRot = headPitch * (Math.PI.toFloat() / 180.0f) * 0.6f + Mth.sin(ageInTicks * 0.15f) * 0.05f

        val baseTilt = 0.62f
        val fanAngles = listOf(0.55f, 0.18f, -0.18f, -0.55f)

        for (i in 0 until 4) {
            val phase = i * 1.1f
            val swing = Mth.cos(limbSwing * 0.8f + phase) * 0.42f * limbSwingAmount
            val lift = kotlin.math.abs(Mth.sin(limbSwing * 0.8f + phase)) * 0.35f * limbSwingAmount

            leftLegs[i].yRot = fanAngles[i] + swing
            leftLegs[i].zRot = baseTilt - lift

            rightLegs[i].yRot = -fanAngles[i] - swing
            rightLegs[i].zRot = -baseTilt + lift
        }
    }

    override fun renderToBuffer(
        poseStack: PoseStack,
        vertexConsumer: VertexConsumer,
        packedLight: Int,
        packedOverlay: Int,
        color: Int
    ) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        leftLegs.forEach { it.render(poseStack, vertexConsumer, packedLight, packedOverlay, color) }
        rightLegs.forEach { it.render(poseStack, vertexConsumer, packedLight, packedOverlay, color) }
    }
}
