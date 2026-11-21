package ru.rlyqaz.divinesouls.worldgen

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.BlockTags
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.levelgen.Heightmap
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent
import net.neoforged.neoforge.event.level.ChunkEvent
import ru.rlyqaz.divinesouls.block.ModBlocks
import ru.rlyqaz.divinesouls.mana.ModAttachments
import kotlin.math.cos
import kotlin.math.sin

object SculkInfectionManager {

    private const val SPAWN_SAFE_RADIUS_SQ = 220.0 * 220.0

    private fun isWorldHardmode(level: ServerLevel): Boolean {
        val endLevel = level.server.getLevel(Level.END)
        return endLevel?.dragonFight?.hasPreviouslyKilledDragon() ?: false
    }

    /**
     * Детерминированный 2D-шум: определяет, попадает ли колонка (blockX, blockZ) в скалк-биом.
     */
    fun isSculkBiomeAt(seed: Long, blockX: Int, blockZ: Int): Boolean {
        val distSq = blockX.toDouble() * blockX.toDouble() + blockZ.toDouble() * blockZ.toDouble()
        if (distSq.compareTo(SPAWN_SAFE_RADIUS_SQ) == -1) return false

        // Обрезаем seed по модулю, чтобы избежать переполнения точности Double в sin/cos
        val seedOffset = ((seed xor 0x5DEECE66DL) % 100000L).toDouble() * 0.05
        val x = blockX.toDouble() * 0.005 + seedOffset
        val z = blockZ.toDouble() * 0.005 - seedOffset

        val wave1 = sin(x) * cos(z)
        val wave2 = sin(x * 2.15 + 1.3) * sin(z * 1.95 - 0.8) * 0.45
        val wave3 = cos(x * 4.3 - z * 3.7) * 0.2
        val combined = wave1 + wave2 + wave3

        return combined.compareTo(0.55) == 1
    }

    @SubscribeEvent
    fun onChunkLoad(event: ChunkEvent.Load) {
        val level = event.level as? ServerLevel ?: return
        if (level.dimension() != Level.OVERWORLD) return
        if (!isWorldHardmode(level)) return

        val chunk = event.chunk as? LevelChunk ?: return
        val attachment = ModAttachments.SCULK_CHECKED.get()
        if (chunk.getData(attachment)) return

        chunk.setData(attachment, true)
        chunk.isUnsaved = true

        // Выполняем на следующем тике сервера, когда чанк на 100% встал в мир (убирает deadlock!)
        level.server.execute {
            if (level.hasChunk(chunk.pos.x, chunk.pos.z)) {
                infectChunkSurface(level, chunk)
            }
        }
    }

    @SubscribeEvent
    fun onDragonDeath(event: LivingDeathEvent) {
        if (event.entity !is EnderDragon) return
        val serverLevel = event.entity.level() as? ServerLevel ?: return
        val overworld = serverLevel.server.overworld()

        serverLevel.server.execute {
            infectLoadedChunksAroundPlayers(overworld)
        }
    }

    fun infectLoadedChunksAroundPlayers(level: ServerLevel) {
        if (level.dimension() != Level.OVERWORLD) return
        val attachment = ModAttachments.SCULK_CHECKED.get()

        for (player in level.players()) {
            val centerChunk = player.chunkPosition()
            for (dx in -6..6) {
                for (dz in -6..6) {
                    val cx = centerChunk.x + dx
                    val cz = centerChunk.z + dz
                    if (level.hasChunk(cx, cz)) {
                        val chunk = level.getChunk(cx, cz)
                        if (!chunk.getData(attachment)) {
                            chunk.setData(attachment, true)
                            chunk.isUnsaved = true
                            infectChunkSurface(level, chunk)
                        }
                    }
                }
            }
        }
    }

    private fun infectChunkSurface(level: ServerLevel, chunk: LevelChunk) {
        val seed = level.seed
        val chunkPos: ChunkPos = chunk.pos
        val startX = chunkPos.minBlockX
        val startZ = chunkPos.minBlockZ
        val mutablePos = BlockPos.MutableBlockPos()
        var changedAny = false

        for (localX in 0..15) {
            val worldX = startX + localX
            for (localZ in 0..15) {
                val worldZ = startZ + localZ
                if (!isSculkBiomeAt(seed, worldX, worldZ)) continue

                val topY = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, localX, localZ)
                val minScanY = (topY - 12).coerceAtLeast(level.minBuildHeight + 1)
                val maxScanY = (topY + 4).coerceAtMost(level.maxBuildHeight - 1)

                var dirtConvertedCount = 0

                for (y in maxScanY downTo minScanY) {
                    mutablePos.set(worldX, y, worldZ)
                    val state = chunk.getBlockState(mutablePos)
                    if (state.isAir) continue

                    // 1. Листва деревьев -> Скалковая листва
                    if (state.`is`(BlockTags.LEAVES)) {
                        var newLeaves = ModBlocks.SCULK_LEAVES.get().defaultBlockState()
                        if (state.hasProperty(LeavesBlock.DISTANCE) && newLeaves.hasProperty(LeavesBlock.DISTANCE)) {
                            newLeaves = newLeaves.setValue(LeavesBlock.DISTANCE, state.getValue(LeavesBlock.DISTANCE))
                        }
                        if (state.hasProperty(LeavesBlock.PERSISTENT) && newLeaves.hasProperty(LeavesBlock.PERSISTENT)) {
                            newLeaves = newLeaves.setValue(LeavesBlock.PERSISTENT, state.getValue(LeavesBlock.PERSISTENT))
                        }
                        chunk.setBlockState(mutablePos, newLeaves, false)
                        changedAny = true
                        continue
                    }

                    // 2. Брёвна деревьев -> Скалковые брёвна
                    if (state.`is`(BlockTags.LOGS)) {
                        var newLog = ModBlocks.SCULK_LOG.get().defaultBlockState()
                        if (state.hasProperty(RotatedPillarBlock.AXIS) && newLog.hasProperty(RotatedPillarBlock.AXIS)) {
                            newLog = newLog.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS))
                        }
                        chunk.setBlockState(mutablePos, newLog, false)
                        changedAny = true
                        continue
                    }

                    // 3. Цветы -> Скалковый цветок
                    if (state.`is`(BlockTags.FLOWERS)) {
                        chunk.setBlockState(mutablePos, ModBlocks.SCULK_FLOWER.get().defaultBlockState(), false)
                        changedAny = true
                        continue
                    }

                    // 4. Трава, папоротники и кусты -> Скалковая трава
                    if (isReplaceableFoliage(state)) {
                        chunk.setBlockState(mutablePos, ModBlocks.SCULK_GRASS.get().defaultBlockState(), false)
                        changedAny = true
                        continue
                    }

                    // 5. Верхний дёрн -> Скалковый дёрн
                    if (state.`is`(Blocks.GRASS_BLOCK) || state.`is`(Blocks.PODZOL) || state.`is`(Blocks.MYCELIUM)) {
                        chunk.setBlockState(mutablePos, ModBlocks.SCULK_GRASS_BLOCK.get().defaultBlockState(), false)
                        dirtConvertedCount = 0
                        changedAny = true
                        continue
                    }

                    // 6. До 3 блоков земли под дёрном -> Ванильный блок скалка (minecraft:sculk)
                    if (state.`is`(Blocks.DIRT) || state.`is`(Blocks.COARSE_DIRT) || state.`is`(Blocks.ROOTED_DIRT)) {
                        if (dirtConvertedCount.compareTo(3) == -1) {
                            chunk.setBlockState(mutablePos, Blocks.SCULK.defaultBlockState(), false)
                            dirtConvertedCount++
                            changedAny = true
                        } else {
                            break
                        }
                        continue
                    }

                    if (state.isSolidRender(chunk, mutablePos)) {
                        break
                    }
                }
            }
        }

        if (changedAny) {
            chunk.isUnsaved = true
            // Переотправляем обновлённый чанк ближайшим игрокам одним пакетом (без лагов!)
            val packet = net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket(
                chunk, level.lightEngine, null, null
            )
            for (player in level.players()) {
                if (player.chunkPosition().getChessboardDistance(chunkPos) <= level.server.playerList.viewDistance) {
                    player.connection.send(packet)
                }
            }
        }
    }

    private fun isReplaceableFoliage(state: BlockState): Boolean {
        return state.`is`(Blocks.SHORT_GRASS) ||
                state.`is`(Blocks.TALL_GRASS) ||
                state.`is`(Blocks.FERN) ||
                state.`is`(Blocks.LARGE_FERN) ||
                state.`is`(Blocks.DEAD_BUSH) ||
                state.`is`(Blocks.SWEET_BERRY_BUSH)
    }

    @SubscribeEvent
    fun onRegisterCommands(event: net.neoforged.neoforge.event.RegisterCommandsEvent) {
        event.dispatcher.register(
            net.minecraft.commands.Commands.literal("findsculk")
                .requires { it.hasPermission(2) }
                .executes { ctx ->
                    val source = ctx.source
                    val level = source.level
                    val startPos = BlockPos.containing(source.position)
                    val seed = level.seed

                    var foundPos: BlockPos? = null
                    // Ищем по спирали с шагом 32 блока в радиусе до 4000 блоков
                    search@ for (radius in 32..4000 step 32) {
                        for (dx in -radius..radius step 32) {
                            for (dz in listOf(-radius, radius)) {
                                val checkX = startPos.x + dx
                                val checkZ = startPos.z + dz
                                if (isSculkBiomeAt(seed, checkX, checkZ)) {
                                    foundPos = BlockPos(checkX, 90, checkZ)
                                    break@search
                                }
                            }
                        }
                        for (dz in -radius..radius step 32) {
                            for (dx in listOf(-radius, radius)) {
                                val checkX = startPos.x + dx
                                val checkZ = startPos.z + dz
                                if (isSculkBiomeAt(seed, checkX, checkZ)) {
                                    foundPos = BlockPos(checkX, 90, checkZ)
                                    break@search
                                }
                            }
                        }
                    }

                    if (foundPos != null) {
                        val tpCmd = "/tp @s ${foundPos.x} ~ ${foundPos.z}"
                        val msg = net.minecraft.network.chat.Component.literal(
                            "Nearest Sculk Biome: [${foundPos.x}, ${foundPos.z}] (click to teleport)"
                        ).withStyle { style ->
                            style.withColor(net.minecraft.ChatFormatting.AQUA)
                                .withClickEvent(
                                    net.minecraft.network.chat.ClickEvent(
                                        net.minecraft.network.chat.ClickEvent.Action.SUGGEST_COMMAND,
                                        tpCmd
                                    )
                                )
                        }
                        source.sendSuccess({ msg }, false)
                    } else {
                        source.sendFailure(net.minecraft.network.chat.Component.literal("No skulk biome found within a 4000-block radius"))
                    }
                    1
                }
        )
    }
}
