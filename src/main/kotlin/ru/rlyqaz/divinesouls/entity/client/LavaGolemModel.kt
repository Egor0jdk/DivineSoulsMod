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
import ru.rlyqaz.divinesouls.entity.custom.LavaGolemEntity

class LavaGolemModel(root: ModelPart) : EntityModel<LavaGolemEntity>() {

    companion object {
        val LAYER_LOCATION: ModelLayerLocation = ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "lava_golem"),
            "main"
        )

        fun createBodyLayer(): LayerDefinition {
            val mesh = MeshDefinition()
            val root = mesh.root

            // Массивный горбатый торс (18 x 12 x 11) + пояс (10 x 5 x 7)
            root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                    .texOffs(0, 0)
                    .addBox(-9.0f, -12.0f, -5.5f, 18.0f, 12.0f, 11.0f, CubeDeformation(0.0f))
                    .texOffs(44, 39)
                    .addBox(-5.0f, 0.0f, -3.5f, 10.0f, 5.0f, 7.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 7.0f, 0.0f)
            )

            // Голова на выдвинутом вперед пивоте (Z = -5.5f, Y = -1.0f), как у Редстоунового голема из Dungeons
            root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                    .texOffs(44, 23)
                    .addBox(-4.0f, -5.0f, -7.5f, 8.0f, 8.0f, 8.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, -1.0f, -5.5f)
            )

            // Левая рука-молот (5 x 22 x 6)
            root.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create()
                    .texOffs(0, 23)
                    .addBox(0.0f, -2.0f, -3.0f, 5.0f, 22.0f, 6.0f, CubeDeformation(0.0f)),
                PartPose.offset(9.0f, -3.0f, 0.0f)
            )

            // Правая рука-молот (зеркальный UV от левой!)
            root.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create()
                    .texOffs(0, 23).mirror()
                    .addBox(-5.0f, -2.0f, -3.0f, 5.0f, 22.0f, 6.0f, CubeDeformation(0.0f)),
                PartPose.offset(-9.0f, -3.0f, 0.0f)
            )

            // Левая нога-колонна (6 x 12 x 6)
            root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                    .texOffs(0, 51)
                    .addBox(-3.0f, 0.0f, -3.0f, 6.0f, 12.0f, 6.0f, CubeDeformation(0.0f)),
                PartPose.offset(4.5f, 12.0f, 0.0f)
            )

            // Правая нога-колонна (зеркальный UV от левой!)
            root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                    .texOffs(0, 51).mirror()
                    .addBox(-3.0f, 0.0f, -3.0f, 6.0f, 12.0f, 6.0f, CubeDeformation(0.0f)),
                PartPose.offset(-4.5f, 12.0f, 0.0f)
            )

            return LayerDefinition.create(mesh, 128, 128)
        }
    }

    private val body: ModelPart = root.getChild("body")
    private val head: ModelPart = root.getChild("head")
    private val leftArm: ModelPart = root.getChild("left_arm")
    private val rightArm: ModelPart = root.getChild("right_arm")
    private val leftLeg: ModelPart = root.getChild("left_leg")
    private val rightLeg: ModelPart = root.getChild("right_leg")

    override fun setupAnim(
        entity: LavaGolemEntity,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        head.yRot = netHeadYaw * (Math.PI.toFloat() / 180.0f) * 0.6f
        head.xRot = headPitch * (Math.PI.toFloat() / 180.0f) * 0.5f

        val legSwing = Mth.cos(limbSwing * 0.6662f) * 1.1f * limbSwingAmount
        leftLeg.xRot = legSwing
        rightLeg.xRot = -legSwing

        val slamTicks = entity.slamAnimTicks
        if (slamTicks > 0) {
            val slamAngle = when {
                // Фаза 1 (тики 20..9): плавно заносит обе руки высоко вверх над головой
                slamTicks > 8 -> {
                    val windupProgress = (20 - slamTicks) / 12.0f
                    -2.55f * Mth.sin(windupProgress * (Math.PI.toFloat() / 2.0f))
                }
                // Фаза 2 (тики 8..6): резкое обрушение кулаков в землю
                slamTicks >= 6 -> {
                    val strikeProgress = (8 - slamTicks) / 2.0f
                    -2.55f + 2.85f * strikeProgress
                }
                // Фаза 3 (тики 5..1): задержка кулаков в земле и возврат в стойку
                else -> {
                    0.30f * (slamTicks / 5.0f)
                }
            }
            leftArm.xRot = slamAngle
            rightArm.xRot = slamAngle
        } else if (attackTime > 0.0f) {
            val swing = -2.0f + 1.5f * Mth.triangleWave(attackTime, 10.0f)
            leftArm.xRot = swing
            rightArm.xRot = swing
        } else {
            leftArm.xRot = -legSwing * 0.75f
            rightArm.xRot = legSwing * 0.75f
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
