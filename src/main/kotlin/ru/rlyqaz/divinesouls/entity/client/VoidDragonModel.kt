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
import ru.rlyqaz.divinesouls.entity.custom.VoidDragonEntity

class VoidDragonModel(root: ModelPart) :
    EntityModel<VoidDragonEntity>(RenderType::entityCutoutNoCull) {

    companion object {
        val LAYER_LOCATION: ModelLayerLocation = ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "void_dragon"),
            "main"
        )

        fun createBodyLayer(): LayerDefinition {
            val mesh = MeshDefinition()
            val root = mesh.root

            val body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                    .texOffs(0, 46)
                    .addBox(-4.0f, -3.0f, -6.0f, 8.0f, 6.0f, 12.0f, CubeDeformation(0.0f))
                    .texOffs(48, 28)
                    .addBox(-1.0f, -5.0f, -5.0f, 2.0f, 2.0f, 10.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 15.0f, 0.0f)
            )

            val neckBase = body.addOrReplaceChild(
                "neck_base",
                CubeListBuilder.create()
                    .texOffs(40, 61)
                    .addBox(-2.0f, -2.0f, -7.0f, 4.0f, 4.0f, 7.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, -1.0f, -6.0f)
            )

            val neckTip = neckBase.addOrReplaceChild(
                "neck_tip",
                CubeListBuilder.create()
                    .texOffs(54, 14)
                    .addBox(-2.0f, -2.0f, -7.0f, 4.0f, 3.0f, 7.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 0.0f, -7.0f)
            )

            val head = neckTip.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                    .texOffs(54, 0)
                    .addBox(-3.0f, -3.0f, -6.0f, 6.0f, 4.0f, 6.0f, CubeDeformation(0.0f))
                    .texOffs(64, 50)
                    .addBox(-2.0f, -2.0f, -12.0f, 4.0f, 2.0f, 6.0f, CubeDeformation(0.0f))
                    .texOffs(22, 64)
                    .addBox(2.0f, -5.0f, -2.0f, 1.0f, 2.0f, 5.0f, CubeDeformation(0.0f))
                    .texOffs(22, 64).mirror()
                    .addBox(-3.0f, -5.0f, -2.0f, 1.0f, 2.0f, 5.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 0.0f, -7.0f)
            )

            head.addOrReplaceChild(
                "jaw",
                CubeListBuilder.create()
                    .texOffs(22, 72)
                    .addBox(-2.0f, 0.0f, -6.0f, 4.0f, 1.0f, 6.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 0.0f, -6.0f)
            )

            val leftWingInner = body.addOrReplaceChild(
                "left_wing_inner",
                CubeListBuilder.create()
                    .texOffs(48, 40)
                    .addBox(0.0f, -1.0f, -1.0f, 13.0f, 2.0f, 2.0f, CubeDeformation(0.0f))
                    .texOffs(0, 24)
                    .addBox(0.0f, 0.0f, 1.0f, 13.0f, 0.0f, 11.0f, CubeDeformation(0.0f)),
                PartPose.offset(4.0f, -2.0f, -4.0f)
            )

            leftWingInner.addOrReplaceChild(
                "left_wing_outer",
                CubeListBuilder.create()
                    .texOffs(40, 46)
                    .addBox(0.0f, -1.0f, -1.0f, 15.0f, 2.0f, 2.0f, CubeDeformation(0.0f))
                    .texOffs(0, 0)
                    .addBox(0.0f, 0.0f, 1.0f, 15.0f, 0.0f, 12.0f, CubeDeformation(0.0f)),
                PartPose.offset(13.0f, 0.0f, 0.0f)
            )

            val rightWingInner = body.addOrReplaceChild(
                "right_wing_inner",
                CubeListBuilder.create()
                    .texOffs(48, 40).mirror()
                    .addBox(-13.0f, -1.0f, -1.0f, 13.0f, 2.0f, 2.0f, CubeDeformation(0.0f))
                    .texOffs(0, 24).mirror()
                    .addBox(-13.0f, 0.0f, 1.0f, 13.0f, 0.0f, 11.0f, CubeDeformation(0.0f)),
                PartPose.offset(-4.0f, -2.0f, -4.0f)
            )

            rightWingInner.addOrReplaceChild(
                "right_wing_outer",
                CubeListBuilder.create()
                    .texOffs(40, 46).mirror()
                    .addBox(-15.0f, -1.0f, -1.0f, 15.0f, 2.0f, 2.0f, CubeDeformation(0.0f))
                    .texOffs(0, 0).mirror()
                    .addBox(-15.0f, 0.0f, 1.0f, 15.0f, 0.0f, 12.0f, CubeDeformation(0.0f)),
                PartPose.offset(-13.0f, 0.0f, 0.0f)
            )

            val tail1 = body.addOrReplaceChild(
                "tail_1",
                CubeListBuilder.create()
                    .texOffs(40, 50)
                    .addBox(-2.0f, -2.0f, 0.0f, 4.0f, 3.0f, 8.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, -1.0f, 6.0f)
            )

            val tail2 = tail1.addOrReplaceChild(
                "tail_2",
                CubeListBuilder.create()
                    .texOffs(62, 61)
                    .addBox(-1.0f, -1.0f, 0.0f, 2.0f, 2.0f, 9.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 0.0f, 8.0f)
            )

            tail2.addOrReplaceChild(
                "tail_3",
                CubeListBuilder.create()
                    .texOffs(0, 64)
                    .addBox(-1.0f, -1.0f, 0.0f, 2.0f, 2.0f, 9.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 0.0f, 9.0f)
            )

            body.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                    .texOffs(54, 72)
                    .addBox(-1.0f, 0.0f, -1.0f, 2.0f, 6.0f, 2.0f, CubeDeformation(0.0f))
                    .texOffs(72, 35)
                    .addBox(-1.0f, 5.0f, -3.0f, 3.0f, 2.0f, 3.0f, CubeDeformation(0.0f)),
                PartPose.offset(3.0f, 2.0f, 3.0f)
            )

            body.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                    .texOffs(54, 72).mirror()
                    .addBox(-1.0f, 0.0f, -1.0f, 2.0f, 6.0f, 2.0f, CubeDeformation(0.0f))
                    .texOffs(72, 35).mirror()
                    .addBox(-2.0f, 5.0f, -3.0f, 3.0f, 2.0f, 3.0f, CubeDeformation(0.0f)),
                PartPose.offset(-3.0f, 2.0f, 3.0f)
            )

            return LayerDefinition.create(mesh, 128, 128)
        }
    }

    private val body: ModelPart = root.getChild("body")
    private val neckBase: ModelPart = body.getChild("neck_base")
    private val neckTip: ModelPart = neckBase.getChild("neck_tip")
    private val head: ModelPart = neckTip.getChild("head")
    private val jaw: ModelPart = head.getChild("jaw")

    private val leftWingInner: ModelPart = body.getChild("left_wing_inner")
    private val leftWingOuter: ModelPart = leftWingInner.getChild("left_wing_outer")
    private val rightWingInner: ModelPart = body.getChild("right_wing_inner")
    private val rightWingOuter: ModelPart = rightWingInner.getChild("right_wing_outer")

    private val tail1: ModelPart = body.getChild("tail_1")
    private val tail2: ModelPart = tail1.getChild("tail_2")
    private val tail3: ModelPart = tail2.getChild("tail_3")

    private val leftLeg: ModelPart = body.getChild("left_leg")
    private val rightLeg: ModelPart = body.getChild("right_leg")

    override fun setupAnim(
        entity: VoidDragonEntity,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        // Тяжёлый, плавный цикл взмаха крыльев (частота 0.28f)
        val flapPhase = ageInTicks * 0.28f
        val innerFlap = Mth.sin(flapPhase)
        // Внешнее крыло запаздывает по фазе на 0.75 рад — создаёт живую волну перепонки
        val outerFlap = Mth.sin(flapPhase - 0.75f)

        // Вертикальное покачивание корпуса в противофазе со взмахом крыльев
        body.y = 15.0f - innerFlap * 1.4f
        body.xRot = 0.12f + Mth.cos(flapPhase) * 0.06f

        leftWingInner.zRot = innerFlap * 0.62f
        leftWingInner.yRot = -0.15f + Mth.cos(flapPhase) * 0.12f
        leftWingOuter.zRot = outerFlap * 0.55f

        rightWingInner.zRot = -innerFlap * 0.62f
        rightWingInner.yRot = 0.15f - Mth.cos(flapPhase) * 0.12f
        rightWingOuter.zRot = -outerFlap * 0.55f

        // Гибкая S-образная шея и слежение головы за целью
        val yawRad = Mth.clamp(netHeadYaw, -50.0f, 50.0f) * (Math.PI.toFloat() / 180.0f)
        val pitchRad = Mth.clamp(headPitch, -40.0f, 40.0f) * (Math.PI.toFloat() / 180.0f)

        neckBase.xRot = -0.28f + innerFlap * 0.08f
        neckBase.yRot = yawRad * 0.35f
        neckTip.xRot = 0.22f - innerFlap * 0.06f
        neckTip.yRot = yawRad * 0.35f
        head.xRot = pitchRad * 0.6f + 0.05f
        head.yRot = yawRad * 0.3f
// Пасть приоткрывается при атаке или ритмично при дыхании
        val attackBite = Mth.sin(attackTime * Math.PI.toFloat()) * 0.55f
        jaw.xRot = 0.12f + Mth.sin(ageInTicks * 0.12f) * 0.06f + attackBite
// Волнообразное движение длинного хвоста
        tail1.xRot = -0.12f + Mth.sin(flapPhase - 0.5f) * 0.14f
        tail2.xRot = 0.06f + Mth.sin(flapPhase - 1.2f) * 0.18f
        tail3.xRot = 0.08f + Mth.sin(flapPhase - 1.9f) * 0.22f
        val tailSideWave = Mth.cos(ageInTicks * 0.11f) * 0.08f
        tail1.yRot = tailSideWave
        tail2.yRot = tailSideWave * 1.3f
        tail3.yRot = tailSideWave * 1.6f
// Поджатые назад когтистые лапы виверны
        leftLeg.xRot = 0.85f + Mth.sin(flapPhase - 0.3f) * 0.12f
        rightLeg.xRot = 0.85f + Mth.sin(flapPhase - 0.3f) * 0.12f
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