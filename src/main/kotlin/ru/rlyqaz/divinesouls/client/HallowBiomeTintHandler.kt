package ru.rlyqaz.divinesouls.client

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import net.minecraft.client.Minecraft
import net.minecraft.client.color.block.BlockColor
import net.minecraft.client.renderer.BiomeColors
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.FoliageColor
import net.minecraft.world.level.GrassColor
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.block.Blocks
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent
import ru.rlyqaz.divinesouls.DivineSoulsMod

@EventBusSubscriber(modid = DivineSoulsMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = [Dist.CLIENT])
object HallowBiomeTintHandler {

    @Volatile
    var clientHardmode: Boolean = false

    private const val HALLOW_GRASS_TINT = 0xF48FB1
    private const val HALLOW_FOLIAGE_TINT = 0xF8BBD0

    private val blendCache = ThreadLocal.withInitial { Long2IntOpenHashMap(4096) }
    private val cachedRadiusForThread = ThreadLocal.withInitial { -1 }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onRegisterBlockColors(event: RegisterColorHandlersEvent.Block) {
        val grassBlocks = listOf(
            Blocks.GRASS_BLOCK,
            Blocks.SHORT_GRASS,
            Blocks.TALL_GRASS,
            Blocks.FERN,
            Blocks.LARGE_FERN,
            Blocks.PINK_PETALS
        )

        val grassColor = BlockColor { _, level: BlockAndTintGetter?, pos: BlockPos?, _ ->
            if (level == null || pos == null) {
                GrassColor.getDefaultColor()
            } else {
                val baseColor = BiomeColors.getAverageGrassColor(level, pos)
                if (!clientHardmode) {
                    baseColor
                } else {
                    val weight = getHallowWeight(pos.x, pos.y, pos.z)
                    if (weight <= 0f) baseColor else lerpColor(baseColor, HALLOW_GRASS_TINT, weight)
                }
            }
        }
        for (block in grassBlocks) {
            event.register(grassColor, block)
        }

        val foliageBlocks = listOf(
            Blocks.OAK_LEAVES,
            Blocks.BIRCH_LEAVES,
            Blocks.DARK_OAK_LEAVES,
            Blocks.VINE
        )

        val foliageColor = BlockColor { _, level: BlockAndTintGetter?, pos: BlockPos?, _ ->
            if (level == null || pos == null) {
                FoliageColor.getDefaultColor()
            } else {
                val baseColor = BiomeColors.getAverageFoliageColor(level, pos)
                if (!clientHardmode) {
                    baseColor
                } else {
                    val weight = getHallowWeight(pos.x, pos.y, pos.z)
                    if (weight <= 0f) baseColor else lerpColor(baseColor, HALLOW_FOLIAGE_TINT, weight)
                }
            }
        }
        for (block in foliageBlocks) {
            event.register(foliageColor, block)
        }
    }

    private fun getHallowWeight(x: Int, y: Int, z: Int): Float {
        val mc = Minecraft.getInstance()
        val clientLevel = mc.level ?: return 0.0f
        // Читаем реальную настройку Biome Blend из меню графики (0..7 блоков)
        val radius = mc.options.biomeBlendRadius().get().coerceAtLeast(0)

        val cache = blendCache.get()
        if (cachedRadiusForThread.get() != radius) {
            cache.clear()
            cachedRadiusForThread.set(radius)
        }

        val key = (x.toLong() shl 32) xor (z.toLong() and 0xFFFFFFFFL)
        if (cache.containsKey(key)) {
            return cache.get(key) / 1000.0f
        }

        val mutablePos = BlockPos.MutableBlockPos()
        if (radius == 0) {
            mutablePos.set(x, y, z)
            val biome = clientLevel.getBiome(mutablePos)
            val isHallow = biome.`is`(Biomes.PLAINS) || biome.`is`(Biomes.SUNFLOWER_PLAINS) || biome.`is`(Biomes.MEADOW)
            val weightInt = if (isHallow) 1000 else 0
            cache.put(key, weightInt)
            return weightInt / 1000.0f
        }

        var hallowHits = 0
        var totalSamples = 0
        for (dx in -radius..radius) {
            for (dz in -radius..radius) {
                totalSamples++
                mutablePos.set(x + dx, y, z + dz)
                val biome = clientLevel.getBiome(mutablePos)
                if (biome.`is`(Biomes.PLAINS) || biome.`is`(Biomes.SUNFLOWER_PLAINS) || biome.`is`(Biomes.MEADOW)) {
                    hallowHits++
                }
            }
        }

        val weightInt = (hallowHits * 1000) / totalSamples
        if (cache.size > 32768) cache.clear()
        cache.put(key, weightInt)
        return weightInt / 1000.0f
    }

    private fun lerpColor(c1: Int, c2: Int, t: Float): Int {
        val r1 = (c1 shr 16) and 0xFF
        val g1 = (c1 shr 8) and 0xFF
        val b1 = c1 and 0xFF

        val r2 = (c2 shr 16) and 0xFF
        val g2 = (c2 shr 8) and 0xFF
        val b2 = c2 and 0xFF

        val r = (r1 + (r2 - r1) * t).toInt().coerceIn(0, 255)
        val g = (g1 + (g2 - g1) * t).toInt().coerceIn(0, 255)
        val b = (b1 + (b2 - b1) * t).toInt().coerceIn(0, 255)

        return (r shl 16) or (g shl 8) or b
    }

    @EventBusSubscriber(modid = DivineSoulsMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = [Dist.CLIENT])
    object ClientLogoutReset {
        @SubscribeEvent
        fun onClientLogout(@Suppress("UNUSED_PARAMETER") event: ClientPlayerNetworkEvent.LoggingOut) {
            clientHardmode = false
            blendCache.get().clear()
        }
    }
}
