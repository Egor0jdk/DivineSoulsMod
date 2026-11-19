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
import ru.rlyqaz.divinesouls.entity.custom.IceElementalEntity
import kotlin.math.cos
import kotlin.math.sin

class IceElementalModel<T : IceElementalEntity>(root: ModelPart) :
    EntityModel<T>(RenderType::entityTranslucent) {

    companion object {
        val LAYER_LOCATION: ModelLayerLocation = ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "ice_elemental"),
            "main"
        )

        fun createBodyLayer(): LayerDefinition {
            val meshdefinition = MeshDefinition()
            val partdefinition = meshdefinition.root

            partdefinition.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                    .texOffs(0, 0).addBox(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f, CubeDeformation.NONE),
                PartPose.offset(0.0f, 12.0f, 0.0f)
            )

            val ring1 = partdefinition.addOrReplaceChild(
                "ring_1",
                CubeListBuilder.create()
                    .texOffs(0, 16).addBox(-13.5f, -4.5f, -1.0f, 2.0f, 9.0f, 2.0f, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0f, 12.0f, 0.0f, 0.0f, 0.0f, -0.6109f)
            )

            ring1.addOrReplaceChild(
                "rod_1_5_r1",
                CubeListBuilder.create()
                    .texOffs(0, 16).addBox(-1.0f, -4.5f, -1.0f, 2.0f, 9.0f, 2.0f, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-6.25f, 0.0f, -10.8253f, 0.0f, 5.236f, 0.0f)
            )
            ring1.addOrReplaceChild(
                "rod_1_4_r1",
                CubeListBuilder.create()
                    .texOffs(0, 16).addBox(-1.0f, -4.5f, -1.0f, 2.0f, 9.0f, 2.0f, CubeDeformation.NONE),
                PartPose.offsetAndRotation(6.25f, 0.0f, -10.8253f, 0.0f, 4.1888f, 0.0f)
            )
            ring1.addOrReplaceChild(
                "rod_1_3_r1",
                CubeListBuilder.create()
                    .texOffs(0, 16).addBox(-1.0f, -4.5f, -1.0f, 2.0f, 9.0f, 2.0f, CubeDeformation.NONE),
                PartPose.offsetAndRotation(12.5f, 0.0f, 0.0f, 0.0f, 3.1416f, 0.0f)
            )
            ring1.addOrReplaceChild(
                "rod_1_2_r1",
                CubeListBuilder.create()
                    .texOffs(0, 16).addBox(-1.0f, -4.5f, -1.0f, 2.0f, 9.0f, 2.0f, CubeDeformation.NONE),
                PartPose.offsetAndRotation(6.25f, 0.0f, 10.8253f, 0.0f, 2.0944f, 0.0f)
            )
            ring1.addOrReplaceChild(
                "rod_1_1_r1",
                CubeListBuilder.create()
                    .texOffs(0, 16).addBox(-1.0f, -4.5f, -1.0f, 2.0f, 9.0f, 2.0f, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-6.25f, 0.0f, 10.8253f, 0.0f, 1.0472f, 0.0f)
            )

            val ring2 = partdefinition.addOrReplaceChild(
                "ring_2",
                CubeListBuilder.create()
                    .texOffs(0, 16).addBox(-13.5f, -4.5f, -1.0f, 2.0f, 9.0f, 2.0f, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0f, 12.0f, 0.0f, 0.0f, 0.0f, 0.6109f)
            )

            ring2.addOrReplaceChild(
                "rod_2_5_r1",
                CubeListBuilder.create()
                    .texOffs(0, 16).addBox(-1.0f, -4.5f, -1.0f, 2.0f, 9.0f, 2.0f, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-6.25f, 0.0f, -10.8253f, 0.0f, 5.236f, 0.0f)
            )
            ring2.addOrReplaceChild(
                "rod_2_4_r1",
                CubeListBuilder.create()
                    .texOffs(0, 16).addBox(-1.0f, -4.5f, -1.0f, 2.0f, 9.0f, 2.0f, CubeDeformation.NONE),
                PartPose.offsetAndRotation(6.25f, 0.0f, -10.8253f, 0.0f, 4.1888f, 0.0f)
            )
            ring2.addOrReplaceChild(
                "rod_2_3_r1",
                CubeListBuilder.create()
                    .texOffs(0, 16).addBox(-1.0f, -4.5f, -1.0f, 2.0f, 9.0f, 2.0f, CubeDeformation.NONE),
                PartPose.offsetAndRotation(12.5f, 0.0f, 0.0f, 0.0f, 3.1416f, 0.0f)
            )
            ring2.addOrReplaceChild(
                "rod_2_2_r1",
                CubeListBuilder.create()
                    .texOffs(0, 16).addBox(-1.0f, -4.5f, -1.0f, 2.0f, 9.0f, 2.0f, CubeDeformation.NONE),
                PartPose.offsetAndRotation(6.25f, 0.0f, 10.8253f, 0.0f, 2.0944f, 0.0f)
            )
            ring2.addOrReplaceChild(
                "rod_2_1_r1",
                CubeListBuilder.create()
                    .texOffs(0, 16).addBox(-1.0f, -4.5f, -1.0f, 2.0f, 9.0f, 2.0f, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-6.25f, 0.0f, 10.8253f, 0.0f, 1.0472f, 0.0f)
            )

            return LayerDefinition.create(meshdefinition, 32, 32)
        }
    }

    private val head: ModelPart = root.getChild("head")
    private val ring1: ModelPart = root.getChild("ring_1")
    private val ring2: ModelPart = root.getChild("ring_2")

    override fun setupAnim(
        entity: T,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        // Голова следит за целью
        head.yRot = netHeadYaw * (Math.PI.toFloat() / 180f)
        head.xRot = headPitch * (Math.PI.toFloat() / 180f)

        // Плавное парение всей конструкции вверх-вниз
        val bob = Mth.sin(ageInTicks * 0.14f) * 1.1f
        head.y = 12.0f + bob
        ring1.y = 12.0f + bob
        ring2.y = 12.0f + bob

        // При зарядке ледяного залпа орбиты раскручиваются в 3.5 раза быстрее!
        val spinSpeed = if (entity.isChargingAttack()) 0.24f else 0.065f
        ring1.yRot = ageInTicks * spinSpeed
        ring2.yRot = -ageInTicks * spinSpeed * 1.1f
    }

    override fun renderToBuffer(
        poseStack: PoseStack,
        vertexConsumer: VertexConsumer,
        packedLight: Int,
        packedOverlay: Int,
        color: Int
    ) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        ring1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
        ring2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
    }
}
