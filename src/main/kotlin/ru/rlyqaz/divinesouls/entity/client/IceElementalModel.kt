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

            // Центральное ледяное ядро-голова (8x8x8 пикселей, UV: 0, 0)
            partdefinition.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                    .texOffs(0, 0).addBox(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f, CubeDeformation.NONE),
                PartPose.offset(0.0f, 12.0f, 0.0f)
            )

            // Первая наклонная орбита (+35 градусов по Z) с 6 ледяными стержнями (UV: 32, 0)
            val ring1 = partdefinition.addOrReplaceChild(
                "ring_1",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, 12.0f, 0.0f, 0.0f, 0.0f, 0.6109f)
            )

            // Вторая пересекающаяся орбита (-35 градусов по Z) с 6 ледяными стержнями
            val ring2 = partdefinition.addOrReplaceChild(
                "ring_2",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, 12.0f, 0.0f, 0.0f, 0.0f, -0.6109f)
            )

            val radius = 12.5f
            for (i in 0 until 6) {
                val angle = (i * (2.0 * Math.PI / 6.0)).toFloat()
                val rx = cos(angle) * radius
                val rz = sin(angle) * radius

                ring1.addOrReplaceChild(
                    "rod_1_$i",
                    CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-1.0f, -4.5f, -1.0f, 2.0f, 9.0f, 2.0f, CubeDeformation.NONE),
                    PartPose.offsetAndRotation(rx, 0.0f, rz, 0.0f, -angle, 0.0f)
                )

                ring2.addOrReplaceChild(
                    "rod_2_$i",
                    CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-1.0f, -4.5f, -1.0f, 2.0f, 9.0f, 2.0f, CubeDeformation.NONE),
                    PartPose.offsetAndRotation(rx, 0.0f, rz, 0.0f, -angle, 0.0f)
                )
            }

            return LayerDefinition.create(meshdefinition, 64, 64)
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
