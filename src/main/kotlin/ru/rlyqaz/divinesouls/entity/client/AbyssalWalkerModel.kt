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
import ru.rlyqaz.divinesouls.entity.custom.AbyssalWalkerEntity

class AbyssalWalkerModel(root: ModelPart) : EntityModel<AbyssalWalkerEntity>() {

    companion object {
        val LAYER_LOCATION: ModelLayerLocation = ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "abyssal_walker"),
            "main"
        )

        fun createBodyLayer(): LayerDefinition {
            val mesh = MeshDefinition()
            val root = mesh.root

            // Голова со скошенной грибной шляпкой (6 x 6 x 6 + шляпка 10 x 3 x 10)
            root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                    .texOffs(0, 14)
                    .addBox(-3.0f, -6.0f, -3.0f, 6.0f, 6.0f, 6.0f, CubeDeformation(0.0f))
                    .texOffs(0, 0)
                    .addBox(-5.0f, -9.0f, -5.0f, 10.0f, 3.0f, 10.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, -2.0f, -1.0f)
            )

            // Вытянутый сутулый торс с грибным наростом на спине (8 x 13 x 5 + нарост 6 x 8 x 3)
            root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                    .texOffs(24, 14)
                    .addBox(-4.0f, 0.0f, -2.5f, 8.0f, 13.0f, 5.0f, CubeDeformation(0.0f))
                    .texOffs(40, 0)
                    .addBox(-3.0f, 1.0f, 2.5f, 6.0f, 8.0f, 3.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, -2.0f, 0.0f)
            )

            // Длинные руки-плети (2.5 x 15 x 2.5)
            root.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create()
                    .texOffs(0, 26)
                    .addBox(0.0f, -1.0f, -1.25f, 2.5f, 15.0f, 2.5f, CubeDeformation(0.0f)),
                PartPose.offset(4.0f, 0.0f, 0.0f)
            )
            root.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create()
                    .texOffs(0, 26).mirror()
                    .addBox(-2.5f, -1.0f, -1.25f, 2.5f, 15.0f, 2.5f, CubeDeformation(0.0f)),
                PartPose.offset(-4.0f, 0.0f, 0.0f)
            )

            // Длинные ходульные ноги (3 x 13 x 3)
            root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                    .texOffs(12, 26)
                    .addBox(-1.5f, 0.0f, -1.5f, 3.0f, 13.0f, 3.0f, CubeDeformation(0.0f)),
                PartPose.offset(2.0f, 11.0f, 0.0f)
            )
            root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                    .texOffs(12, 26).mirror()
                    .addBox(-1.5f, 0.0f, -1.5f, 3.0f, 13.0f, 3.0f, CubeDeformation(0.0f)),
                PartPose.offset(-2.0f, 11.0f, 0.0f)
            )

            return LayerDefinition.create(mesh, 64, 64)
        }
    }

    private val head: ModelPart = root.getChild("head")
    private val body: ModelPart = root.getChild("body")
    private val leftArm: ModelPart = root.getChild("left_arm")
    private val rightArm: ModelPart = root.getChild("right_arm")
    private val leftLeg: ModelPart = root.getChild("left_leg")
    private val rightLeg: ModelPart = root.getChild("right_leg")

    override fun setupAnim(
        entity: AbyssalWalkerEntity,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        head.yRot = netHeadYaw * (Math.PI.toFloat() / 180.0f) * 0.6f
        head.xRot = headPitch * (Math.PI.toFloat() / 180.0f) * 0.5f

        val stride = Mth.cos(limbSwing * 0.65f) * 0.95f * limbSwingAmount
        leftLeg.xRot = stride
        rightLeg.xRot = -stride
        leftArm.xRot = -stride * 0.7f + Mth.sin(ageInTicks * 0.08f) * 0.06f
        rightArm.xRot = stride * 0.7f - Mth.sin(ageInTicks * 0.08f) * 0.06f
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
        leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
    }
}
