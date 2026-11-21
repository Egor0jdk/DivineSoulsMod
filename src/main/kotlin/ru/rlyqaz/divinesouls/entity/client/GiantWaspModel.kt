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
import ru.rlyqaz.divinesouls.entity.custom.GiantWaspEntity

class GiantWaspModel<T : GiantWaspEntity>(root: ModelPart) :
    EntityModel<T>(RenderType::entityCutoutNoCull) {

    companion object {
        val LAYER_LOCATION: ModelLayerLocation = ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "giant_wasp"),
            "main"
        )

        fun createBodyLayer(): LayerDefinition {
            val meshdefinition = MeshDefinition()
            val partdefinition = meshdefinition.root

            partdefinition.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                    .texOffs(0, 28).addBox(-3.5f, -3.0f, -4.0f, 7.0f, 8.0f, 4.0f, CubeDeformation.NONE)
                    .texOffs(28, 10).addBox(-2.0f, -4.0f, -8.0f, 4.0f, 2.0f, 4.0f, CubeDeformation.NONE),
                PartPose.offset(0.0f, 9.0f, 0.0f)
            )

            partdefinition.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                    .texOffs(0, 16).addBox(-3.0f, -19.0f, 0.0f, 6.0f, 6.0f, 6.0f, CubeDeformation.NONE)
                    .texOffs(28, 0).addBox(-3.0f, -13.0f, 0.0f, 6.0f, 4.0f, 6.0f, CubeDeformation.NONE),
                PartPose.offset(0.0f, 25.0f, 0.0f)
            )

            val abdomen = partdefinition.addOrReplaceChild(
                "abdomen",
                CubeListBuilder.create()
                    .texOffs(22, 28).addBox(-2.0f, 7.0f, 4.0f, 4.0f, 4.0f, 4.0f, CubeDeformation.NONE),
                PartPose.offset(0.0f, 10.0f, 5.0f)
            )

            abdomen.addOrReplaceChild(
                "cube_r1",
                CubeListBuilder.create()
                    .texOffs(22, 36).addBox(1.0f, -2.0f, -1.0f, 0.0f, 2.0f, 3.0f, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-1.0f, 13.0f, 4.0f, 0.7854f, 0.0f, 0.0f)
            )

            abdomen.addOrReplaceChild(
                "cube_r2",
                CubeListBuilder.create()
                    .texOffs(0, 0).addBox(-7.0f, -10.0f, -1.0f, 8.0f, 10.0f, 6.0f, CubeDeformation.NONE),
                PartPose.offsetAndRotation(3.0f, 8.0f, 6.0f, 0.7854f, 0.0f, 0.0f)
            )

            partdefinition.addOrReplaceChild(
                "right_wing",
                CubeListBuilder.create()
                    .texOffs(24, 22).mirror().addBox(-9.0f, 0.0f, -3.0f, 9.0f, 0.0f, 6.0f, CubeDeformation.NONE),
                PartPose.offset(-3.0f, 6.0f, 3.0f)
            )

            partdefinition.addOrReplaceChild(
                "left_wing",
                CubeListBuilder.create()
                    .texOffs(24, 22).addBox(0.0f, 0.0f, -3.0f, 9.0f, 0.0f, 6.0f, CubeDeformation.NONE),
                PartPose.offset(3.0f, 6.0f, 3.0f)
            )

            return LayerDefinition.create(meshdefinition, 64, 64)
        }
    }

    private val head: ModelPart = root.getChild("head")
    private val body: ModelPart = root.getChild("body")
    private val abdomen: ModelPart = root.getChild("abdomen")
    private val rightWing: ModelPart = root.getChild("right_wing")
    private val leftWing: ModelPart = root.getChild("left_wing")

    override fun setupAnim(
        entity: T,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        // Голова плавно следит за игроком (с небольшим ограничением угла, чтобы не выворачивалась)
        head.yRot = Mth.clamp(netHeadYaw, -45.0f, 45.0f) * (Math.PI.toFloat() / 180f)
        head.xRot = Mth.clamp(headPitch, -35.0f, 35.0f) * (Math.PI.toFloat() / 180f)

        // Быстрая вибрация крыльев вверх-вниз в полёте
        val wingFlap = Mth.cos(ageInTicks * 2.1f) * 0.45f
        rightWing.zRot = 0.25f + wingFlap
        leftWing.zRot = -0.25f - wingFlap

        // Покачивание всего брюшка с жалом вперёд-назад как единого маятника
        var abdomenPitch = Mth.sin(ageInTicks * 0.18f) * 0.14f
        // Резкий выпад жалом вперёд в момент атаки
        if (attackTime > 0.0f) {
            abdomenPitch -= Mth.sin(attackTime * Math.PI.toFloat()) * 0.55f
        }
        abdomen.xRot = abdomenPitch
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
        abdomen.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        rightWing.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        leftWing.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
    }
}
