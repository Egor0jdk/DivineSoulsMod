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
import ru.rlyqaz.divinesouls.entity.custom.MechaWitherEntity

class MechaWitherModel(root: ModelPart) : EntityModel<MechaWitherEntity>() {

    companion object {
        val LAYER_LOCATION: ModelLayerLocation = ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "mecha_wither"),
            "main"
        )

        fun createBodyLayer(): LayerDefinition {
            val mesh = MeshDefinition()
            val root = mesh.root

            // Плечи (верхняя перекладина ванильного визера: 20 x 3 x 3)
            root.addOrReplaceChild(
                "shoulders",
                CubeListBuilder.create()
                    .texOffs(0, 16)
                    .addBox(-10.0f, 3.9f, -0.5f, 20.0f, 3.0f, 3.0f, CubeDeformation(0.0f)),
                PartPose.ZERO
            )

            // Позвоночник и 3 пары рёбер (100% ванильные UV)
            root.addOrReplaceChild(
                "ribcage",
                CubeListBuilder.create()
                    .texOffs(0, 22)
                    .addBox(0.0f, 0.0f, 0.0f, 3.0f, 10.0f, 3.0f, CubeDeformation(0.0f))
                    .texOffs(24, 22)
                    .addBox(-4.0f, 1.5f, 0.5f, 11.0f, 2.0f, 2.0f, CubeDeformation(0.0f))
                    .texOffs(24, 22)
                    .addBox(-4.0f, 4.0f, 0.5f, 11.0f, 2.0f, 2.0f, CubeDeformation(0.0f))
                    .texOffs(24, 22)
                    .addBox(-4.0f, 6.5f, 0.5f, 11.0f, 2.0f, 2.0f, CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-2.0f, 6.9f, -0.5f, 0.20420352f, 0.0f, 0.0f)
            )

            // Нижний хвост позвоночника
            root.addOrReplaceChild(
                "tail",
                CubeListBuilder.create()
                    .texOffs(12, 22)
                    .addBox(0.0f, 0.0f, 0.0f, 3.0f, 6.0f, 3.0f, CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-2.0f, 16.9f, 1.5f, 0.5042035f, 0.0f, 0.0f)
            )

            // Центральная голова (8 x 8 x 8 на UV (0, 0)) + 1 выступающий блок лазерной пушки спереди (4 x 4 x 4 на UV (48, 48))!
            root.addOrReplaceChild(
                "center_head",
                CubeListBuilder.create()
                    .texOffs(0, 0)
                    .addBox(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f, CubeDeformation(0.0f))
                    .texOffs(48, 48)
                    .addBox(-2.0f, -1.5f, -7.5f, 4.0f, 4.0f, 4.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 0.0f, 0.0f)
            )

            // Правая боковая голова (ракетная установка, стандартный UV (32, 0))
            root.addOrReplaceChild(
                "right_head",
                CubeListBuilder.create()
                    .texOffs(32, 0)
                    .addBox(-4.0f, -4.0f, -4.0f, 6.0f, 6.0f, 6.0f, CubeDeformation(0.0f)),
                PartPose.offset(-8.0f, 4.0f, 0.0f)
            )

            // Левая боковая голова (ракетная установка, стандартный UV (32, 0))
            root.addOrReplaceChild(
                "left_head",
                CubeListBuilder.create()
                    .texOffs(32, 0)
                    .addBox(-4.0f, -4.0f, -4.0f, 6.0f, 6.0f, 6.0f, CubeDeformation(0.0f)),
                PartPose.offset(10.0f, 4.0f, 0.0f)
            )

            return LayerDefinition.create(mesh, 64, 64)
        }
    }

    private val shoulders: ModelPart = root.getChild("shoulders")
    private val ribcage: ModelPart = root.getChild("ribcage")
    private val tail: ModelPart = root.getChild("tail")
    private val centerHead: ModelPart = root.getChild("center_head")
    private val rightHead: ModelPart = root.getChild("right_head")
    private val leftHead: ModelPart = root.getChild("left_head")

    override fun setupAnim(
        entity: MechaWitherEntity,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        val hoverTilt = Mth.cos(ageInTicks * 0.1f)
        val dashLean = if (entity.isDashing()) 0.45f else 0.0f

        ribcage.xRot = (0.065f + 0.05f * hoverTilt) * Math.PI.toFloat() + dashLean
        tail.setPos(-2.0f, 6.9f + Mth.cos(ribcage.xRot) * 10.0f, -0.5f + Mth.sin(ribcage.xRot) * 10.0f)
        tail.xRot = (0.265f + 0.1f * hoverTilt) * Math.PI.toFloat() + dashLean

        val yawRad = netHeadYaw * (Math.PI.toFloat() / 180.0f)
        val pitchRad = headPitch * (Math.PI.toFloat() / 180.0f)

        centerHead.yRot = yawRad
        centerHead.xRot = pitchRad
        rightHead.yRot = yawRad * 0.85f
        rightHead.xRot = pitchRad * 0.85f
        leftHead.yRot = yawRad * 0.85f
        leftHead.xRot = pitchRad * 0.85f
    }

    override fun renderToBuffer(
        poseStack: PoseStack,
        vertexConsumer: VertexConsumer,
        packedLight: Int,
        packedOverlay: Int,
        color: Int
    ) {
        shoulders.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        ribcage.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        tail.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        centerHead.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        rightHead.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        leftHead.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
    }
}
