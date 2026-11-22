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
import ru.rlyqaz.divinesouls.entity.custom.UnicornEntity

class UnicornModel(root: ModelPart) : EntityModel<UnicornEntity>() {

    companion object {
        val LAYER_LOCATION: ModelLayerLocation = ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "unicorn"),
            "main"
        )

        fun createBodyLayer(): LayerDefinition {
            val mesh = MeshDefinition()
            val root = mesh.root

            // Корпус коня (10 x 10 x 22) + хвост (3 x 14 x 4)
            val body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                    .texOffs(0, 32)
                    .addBox(-5.0f, -8.0f, -17.0f, 10.0f, 10.0f, 22.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 11.0f, 5.0f)
            )

            body.addOrReplaceChild(
                "tail",
                CubeListBuilder.create()
                    .texOffs(42, 36)
                    .addBox(-1.5f, 0.0f, 0.0f, 3.0f, 14.0f, 4.0f, CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -5.0f, 5.0f, 0.52f, 0.0f, 0.0f)
            )

            // Шея (4 x 12 x 7) + грива (2 x 16 x 2)
            val neck = root.addOrReplaceChild(
                "neck",
                CubeListBuilder.create()
                    .texOffs(0, 35)
                    .addBox(-2.0f, -11.0f, -3.0f, 4.0f, 12.0f, 7.0f, CubeDeformation(0.0f))
                    .texOffs(56, 36)
                    .addBox(-1.0f, -16.0f, 4.0f, 2.0f, 16.0f, 2.0f, CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 7.0f, -8.0f, 0.52f, 0.0f, 0.0f)
            )

            // Голова (6 x 5 x 7), морда (4 x 5 x 5), уши и один цельный 3D-РОГ ЕДИНОРОГА (2 x 8 x 2)
            neck.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                    .texOffs(0, 13)
                    .addBox(-3.0f, -16.0f, -3.0f, 6.0f, 5.0f, 7.0f, CubeDeformation(0.0f))
                    .texOffs(0, 25)
                    .addBox(-2.0f, -16.0f, -8.0f, 4.0f, 5.0f, 5.0f, CubeDeformation(0.0f))
                    // Уши
                    .texOffs(19, 16)
                    .addBox(1.0f, -18.0f, 2.0f, 2.0f, 3.0f, 1.0f, CubeDeformation(0.0f))
                    .texOffs(19, 16).mirror()
                    .addBox(-3.0f, -18.0f, 2.0f, 2.0f, 3.0f, 1.0f, CubeDeformation(0.0f))
                    // Один цельный рог единорога (2 x 8 x 2) в правом верхнем углу развёртки (X: 44..52, Y: 0..10)
                    .texOffs(44, 0)
                    .addBox(-1.0f, -24.0f, -2.0f, 2.0f, 8.0f, 2.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 0.0f, 0.0f)
            )

            val legBuilder = CubeListBuilder.create()
                .texOffs(48, 21)
                .addBox(-2.0f, 0.0f, -2.0f, 4.0f, 11.0f, 4.0f, CubeDeformation(0.0f))

            root.addOrReplaceChild("front_left_leg", legBuilder, PartPose.offset(3.0f, 13.0f, -10.0f))
            root.addOrReplaceChild("front_right_leg", legBuilder, PartPose.offset(-3.0f, 13.0f, -10.0f))
            root.addOrReplaceChild("back_left_leg", legBuilder, PartPose.offset(3.0f, 13.0f, 8.0f))
            root.addOrReplaceChild("back_right_leg", legBuilder, PartPose.offset(-3.0f, 13.0f, 8.0f))

            return LayerDefinition.create(mesh, 64, 64)
        }
    }

    private val body: ModelPart = root.getChild("body")
    private val neck: ModelPart = root.getChild("neck")
    private val frontLeftLeg: ModelPart = root.getChild("front_left_leg")
    private val frontRightLeg: ModelPart = root.getChild("front_right_leg")
    private val backLeftLeg: ModelPart = root.getChild("back_left_leg")
    private val backRightLeg: ModelPart = root.getChild("back_right_leg")

    override fun setupAnim(
        entity: UnicornEntity,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        val yawRad = netHeadYaw * (Math.PI.toFloat() / 180.0f) * 0.55f
        val pitchRad = headPitch * (Math.PI.toFloat() / 180.0f) * 0.45f

        // При подготовке (1) и рывке (2) единорог сильно наклоняет шею и голову вперёд, выставляя рог как копьё!
        val hornThrustPitch = when (entity.chargeState) {
            1 -> 0.55f + Mth.sin(ageInTicks * 0.8f) * 0.08f
            2 -> 0.72f
            3 -> 0.25f + Mth.sin(ageInTicks * 0.25f) * 0.08f // Тяжело дышит на передышке
            else -> 0.0f
        }

        neck.yRot = yawRad
        neck.xRot = 0.52f + pitchRad + hornThrustPitch

        val walk = Mth.cos(limbSwing * 0.6662f) * 1.25f * limbSwingAmount
        frontLeftLeg.xRot = walk
        backRightLeg.xRot = walk
        frontRightLeg.xRot = -walk
        backLeftLeg.xRot = -walk
    }

    override fun renderToBuffer(
        poseStack: PoseStack,
        vertexConsumer: VertexConsumer,
        packedLight: Int,
        packedOverlay: Int,
        color: Int
    ) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        neck.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        frontLeftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        frontRightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        backLeftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        backRightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
    }
}
