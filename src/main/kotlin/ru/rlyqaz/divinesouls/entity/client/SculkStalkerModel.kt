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
import ru.rlyqaz.divinesouls.entity.custom.SculkStalkerEntity

class SculkStalkerModel(root: ModelPart) :
    EntityModel<SculkStalkerEntity>(RenderType::entityCutoutNoCull) {

    companion object {
        val LAYER_LOCATION: ModelLayerLocation = ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "sculk_stalker"),
            "main"
        )

        fun createBodyLayer(): LayerDefinition {
            val mesh = MeshDefinition()
            val root = mesh.root

            // Сгорбленный торс (8 x 13 x 5) + спинной гребень (2 x 10 x 2)
            val body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                    .texOffs(0, 0)
                    .addBox(-4.0f, -13.0f, -2.0f, 8.0f, 13.0f, 5.0f, CubeDeformation(0.0f))
                    .texOffs(42, 7)
                    .addBox(-1.0f, -12.0f, 3.0f, 2.0f, 10.0f, 2.0f, CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 12.0f, 2.0f, 0.48f, 0.0f, 0.0f)
            )

            // Вытянутая слепая голова (6 x 7 x 8) + массивные клыки-челюсти (4 x 4 x 3)
            body.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                    .texOffs(0, 18)
                    .addBox(-3.0f, -7.0f, -7.0f, 6.0f, 7.0f, 8.0f, CubeDeformation(0.0f))
                    .texOffs(40, 0)
                    .addBox(-2.0f, -2.0f, -9.0f, 4.0f, 4.0f, 3.0f, CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -12.0f, -1.0f, -0.42f, 0.0f, 0.0f)
            )

            // Левая рука (3 x 12 x 3) + плоский коготь-лезвие (0 x 10 x 7)
            body.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create()
                    .texOffs(0, 33)
                    .addBox(0.0f, -1.0f, -2.0f, 3.0f, 12.0f, 3.0f, CubeDeformation(0.0f))
                    .texOffs(26, 0)
                    .addBox(1.0f, 7.0f, -8.0f, 0.0f, 10.0f, 7.0f, CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(4.0f, -11.0f, 0.0f, -0.55f, 0.0f, -0.08f)
            )

            // Правая рука + коготь (зеркальные UV от левой руки!)
            body.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create()
                    .texOffs(0, 33).mirror()
                    .addBox(-3.0f, -1.0f, -2.0f, 3.0f, 12.0f, 3.0f, CubeDeformation(0.0f))
                    .texOffs(26, 0).mirror()
                    .addBox(-1.0f, 7.0f, -8.0f, 0.0f, 10.0f, 7.0f, CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-4.0f, -11.0f, 0.0f, -0.55f, 0.0f, 0.08f)
            )

            // Левая нога (3 x 12 x 3)
            root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                    .texOffs(24, 34)
                    .addBox(-2.0f, 0.0f, -2.0f, 3.0f, 12.0f, 3.0f, CubeDeformation(0.0f)),
                PartPose.offset(2.0f, 12.0f, 2.0f)
            )

            // Правая нога (зеркальные UV от левой ноги!)
            root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                    .texOffs(24, 34).mirror()
                    .addBox(-1.0f, 0.0f, -2.0f, 3.0f, 12.0f, 3.0f, CubeDeformation(0.0f)),
                PartPose.offset(-2.0f, 12.0f, 2.0f)
            )

            return LayerDefinition.create(mesh, 64, 64)
        }
    }

    private val body: ModelPart = root.getChild("body")
    private val head: ModelPart = body.getChild("head")
    private val leftArm: ModelPart = body.getChild("left_arm")
    private val rightArm: ModelPart = body.getChild("right_arm")
    private val leftLeg: ModelPart = root.getChild("left_leg")
    private val rightLeg: ModelPart = root.getChild("right_leg")

    override fun setupAnim(
        entity: SculkStalkerEntity,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        val twitch = Mth.sin(ageInTicks * 0.22f) * 0.12f
        head.yRot = netHeadYaw * (Math.PI.toFloat() / 180.0f) * 0.7f + twitch
        head.xRot = -0.42f + headPitch * (Math.PI.toFloat() / 180.0f) * 0.5f

        val aggroLean = if (entity.isAggressive) 0.18f else 0.0f
        body.xRot = 0.48f + aggroLean + Mth.cos(limbSwing * 1.2f) * 0.06f * limbSwingAmount

        val legSwing = Mth.cos(limbSwing * 0.75f) * 1.15f * limbSwingAmount
        rightLeg.xRot = legSwing
        leftLeg.xRot = -legSwing

        val attackSlash = Mth.sin(attackTime * Math.PI.toFloat()) * 1.7f
        rightArm.xRot = -0.55f - legSwing * 0.85f - attackSlash
        leftArm.xRot = -0.55f + legSwing * 0.85f - attackSlash * 0.7f
    }

    override fun renderToBuffer(
        poseStack: PoseStack,
        vertexConsumer: VertexConsumer,
        packedLight: Int,
        packedOverlay: Int,
        color: Int
    ) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
    }
}
