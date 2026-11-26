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
import ru.rlyqaz.divinesouls.entity.custom.AbyssalRayEntity

class AbyssalRayModel(root: ModelPart) : EntityModel<AbyssalRayEntity>() {

    companion object {
        val LAYER_LOCATION: ModelLayerLocation = ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "abyssal_ray"),
            "main"
        )

        fun createBodyLayer(): LayerDefinition {
            val mesh = MeshDefinition()
            val root = mesh.root

            // Массивный центральный корпус ската (14 x 4 x 18) + передние головные плавники манты
            val body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                    .texOffs(0, 0)
                    .addBox(-7.0f, -2.0f, -9.0f, 14.0f, 4.0f, 18.0f, CubeDeformation(0.0f))
                    // Левый головной рог-плавник
                    .texOffs(0, 44)
                    .addBox(3.0f, -1.0f, -14.0f, 3.0f, 2.0f, 5.0f, CubeDeformation(0.0f))
                    // Правый головной рог-плавник
                    .texOffs(0, 44).mirror()
                    .addBox(-6.0f, -1.0f, -14.0f, 3.0f, 2.0f, 5.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 19.0f, 0.0f)
            )

            // Левое крыло (внутренняя секция 11 x 2 x 14 + внешний кончик 10 x 1 x 10)
            val leftWing = body.addOrReplaceChild(
                "left_wing",
                CubeListBuilder.create()
                    .texOffs(0, 22)
                    .addBox(0.0f, -1.0f, -7.0f, 11.0f, 2.0f, 14.0f, CubeDeformation(0.0f)),
                PartPose.offset(7.0f, 0.0f, 0.0f)
            )
            leftWing.addOrReplaceChild(
                "left_wing_tip",
                CubeListBuilder.create()
                    .texOffs(50, 22)
                    .addBox(0.0f, -0.5f, -5.0f, 10.0f, 1.0f, 10.0f, CubeDeformation(0.0f)),
                PartPose.offset(11.0f, 0.0f, 1.0f)
            )

            // Правое крыло (зеркальное)
            val rightWing = body.addOrReplaceChild(
                "right_wing",
                CubeListBuilder.create()
                    .texOffs(0, 22).mirror()
                    .addBox(-11.0f, -1.0f, -7.0f, 11.0f, 2.0f, 14.0f, CubeDeformation(0.0f)),
                PartPose.offset(-7.0f, 0.0f, 0.0f)
            )
            rightWing.addOrReplaceChild(
                "right_wing_tip",
                CubeListBuilder.create()
                    .texOffs(50, 22).mirror()
                    .addBox(-10.0f, -0.5f, -5.0f, 10.0f, 1.0f, 10.0f, CubeDeformation(0.0f)),
                PartPose.offset(-11.0f, 0.0f, 1.0f)
            )

            // Гибкий хвост-хлыст из двух сегментов
            val tailBase = body.addOrReplaceChild(
                "tail_base",
                CubeListBuilder.create()
                    .texOffs(64, 0)
                    .addBox(-1.5f, -1.0f, 0.0f, 3.0f, 2.0f, 11.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 0.0f, 9.0f)
            )
            tailBase.addOrReplaceChild(
                "tail_tip",
                CubeListBuilder.create()
                    .texOffs(64, 13)
                    .addBox(-0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 12.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 0.0f, 11.0f)
            )

            return LayerDefinition.create(mesh, 128, 128)
        }
    }

    private val body: ModelPart = root.getChild("body")
    private val leftWing: ModelPart = body.getChild("left_wing")
    private val leftWingTip: ModelPart = leftWing.getChild("left_wing_tip")
    private val rightWing: ModelPart = body.getChild("right_wing")
    private val rightWingTip: ModelPart = rightWing.getChild("right_wing_tip")
    private val tailBase: ModelPart = body.getChild("tail_base")
    private val tailTip: ModelPart = tailBase.getChild("tail_tip")

    override fun setupAnim(
        entity: AbyssalRayEntity,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        val wave = ageInTicks * 0.18f
        val innerFlap = Mth.sin(wave) * 0.28f
        val outerFlap = Mth.sin(wave - 0.85f) * 0.36f

        body.y = 19.0f - Mth.sin(wave) * 1.1f
        body.xRot = Mth.clamp(headPitch, -30.0f, 30.0f) * (Math.PI.toFloat() / 180.0f) * 0.45f

        leftWing.zRot = innerFlap
        leftWingTip.zRot = outerFlap

        rightWing.zRot = -innerFlap
        rightWingTip.zRot = -outerFlap

        tailBase.xRot = -0.08f + Mth.sin(wave - 0.6f) * 0.14f
        tailTip.xRot = -0.06f + Mth.sin(wave - 1.4f) * 0.20f
        tailBase.yRot = Mth.cos(ageInTicks * 0.09f) * 0.08f
        tailTip.yRot = Mth.cos(ageInTicks * 0.09f - 0.7f) * 0.12f
    }

    override fun renderToBuffer(
        poseStack: PoseStack,
        vertexConsumer: VertexConsumer,
        packedLight: Int,
        packedOverlay: Int,
        color: Int
    ) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
    }
}
