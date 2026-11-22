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
import ru.rlyqaz.divinesouls.entity.custom.PurpurGuardianEntity

class PurpurGuardianModel(root: ModelPart) : EntityModel<PurpurGuardianEntity>() {

    companion object {
        val LAYER_LOCATION: ModelLayerLocation = ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "purpur_guardian"),
            "main"
        )

        fun createBodyLayer(): LayerDefinition {
            val mesh = MeshDefinition()
            val root = mesh.root

            // Пурпурный торс (12 x 12 x 7)
            val body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                    .texOffs(0, 0)
                    .addBox(-6.0f, -12.0f, -3.5f, 12.0f, 12.0f, 7.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 12.0f, 0.0f)
            )

            // 3 хаотично торчащих кристалла (все три используют один общий UV (36, 39)!)
            val crystalLeft = body.addOrReplaceChild(
                "crystal_left",
                CubeListBuilder.create(),
                PartPose.offset(-4.5f, -11.5f, 0.0f)
            )
            crystalLeft.addOrReplaceChild(
                "crystal_left_r1",
                CubeListBuilder.create()
                    .texOffs(36, 39)
                    .addBox(-1.5f, -6.0f, -1.5f, 3.0f, 6.0f, 3.0f, CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 2.0f, 0.0f, -0.4363f, -0.3491f, -0.3142f)
            )

            val crystalRight = body.addOrReplaceChild(
                "crystal_right",
                CubeListBuilder.create(),
                PartPose.offset(4.5f, -11.5f, 0.0f)
            )
            crystalRight.addOrReplaceChild(
                "crystal_right_r1",
                CubeListBuilder.create()
                    .texOffs(36, 39)
                    .addBox(-1.5f, -6.0f, -1.5f, 3.0f, 6.0f, 3.0f, CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-3.0f, 11.0f, -4.0f, -0.4385f, 0.3619f, 0.355f)
            )

            val crystalBack = body.addOrReplaceChild(
                "crystal_back",
                CubeListBuilder.create(),
                PartPose.offset(0.0f, -8.5f, 3.0f)
            )
            crystalBack.addOrReplaceChild(
                "crystal_back_r1",
                CubeListBuilder.create()
                    .texOffs(36, 39)
                    .addBox(-1.5f, -6.0f, -1.5f, 3.0f, 6.0f, 3.0f, CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(1.0f, 0.0f, 0.0f, 0.5651f, 0.1182f, -0.1838f)
            )

            // Выдвинутая вперёд голова стража (8 x 8 x 8)
            root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                    .texOffs(0, 19)
                    .addBox(-4.0f, -6.0f, -7.0f, 8.0f, 8.0f, 8.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 0.0f, 0.0f)
            )

            // Левая рука (4 x 15 x 5)
            root.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create()
                    .texOffs(32, 19)
                    .addBox(-4.0f, -2.0f, -2.5f, 4.0f, 15.0f, 5.0f, CubeDeformation(0.0f)),
                PartPose.offset(-6.0f, 2.0f, 0.0f)
            )

            // Правая рука (зеркальный UV от левой!)
            root.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create()
                    .texOffs(32, 19).mirror()
                    .addBox(0.0f, -2.0f, -2.5f, 4.0f, 15.0f, 5.0f, CubeDeformation(0.0f)),
                PartPose.offset(6.0f, 2.0f, 0.0f)
            )

            // Левая нога (4 x 12 x 5)
            root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                    .texOffs(38, 0)
                    .addBox(-2.0f, 0.0f, -2.5f, 4.0f, 12.0f, 5.0f, CubeDeformation(0.0f)),
                PartPose.offset(-3.0f, 12.0f, 0.0f)
            )

            // Правая нога (зеркальный UV от левой!)
            root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                    .texOffs(38, 0).mirror()
                    .addBox(-2.0f, 0.0f, -2.5f, 4.0f, 12.0f, 5.0f, CubeDeformation(0.0f)),
                PartPose.offset(3.0f, 12.0f, 0.0f)
            )

            return LayerDefinition.create(mesh, 64, 64)
        }
    }

    private val body: ModelPart = root.getChild("body")
    private val head: ModelPart = root.getChild("head")
    private val leftArm: ModelPart = root.getChild("left_arm")
    private val rightArm: ModelPart = root.getChild("right_arm")
    private val leftLeg: ModelPart = root.getChild("left_leg")
    private val rightLeg: ModelPart = root.getChild("right_leg")

    override fun setupAnim(
        entity: PurpurGuardianEntity,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        head.yRot = netHeadYaw * (Math.PI.toFloat() / 180.0f)
        head.xRot = headPitch * (Math.PI.toFloat() / 180.0f)

        val legSwing = Mth.cos(limbSwing * 0.6662f) * 1.15f * limbSwingAmount
        leftLeg.xRot = legSwing
        rightLeg.xRot = -legSwing

        if (attackTime > 0.0f) {
            val punch = -1.85f * Mth.sin(attackTime * Math.PI.toFloat())
            rightArm.xRot = punch
            leftArm.xRot = punch * 0.6f
        } else {
            leftArm.xRot = -legSwing * 0.85f
            rightArm.xRot = legSwing * 0.85f
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
        leftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        rightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
    }
}
