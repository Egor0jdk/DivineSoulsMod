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
import ru.rlyqaz.divinesouls.entity.custom.SandyCrabEntity

class SandyCrabModel(root: ModelPart) : EntityModel<SandyCrabEntity>() {

    companion object {
        val LAYER_LOCATION = ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "sandy_crab"),
            "main"
        )

        fun createBodyLayer(): LayerDefinition {
            val meshdefinition = MeshDefinition()
            val partdefinition = meshdefinition.root

            // Единый ровный прямоугольный панцирь (12 x 5 x 10 пикселей)
            partdefinition.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                    .texOffs(0, 0).addBox(-6.0f, -5.0f, -5.0f, 12.0f, 5.0f, 10.0f, CubeDeformation.NONE),
                PartPose.offset(0.0f, 21.0f, 0.0f)
            )

            // Две аккуратные клешни спереди (UV из Blockbench: 18, 15 и 0, 15)
            partdefinition.addOrReplaceChild(
                "right_claw",
                CubeListBuilder.create()
                    .texOffs(18, 15).addBox(-2.0f, -1.5f, -5.0f, 4.0f, 3.0f, 5.0f, CubeDeformation.NONE),
                PartPose.offset(-4.0f, 19.5f, -5.0f)
            )

            partdefinition.addOrReplaceChild(
                "left_claw",
                CubeListBuilder.create()
                    .texOffs(0, 15).addBox(-2.0f, -1.5f, -5.0f, 4.0f, 3.0f, 5.0f, CubeDeformation.NONE),
                PartPose.offset(4.0f, 19.5f, -5.0f)
            )

            // 6 паучьих лапок с индивидуальными UV-координатами из Blockbench
            // Правые лапы (X = -5.5)
            partdefinition.addOrReplaceChild(
                "right_front_leg",
                CubeListBuilder.create()
                    .texOffs(18, 27).addBox(-7.0f, -1.0f, -1.0f, 7.0f, 2.0f, 2.0f, CubeDeformation.NONE),
                PartPose.offset(-5.5f, 19.5f, -2.0f)
            )
            partdefinition.addOrReplaceChild(
                "right_middle_leg",
                CubeListBuilder.create()
                    .texOffs(0, 31).addBox(-7.0f, -1.0f, -1.0f, 7.0f, 2.0f, 2.0f, CubeDeformation.NONE),
                PartPose.offset(-5.5f, 19.5f, 0.0f)
            )
            partdefinition.addOrReplaceChild(
                "right_hind_leg",
                CubeListBuilder.create()
                    .texOffs(18, 31).addBox(-7.0f, -1.0f, -1.0f, 7.0f, 2.0f, 2.0f, CubeDeformation.NONE),
                PartPose.offset(-5.5f, 19.5f, 2.0f)
            )

            // Левые лапы (X = +5.5)
            partdefinition.addOrReplaceChild(
                "left_front_leg",
                CubeListBuilder.create()
                    .texOffs(0, 23).addBox(0.0f, -1.0f, -1.0f, 7.0f, 2.0f, 2.0f, CubeDeformation.NONE),
                PartPose.offset(5.5f, 19.5f, -2.0f)
            )
            partdefinition.addOrReplaceChild(
                "left_middle_leg",
                CubeListBuilder.create()
                    .texOffs(18, 23).addBox(0.0f, -1.0f, -1.0f, 7.0f, 2.0f, 2.0f, CubeDeformation.NONE),
                PartPose.offset(5.5f, 19.5f, 0.0f)
            )
            partdefinition.addOrReplaceChild(
                "left_hind_leg",
                CubeListBuilder.create()
                    .texOffs(0, 27).addBox(0.0f, -1.0f, -1.0f, 7.0f, 2.0f, 2.0f, CubeDeformation.NONE),
                PartPose.offset(5.5f, 19.5f, 2.0f)
            )

            return LayerDefinition.create(meshdefinition, 64, 64)
        }
    }

    private val body: ModelPart = root.getChild("body")
    private val rightClaw: ModelPart = root.getChild("right_claw")
    private val leftClaw: ModelPart = root.getChild("left_claw")

    private val rightFrontLeg: ModelPart = root.getChild("right_front_leg")
    private val rightMiddleLeg: ModelPart = root.getChild("right_middle_leg")
    private val rightHindLeg: ModelPart = root.getChild("right_hind_leg")

    private val leftFrontLeg: ModelPart = root.getChild("left_front_leg")
    private val leftMiddleLeg: ModelPart = root.getChild("left_middle_leg")
    private val leftHindLeg: ModelPart = root.getChild("left_hind_leg")

    override fun setupAnim(
        entity: SandyCrabEntity,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        // Наклон лап к земле, как у ванильного паука (zRot)
        val legTilt = 0.55f
        rightFrontLeg.zRot = -legTilt
        rightMiddleLeg.zRot = -legTilt
        rightHindLeg.zRot = -legTilt

        leftFrontLeg.zRot = legTilt
        leftMiddleLeg.zRot = legTilt
        leftHindLeg.zRot = legTilt

        // Веерное разведение лап (чуть мягче, так как основания уже раздвинуты на 2 пикселя)
        rightFrontLeg.yRot = -0.38f
        rightMiddleLeg.yRot = 0.0f
        rightHindLeg.yRot = 0.38f

        leftFrontLeg.yRot = 0.38f
        leftMiddleLeg.yRot = 0.0f
        leftHindLeg.yRot = -0.38f

        // Ванильная паучья анимация перебирания лап при движении
        val frontSwing = -(Mth.cos(limbSwing * 0.6662f * 2.0f + 0.0f) * 0.4f) * limbSwingAmount
        val midSwing = -(Mth.cos(limbSwing * 0.6662f * 2.0f + Mth.PI) * 0.4f) * limbSwingAmount
        val hindSwing = -(Mth.cos(limbSwing * 0.6662f * 2.0f + Mth.HALF_PI) * 0.4f) * limbSwingAmount

        val frontLift = Math.abs(Mth.sin(limbSwing * 0.6662f + 0.0f) * 0.4f) * limbSwingAmount
        val midLift = Math.abs(Mth.sin(limbSwing * 0.6662f + Mth.PI) * 0.4f) * limbSwingAmount
        val hindLift = Math.abs(Mth.sin(limbSwing * 0.6662f + Mth.HALF_PI) * 0.4f) * limbSwingAmount

        rightFrontLeg.yRot += frontSwing
        leftFrontLeg.yRot -= frontSwing
        rightMiddleLeg.yRot += midSwing
        leftMiddleLeg.yRot -= midSwing
        rightHindLeg.yRot += hindSwing
        leftHindLeg.yRot -= hindSwing

        rightFrontLeg.zRot += frontLift
        leftFrontLeg.zRot -= frontLift
        rightMiddleLeg.zRot += midLift
        leftMiddleLeg.zRot -= midLift
        rightHindLeg.zRot += hindLift
        leftHindLeg.zRot -= hindLift

        // Анимация клешней
        val idleWave = Mth.sin(ageInTicks * 0.1f) * 0.08f
        val attackSwing = Mth.sin(attackTime * Mth.PI) * 0.7f
        rightClaw.yRot = -0.15f + idleWave - attackSwing
        leftClaw.yRot = 0.15f - idleWave + attackSwing

        // Когда краб зарыт — прячем лапы и клешни, остаётся торчать только «камушек» панциря!
        val showLimbs = !entity.isBurrowed
        rightClaw.visible = showLimbs
        leftClaw.visible = showLimbs
        rightFrontLeg.visible = showLimbs
        rightMiddleLeg.visible = showLimbs
        rightHindLeg.visible = showLimbs
        leftFrontLeg.visible = showLimbs
        leftMiddleLeg.visible = showLimbs
        leftHindLeg.visible = showLimbs
    }

    override fun renderToBuffer(
        poseStack: PoseStack,
        vertexConsumer: VertexConsumer,
        packedLight: Int,
        packedOverlay: Int,
        color: Int
    ) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        rightClaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        leftClaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        rightFrontLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        rightMiddleLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        rightHindLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        leftFrontLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        leftMiddleLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        leftHindLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
    }
}
