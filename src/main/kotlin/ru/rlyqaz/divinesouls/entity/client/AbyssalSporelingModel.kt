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
import ru.rlyqaz.divinesouls.entity.custom.AbyssalSporelingEntity

class AbyssalSporelingModel(root: ModelPart) : EntityModel<AbyssalSporelingEntity>() {

    companion object {
        val LAYER_LOCATION: ModelLayerLocation = ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "abyssal_sporeling"),
            "main"
        )

        fun createBodyLayer(): LayerDefinition {
            val mesh = MeshDefinition()
            val root = mesh.root

            // Широкая шляпка-зонтик гриба (голова: 12 x 5 x 12) + верхний купол (8 x 3 x 8)
            root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                    .texOffs(0, 0)
                    .addBox(-6.0f, -5.0f, -6.0f, 12.0f, 5.0f, 12.0f, CubeDeformation(0.0f))
                    .texOffs(0, 17)
                    .addBox(-4.0f, -8.0f, -4.0f, 8.0f, 3.0f, 8.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 13.0f, 0.0f)
            )

            // Коренастое тело-ножка (6 x 6 x 6)
            root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                    .texOffs(32, 17)
                    .addBox(-3.0f, 0.0f, -3.0f, 6.0f, 6.0f, 6.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 13.0f, 0.0f)
            )

            // Короткие ручки (2 x 5 x 2)
            root.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create()
                    .texOffs(0, 28)
                    .addBox(0.0f, -1.0f, -1.0f, 2.0f, 5.0f, 2.0f, CubeDeformation(0.0f)),
                PartPose.offset(3.0f, 14.5f, 0.0f)
            )
            root.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create()
                    .texOffs(0, 28).mirror()
                    .addBox(-2.0f, -1.0f, -1.0f, 2.0f, 5.0f, 2.0f, CubeDeformation(0.0f)),
                PartPose.offset(-3.0f, 14.5f, 0.0f)
            )

            // Короткие ножки (2 x 5 x 2)
            root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                    .texOffs(8, 28)
                    .addBox(-1.0f, 0.0f, -1.0f, 2.0f, 5.0f, 2.0f, CubeDeformation(0.0f)),
                PartPose.offset(1.5f, 19.0f, 0.0f)
            )
            root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                    .texOffs(8, 28).mirror()
                    .addBox(-1.0f, 0.0f, -1.0f, 2.0f, 5.0f, 2.0f, CubeDeformation(0.0f)),
                PartPose.offset(-1.5f, 19.0f, 0.0f)
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
        entity: AbyssalSporelingEntity,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        head.yRot = netHeadYaw * (Math.PI.toFloat() / 180.0f) * 0.5f
        head.xRot = headPitch * (Math.PI.toFloat() / 180.0f) * 0.4f + Mth.sin(ageInTicks * 0.1f) * 0.04f

        val walk = Mth.cos(limbSwing * 0.85f) * 1.1f * limbSwingAmount
        leftLeg.xRot = walk
        rightLeg.xRot = -walk
        leftArm.xRot = -walk * 0.8f
        rightArm.xRot = walk * 0.8f
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
