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
import ru.rlyqaz.divinesouls.entity.custom.ForestEntEntity

class ForestEntModel(root: ModelPart) : EntityModel<ForestEntEntity>() {

    companion object {
        val LAYER_LOCATION: ModelLayerLocation = ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "forest_ent"),
            "main"
        )

        fun createBodyLayer(): LayerDefinition {
            val mesh = MeshDefinition()
            val root = mesh.root

            val body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                    .texOffs(102, 50)
                    .addBox(-7.0f, -18.0f, -5.0f, 14.0f, 18.0f, 10.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 12.0f, 0.0f)
            )

            body.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                    .texOffs(102, 78)
                    .addBox(-6.0f, -11.0f, -6.0f, 12.0f, 11.0f, 12.0f, CubeDeformation(0.0f))
                    .texOffs(0, 0)
                    .addBox(-19.0f, -19.0f, -19.0f, 38.0f, 12.0f, 38.0f, CubeDeformation(0.0f))
                    .texOffs(0, 100)
                    .addBox(-19.0f, -31.0f, -6.5f, 38.0f, 12.0f, 13.0f, CubeDeformation(0.0f))
                    .texOffs(0, 50)
                    .addBox(-6.5f, -31.0f, -19.0f, 13.0f, 12.0f, 38.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, -18.0f, 0.0f)
            )

            // Возвращаем исходные пивоты +7.0f и -7.0f, чтобы руки стояли снаружи торса
            body.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create()
                    .texOffs(124, 101)
                    .addBox(0.0f, -2.0f, -3.0f, 5.0f, 22.0f, 6.0f, CubeDeformation(0.0f)),
                PartPose.offset(7.0f, -15.0f, 0.0f)
            )

            body.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create()
                    .texOffs(102, 101)
                    .addBox(-5.0f, -2.0f, -3.0f, 5.0f, 22.0f, 6.0f, CubeDeformation(0.0f)),
                PartPose.offset(-7.0f, -15.0f, 0.0f)
            )

            root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                    .texOffs(26, 125)
                    .addBox(-3.0f, 0.0f, -3.5f, 6.0f, 12.0f, 7.0f, CubeDeformation(0.0f)),
                PartPose.offset(4.0f, 12.0f, 0.0f)
            )

            root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                    .texOffs(0, 125)
                    .addBox(-3.0f, 0.0f, -3.5f, 6.0f, 12.0f, 7.0f, CubeDeformation(0.0f)),
                PartPose.offset(-4.0f, 12.0f, 0.0f)
            )

            return LayerDefinition.create(mesh, 256, 256)
        }
    }

    private val body: ModelPart = root.getChild("body")
    private val head: ModelPart = body.getChild("head")
    private val leftArm: ModelPart = body.getChild("left_arm")
    private val rightArm: ModelPart = body.getChild("right_arm")
    private val leftLeg: ModelPart = root.getChild("left_leg")
    private val rightLeg: ModelPart = root.getChild("right_leg")

    override fun setupAnim(
        entity: ForestEntEntity,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        val wakeProgress = (entity.wakeTicks.toFloat() / 24.0f).coerceIn(0.0f, 1.0f)
        val wakeEase = Mth.sin(wakeProgress * (Math.PI.toFloat() / 2.0f))
        val wakeSurge = Mth.sin(wakeProgress * Math.PI.toFloat())

        // Прогресс удара корнями: от 1.0 (начало замаха) до 0.0 (конец удара)
        val slamTime = entity.slamAnimTicks.toFloat()
        var slamArmAngle = 0.0f
        var slamBodyLean = 0.0f
        if (slamTime.compareTo(0.0f) == 1) {
            if (slamTime.compareTo(6.0f) == 1) {
                // Фаза 1 (тики 16 -> 6): замах рук вверх над головой
                val windUp = ((20.0f - slamTime) / 14.0f).coerceIn(0.0f, 1.0f)
                slamArmAngle = -2.4f * Mth.sin(windUp * (Math.PI.toFloat() / 2.0f))
                slamBodyLean = -0.22f * windUp
            } else {
                // Фаза 2 (тики 6 -> 0): резкий удар обеими руками в землю
                val strike = (slamTime / 6.0f).coerceIn(0.0f, 1.0f)
                slamArmAngle = -2.4f * strike + 0.55f * (1.0f - strike)
                slamBodyLean = 0.35f * (1.0f - strike)
            }
        }

        head.yRot = netHeadYaw * (Math.PI.toFloat() / 180.0f) * 0.5f
        head.xRot = headPitch * (Math.PI.toFloat() / 180.0f) * 0.4f - wakeSurge * 0.25f + slamBodyLean * 0.5f

        val walkLeg = Mth.cos(limbSwing * 0.6f) * 0.85f * limbSwingAmount
        rightLeg.xRot = walkLeg
        leftLeg.xRot = -walkLeg

        body.zRot = Mth.sin(limbSwing * 0.6f) * 0.07f * limbSwingAmount
        body.xRot = (1.0f - wakeEase) * 0.2f + Mth.cos(limbSwing * 1.2f) * 0.04f * limbSwingAmount + slamBodyLean

        val idleSway = Mth.sin(ageInTicks * 0.06f) * 0.05f
        val attackLift = Mth.sin(attackTime * Math.PI.toFloat()) * 1.6f

        rightArm.xRot = -walkLeg * 0.7f - wakeSurge * 1.45f - attackLift + slamArmAngle
        leftArm.xRot = walkLeg * 0.7f - wakeSurge * 1.45f - attackLift * 0.85f + slamArmAngle

        rightArm.zRot = 0.08f + idleSway + wakeSurge * 0.35f
        leftArm.zRot = -0.08f - idleSway - wakeSurge * 0.35f
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
