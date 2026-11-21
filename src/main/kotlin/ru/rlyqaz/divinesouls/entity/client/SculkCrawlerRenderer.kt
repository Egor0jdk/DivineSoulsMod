package ru.rlyqaz.divinesouls.entity.client

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import org.joml.Matrix4f
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.SculkCrawlerEntity
import kotlin.math.cos
import kotlin.math.sin

class SculkCrawlerRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<SculkCrawlerEntity, SculkCrawlerModel>(
        context,
        SculkCrawlerModel(context.bakeLayer(SculkCrawlerModel.LAYER_LOCATION)),
        0.75f
    ) {

    companion object {
        private val TEXTURE = ResourceLocation.fromNamespaceAndPath(
            DivineSoulsMod.MOD_ID,
            "textures/entity/sculk_crawler.png"
        )
        // Используем ванильную светящуюся текстуру скалка (или можешь заменить на свою текстуру кольца)
        private val RING_TEXTURE = ResourceLocation.withDefaultNamespace(
            "textures/block/sculk_catalyst_top_bloom.png"
        )
    }

    override fun getTextureLocation(entity: SculkCrawlerEntity): ResourceLocation = TEXTURE

    override fun render(
        entity: SculkCrawlerEntity,
        entityYaw: Float,
        partialTicks: Float,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int
    ) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight)

        val ticksLeft = entity.echoWaveTicks
        if (ticksLeft > 0) {
            renderEchoRing(ticksLeft, partialTicks, poseStack, buffer)
        }
    }

    private fun renderEchoRing(
        ticksLeft: Int,
        partialTicks: Float,
        poseStack: PoseStack,
        buffer: MultiBufferSource
    ) {
        val totalTicks = 14.0f
        val elapsed = (totalTicks - ticksLeft + partialTicks).coerceIn(0.0f, totalTicks)
        val progress = elapsed / totalTicks

        // Плавное расширение от 0.8 до 13.5 блоков с замедлением к краю
        val eased = 1.0f - (1.0f - progress) * (1.0f - progress)
        val outerRadius = 0.8f + eased * 12.7f
        val ringWidth = 0.45f * (1.0f - progress * 0.4f)
        val innerRadius = (outerRadius - ringWidth).coerceAtLeast(0.1f)

        // Плавное затухание прозрачности к концу волны
        val alpha = ((1.0f - progress) * 0.85f).coerceIn(0.0f, 1.0f)
        if (alpha <= 0.02f) return

        poseStack.pushPose()
        // Поднимаем обруч на 0.06 блока над полом, чтобы не мерцал с блоками земли
        poseStack.translate(0.0, 0.06, 0.0)

        val consumer: VertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(RING_TEXTURE))
        val matrix: Matrix4f = poseStack.last().pose()
        val segments = 48

        val r = 0.12f
        val g = 0.90f
        val b = 0.95f

        for (i in 0 until segments) {
            val a1 = (i.toFloat() / segments) * Mth.TWO_PI
            val a2 = ((i + 1).toFloat() / segments) * Mth.TWO_PI

            val cos1 = cos(a1)
            val sin1 = sin(a1)
            val cos2 = cos(a2)
            val sin2 = sin(a2)

            val x1In = cos1 * innerRadius
            val z1In = sin1 * innerRadius
            val x1Out = cos1 * outerRadius
            val z1Out = sin1 * outerRadius

            val x2In = cos2 * innerRadius
            val z2In = sin2 * innerRadius
            val x2Out = cos2 * outerRadius
            val z2Out = sin2 * outerRadius

            // Двусторонний квад (виден и сверху, и снизу)
            addVertex(consumer, matrix, x1In, 0.0f, z1In, 0.0f, 0.0f, r, g, b, alpha)
            addVertex(consumer, matrix, x1Out, 0.0f, z1Out, 0.0f, 1.0f, r, g, b, alpha)
            addVertex(consumer, matrix, x2Out, 0.0f, z2Out, 1.0f, 1.0f, r, g, b, alpha)
            addVertex(consumer, matrix, x2In, 0.0f, z2In, 1.0f, 0.0f, r, g, b, alpha)
        }

        poseStack.popPose()
    }

    private fun addVertex(
        consumer: VertexConsumer,
        matrix: Matrix4f,
        x: Float,
        y: Float,
        z: Float,
        u: Float,
        v: Float,
        r: Float,
        g: Float,
        b: Float,
        a: Float
    ) {
        consumer.addVertex(matrix, x, y, z)
            .setColor(r, g, b, a)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(0.0f, 1.0f, 0.0f)
    }
}
