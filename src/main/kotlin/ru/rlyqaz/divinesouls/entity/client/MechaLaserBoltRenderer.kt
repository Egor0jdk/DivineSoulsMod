package ru.rlyqaz.divinesouls.entity.client

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.MechaLaserBoltEntity
import kotlin.math.cos
import kotlin.math.sin

class MechaLaserBoltRenderer(context: EntityRendererProvider.Context) :
    EntityRenderer<MechaLaserBoltEntity>(context) {

    companion object {
        private val TEXTURE = ResourceLocation.fromNamespaceAndPath(
            DivineSoulsMod.MOD_ID,
            "textures/entity/mecha_laser.png"
        )
    }

    override fun render(
        entity: MechaLaserBoltEntity,
        entityYaw: Float,
        partialTicks: Float,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int
    ) {
        poseStack.pushPose()

        val vel = entity.deltaMovement
        val forward = if (vel.lengthSqr() > 0.0001) vel.normalize() else Vec3(0.0, 0.0, 1.0)

        // Вектор от снаряда к камере игрока
        val camPos = entityRenderDispatcher.camera.position
        val entityPos = entity.getPosition(partialTicks)
        val toCam = camPos.subtract(entityPos)

        // Вращаем 2D-спрайт вокруг продольной оси полёта строго плоской стороной к камере
        var side = forward.cross(toCam)
        if (side.lengthSqr() < 0.0001) {
            side = forward.cross(Vec3(0.0, 1.0, 0.0))
        }
        side = side.normalize()

        val halfLength = 0.65
        val halfWidth = 0.22

        val pStart = forward.scale(-halfLength)
        val pEnd = forward.scale(halfLength)
        val offset = side.scale(halfWidth)

        val v1 = pStart.subtract(offset)
        val v2 = pStart.add(offset)
        val v3 = pEnd.add(offset)
        val v4 = pEnd.subtract(offset)

        val consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE))
        val matrix = poseStack.last().pose()

        addVertex(consumer, matrix, v1, 0.0f, 1.0f)
        addVertex(consumer, matrix, v2, 0.0f, 0.0f)
        addVertex(consumer, matrix, v3, 1.0f, 0.0f)
        addVertex(consumer, matrix, v4, 1.0f, 1.0f)

        poseStack.popPose()
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight)
    }

    private fun addVertex(c: VertexConsumer, m: Matrix4f, pos: Vec3, u: Float, v: Float) {
        c.addVertex(m, pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat())
            .setColor(255, 255, 255, 255)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(0.0f, 1.0f, 0.0f)
    }

    override fun getTextureLocation(entity: MechaLaserBoltEntity): ResourceLocation = TEXTURE
}
