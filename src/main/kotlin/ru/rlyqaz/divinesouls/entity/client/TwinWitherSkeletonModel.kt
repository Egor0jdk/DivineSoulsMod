package ru.rlyqaz.divinesouls.entity.client

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.model.ArmedModel
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
import net.minecraft.world.entity.HumanoidArm
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.TwinWitherSkeletonEntity

class TwinWitherSkeletonModel(root: ModelPart) : EntityModel<TwinWitherSkeletonEntity>(), ArmedModel {

    companion object {
        val LAYER_LOCATION = ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "twin_wither_skeleton"),
            "main"
        )

        fun createBodyLayer(): LayerDefinition {
            val meshDefinition = MeshDefinition()
            val partDefinition = meshDefinition.root

            partDefinition.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                    .texOffs(32, 0).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, CubeDeformation(0.0f))
                    .texOffs(32, 16).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, CubeDeformation(0.3f)),
                PartPose.offset(0.0f, 0.0f, 0.0f)
            )

            partDefinition.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create()
                    .texOffs(16, 16).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f, CubeDeformation(0.0f)),
                PartPose.offset(5.0f, 2.0f, 0.0f)
            )

            partDefinition.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create()
                    .texOffs(16, 16).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f, CubeDeformation(0.0f)),
                PartPose.offset(-5.0f, 2.0f, 0.0f)
            )

            partDefinition.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                    .texOffs(0, 16).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f, CubeDeformation(0.0f)),
                PartPose.offset(2.0f, 12.0f, 0.0f)
            )

            partDefinition.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                    .texOffs(0, 16).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f, CubeDeformation(0.0f)),
                PartPose.offset(-2.0f, 12.0f, 0.0f)
            )

            partDefinition.addOrReplaceChild(
                "left_head",
                CubeListBuilder.create()
                    .texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, CubeDeformation(0.0f)),
                PartPose.offset(4.2f, 0.0f, 0.0f)
            )

            partDefinition.addOrReplaceChild(
                "right_head",
                CubeListBuilder.create()
                    .texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, CubeDeformation(0.0f)),
                PartPose.offset(-4.2f, 0.0f, 0.0f)
            )

            return LayerDefinition.create(meshDefinition, 64, 64)
        }
    }

    private val body: ModelPart = root.getChild("body")
    private val rightArm: ModelPart = root.getChild("right_arm")
    private val leftArm: ModelPart = root.getChild("left_arm")
    private val rightLeg: ModelPart = root.getChild("right_leg")
    private val leftLeg: ModelPart = root.getChild("left_leg")
    private val leftHead: ModelPart = root.getChild("left_head")
    private val rightHead: ModelPart = root.getChild("right_head")

    override fun setupAnim(
        entity: TwinWitherSkeletonEntity,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        this.rightArm.xRot = 0.0f
        this.rightArm.yRot = 0.0f
        this.rightArm.zRot = 0.0f

        this.leftArm.xRot = 0.0f
        this.leftArm.yRot = 0.0f
        this.leftArm.zRot = 0.0f

        val yawRad = netHeadYaw * (Math.PI.toFloat() / 180f)
        val pitchRad = headPitch * (Math.PI.toFloat() / 180f)

        this.leftHead.yRot = yawRad + Mth.cos(ageInTicks * 0.05f) * 0.08f
        this.leftHead.xRot = pitchRad + Mth.sin(ageInTicks * 0.04f) * 0.05f

        this.rightHead.yRot = yawRad - Mth.cos(ageInTicks * 0.05f) * 0.08f
        this.rightHead.xRot = pitchRad - Mth.sin(ageInTicks * 0.04f) * 0.05f

        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662f) * 1.4f * limbSwingAmount
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662f + Math.PI.toFloat()) * 1.4f * limbSwingAmount

        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662f + Math.PI.toFloat()) * 2.0f * limbSwingAmount * 0.5f
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662f) * 2.0f * limbSwingAmount * 0.5f

        val windup = entity.getSlamWindupTicks()
        if (windup > 0) {
            val chargeProgress = ((15 - windup).coerceIn(0, 15)) / 15.0f
            this.rightArm.xRot = -2.6f * chargeProgress
            this.rightArm.zRot = 0.15f * chargeProgress
            this.leftArm.xRot = -0.6f * chargeProgress
        } else {
            if (entity.isAggressive) {
                this.rightArm.xRot -= 0.45f
                this.leftArm.xRot -= 0.35f
            }

            if (this.attackTime > 0.0f) {
                val swing = Mth.sin(this.attackTime * Math.PI.toFloat())
                this.rightArm.xRot -= swing * 1.6f
            }
        }
    }

    override fun translateToHand(side: HumanoidArm, poseStack: PoseStack) {
        val arm = if (side == HumanoidArm.RIGHT) this.rightArm else this.leftArm
        arm.translateAndRotate(poseStack)
    }

    override fun renderToBuffer(
        poseStack: PoseStack,
        vertexConsumer: VertexConsumer,
        packedLight: Int,
        packedOverlay: Int,
        color: Int
    ) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        rightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        leftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        leftHead.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        rightHead.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
    }
}
