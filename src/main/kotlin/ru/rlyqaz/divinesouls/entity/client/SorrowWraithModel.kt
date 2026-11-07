package ru.rlyqaz.divinesouls.entity.client

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.model.EntityModel
import net.minecraft.client.model.geom.ModelLayerLocation
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.PartPose
import net.minecraft.client.model.geom.builders.*
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.SorrowWraithEntity

class SorrowWraithModel<T : SorrowWraithEntity>(rootPart: ModelPart) : EntityModel<T>() {

    // БАГФИКС: Достаем кости напрямую из rootPart, чтобы избежать NullPointerException при спавне!
    private val root: ModelPart = rootPart.getChild("root")
    private val body: ModelPart = this.root.getChild("body")
    private val head: ModelPart = this.root.getChild("head")
    private val rightArm: ModelPart = this.root.getChild("right_arm")
    private val leftArm: ModelPart = this.root.getChild("left_arm")

    override fun setupAnim(
        entity: T,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        // Поворот головы по углам взгляда ИИ моба
        this.head.yRot = netHeadYaw * (Mth.PI / 180.0f)
        this.head.xRot = headPitch * (Mth.PI / 180.0f)

        // Руки и тело в абсолютной статике
        this.rightArm.xRot = 0.0f
        this.rightArm.yRot = 0.0f
        this.leftArm.xRot = 0.0f
        this.leftArm.yRot = 0.0f
    }

    override fun renderToBuffer(
        poseStack: PoseStack,
        vertexConsumer: VertexConsumer,
        packedLight: Int,
        packedOverlay: Int,
        color: Int
    ) {
        // Отрисовываем корень модели
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color)
    }

    companion object {
        val LAYER_LOCATION = ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "sorrow_wraith"),
            "main"
        )

        @JvmStatic
        fun createBodyLayer(): LayerDefinition {
            val meshdefinition = MeshDefinition()
            val partdefinition = meshdefinition.root

            // Обязательно регистрируем корневую кость "root"
            val root = partdefinition.addOrReplaceChild(
                "root",
                CubeListBuilder.create(),
                PartPose.offset(0.0f, 24.0f, 0.0f)
            )

            root.addOrReplaceChild(
                "body",
                CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, -12.0f, -2.0f, 8.0f, 24.0f, 4.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, -12.0f, 0.0f)
            )

            root.addOrReplaceChild(
                "head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, -24.0f, 0.0f)
            )

            root.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create().texOffs(0, 28).addBox(-4.0f, -3.0f, -10.0f, 4.0f, 4.0f, 12.0f, CubeDeformation(0.0f)),
                PartPose.offset(-4.0f, -21.0f, 0.0f)
            )

            root.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create().texOffs(32, 28).addBox(0.0f, -2.0f, -10.0f, 4.0f, 4.0f, 12.0f, CubeDeformation(0.0f)),
                PartPose.offset(4.0f, -22.0f, 0.0f)
            )

            return LayerDefinition.create(meshdefinition, 64, 64)
        }
    }
}
