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
import ru.rlyqaz.divinesouls.entity.custom.CaveGoblinEntity

class CaveGoblinModel<T : CaveGoblinEntity>(root: ModelPart) : EntityModel<T>(), ArmedModel {

    companion object {
        val LAYER_LOCATION: ModelLayerLocation = ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "cave_goblin"),
            "main"
        )

        fun createBodyLayer(): LayerDefinition {
            val meshdefinition = MeshDefinition()
            val partdefinition = meshdefinition.root

            partdefinition.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                    .texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, CubeDeformation.NONE)
                    .texOffs(0, 0).addBox(-1.0f, -3.0f, -6.0f, 2.0f, 3.0f, 2.0f, CubeDeformation.NONE)
                    .texOffs(30, 0).addBox(-7.0f, -6.0f, -1.0f, 3.0f, 4.0f, 0.0f, CubeDeformation.NONE)
                    .texOffs(24, 0).addBox(4.0f, -6.0f, -1.0f, 3.0f, 4.0f, 0.0f, CubeDeformation.NONE),
                PartPose.offset(0.0f, 8.0f, 0.0f)
            )

            partdefinition.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                    .texOffs(0, 16).addBox(-4.0f, -8.0f, -2.0f, 8.0f, 8.0f, 4.0f, CubeDeformation.NONE),
                PartPose.offset(0.0f, 16.0f, 0.0f)
            )

            partdefinition.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create()
                    .texOffs(49, 13).addBox(-3.0f, -2.0f, -1.5f, 3.0f, 8.0f, 3.0f, CubeDeformation.NONE)
                    .texOffs(16, 28).addBox(-3.5f, -2.0f, -2.0f, 4.0f, 8.0f, 4.0f, CubeDeformation.NONE),
                PartPose.offset(-4.0f, 10.0f, 0.0f)
            )

            partdefinition.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create()
                    .texOffs(33, 13).addBox(0.0f, -2.0f, -1.5f, 3.0f, 8.0f, 3.0f, CubeDeformation.NONE)
                    .texOffs(0, 28).addBox(-0.5f, -2.0f, -2.0f, 4.0f, 8.0f, 4.0f, CubeDeformation.NONE),
                PartPose.offset(4.0f, 10.0f, 0.0f)
            )

            partdefinition.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                    .texOffs(49, 29).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 8.0f, 3.0f, CubeDeformation.NONE)
                    .texOffs(48, 0).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 8.0f, 4.0f, CubeDeformation.NONE),
                PartPose.offset(-2.0f, 16.0f, 0.0f)
            )

            partdefinition.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                    .texOffs(33, 29).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 8.0f, 3.0f, CubeDeformation.NONE)
                    .texOffs(32, 0).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 8.0f, 4.0f, CubeDeformation.NONE),
                PartPose.offset(2.0f, 16.0f, 0.0f)
            )

            return LayerDefinition.create(meshdefinition, 64, 64)
        }
    }

    private val head: ModelPart = root.getChild("head")
    private val body: ModelPart = root.getChild("body")
    private val rightArm: ModelPart = root.getChild("right_arm")
    private val leftArm: ModelPart = root.getChild("left_arm")
    private val rightLeg: ModelPart = root.getChild("right_leg")
    private val leftLeg: ModelPart = root.getChild("left_leg")

    override fun setupAnim(
        entity: T,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        head.yRot = netHeadYaw * (Math.PI.toFloat() / 180f)
        head.xRot = headPitch * (Math.PI.toFloat() / 180f)

        rightLeg.xRot = Mth.cos(limbSwing * 0.6662f) * 1.4f * limbSwingAmount
        leftLeg.xRot = Mth.cos(limbSwing * 0.6662f + Math.PI.toFloat()) * 1.4f * limbSwingAmount

        rightArm.xRot = Mth.cos(limbSwing * 0.6662f + Math.PI.toFloat()) * 1.6f * limbSwingAmount
        leftArm.xRot = Mth.cos(limbSwing * 0.6662f) * 1.6f * limbSwingAmount
        rightArm.zRot = 0.0f
        leftArm.zRot = 0.0f

        // Если гоблин держит дубинку и бежит за игроком — заносит правую руку для удара
        if (entity.isAggressive) {
            rightArm.xRot -= 0.75f
        }

        // Анимация взмаха при ударе
        if (attackTime > 0.0f) {
            val swing = Mth.sin(attackTime * Math.PI.toFloat())
            rightArm.xRot -= swing * 1.8f
        }

        // Лёгкое покачивание рук в покое
        rightArm.zRot += Mth.cos(ageInTicks * 0.09f) * 0.04f + 0.04f
        leftArm.zRot -= Mth.cos(ageInTicks * 0.09f) * 0.04f + 0.04f
    }

    override fun translateToHand(side: HumanoidArm, poseStack: PoseStack) {
        val arm = if (side == HumanoidArm.RIGHT) rightArm else leftArm
        arm.translateAndRotate(poseStack)
        poseStack.translate(if (side == HumanoidArm.RIGHT) -0.0625 else 0.0625, -0.15, 0.0)
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
        rightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        leftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
    }
}
