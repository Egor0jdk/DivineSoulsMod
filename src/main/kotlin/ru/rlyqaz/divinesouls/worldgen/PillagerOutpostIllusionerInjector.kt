package ru.rlyqaz.divinesouls.worldgen

import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.LongTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.structure.BuiltinStructures
import net.minecraft.world.level.saveddata.SavedData
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.EventHooks
import net.neoforged.neoforge.event.level.ChunkEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import ru.rlyqaz.divinesouls.DivineSoulsMod
import java.util.concurrent.ConcurrentLinkedQueue

object PillagerOutpostIllusionerInjector {

    private data class PendingOutpost(
        val level: ServerLevel,
        val outpostOrigin: BlockPos,
        val minX: Int,
        val minY: Int,
        val minZ: Int,
        val maxX: Int,
        val maxY: Int,
        val maxZ: Int
    )

    private val pendingOutposts = ConcurrentLinkedQueue<PendingOutpost>()

    fun clearQueue() {
        pendingOutposts.clear()
    }

    @SubscribeEvent
    fun onChunkLoad(event: ChunkEvent.Load) {
        if (!event.isNewChunk) return
        val level = event.level as? ServerLevel ?: return
        if (level.dimension() != Level.OVERWORLD) return

        val chunkPos = event.chunk.pos
        val checkPos = chunkPos.getMiddleBlockPosition(64)

        val structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
        val outpostStructure = structureRegistry.get(BuiltinStructures.PILLAGER_OUTPOST) ?: return

        val structureStart = level.structureManager().getStructureWithPieceAt(checkPos, outpostStructure)
        if (!structureStart.isValid) return

        val bb = structureStart.boundingBox
        val origin = bb.center
        val savedData = OutpostSavedData.get(level)
        if (savedData.isProcessed(origin)) return

        pendingOutposts.add(
            PendingOutpost(
                level = level,
                outpostOrigin = origin,
                minX = bb.minX(),
                minY = bb.minY(),
                minZ = bb.minZ(),
                maxX = bb.maxX(),
                maxY = bb.maxY(),
                maxZ = bb.maxZ()
            )
        )
    }

    @SubscribeEvent
    fun onServerTick(@Suppress("UNUSED_PARAMETER") event: ServerTickEvent.Post) {
        while (pendingOutposts.isNotEmpty()) {
            val task = pendingOutposts.poll() ?: break
            val level = task.level
            val savedData = OutpostSavedData.get(level)
            if (savedData.isProcessed(task.outpostOrigin)) continue

            val minChunkX = task.minX shr 4
            val maxChunkX = task.maxX shr 4
            val minChunkZ = task.minZ shr 4
            val maxChunkZ = task.maxZ shr 4

            var allLoaded = true
            for (cx in minChunkX..maxChunkX) {
                for (cz in minChunkZ..maxChunkZ) {
                    if (!level.hasChunk(cx, cz)) {
                        allLoaded = false
                        break
                    }
                }
                if (!allLoaded) break
            }

            if (!allLoaded) {
                pendingOutposts.add(task)
                break
            }

            val topRoomPos = findTopTowerRoom(level, task)
            if (topRoomPos != null) {
                val illusioner = EntityType.ILLUSIONER.create(level)
                if (illusioner != null) {
                    illusioner.moveTo(
                        topRoomPos.x + 0.5,
                        topRoomPos.y.toDouble(),
                        topRoomPos.z + 0.5,
                        level.random.nextFloat() * 360.0f,
                        0.0f
                    )
                    EventHooks.finalizeMobSpawn(
                        illusioner,
                        level,
                        level.getCurrentDifficultyAt(topRoomPos),
                        MobSpawnType.STRUCTURE,
                        null
                    )
                    if (illusioner.mainHandItem.isEmpty) {
                        illusioner.setItemSlot(EquipmentSlot.MAINHAND, ItemStack(Items.BOW))
                    }
                    illusioner.setPersistenceRequired()
                    level.addFreshEntity(illusioner)
                    DivineSoulsMod.LOGGER.info("Divine Souls: Spawned Illusioner at Pillager Outpost top room $topRoomPos")
                }
            }

            savedData.markProcessed(task.outpostOrigin)
        }
    }

    /**
     * Находит самую верхнюю комнату/балкон башни аванпоста (пол + 2 блока воздуха + крыша сверху, чтобы не спавнить на самой макушке)
     */
    private fun findTopTowerRoom(level: ServerLevel, task: PendingOutpost): BlockPos? {
        val centerX = (task.minX + task.maxX) / 2
        val centerZ = (task.minZ + task.maxZ) / 2

        // Ищем сверху вниз внутри башни: пол, над ним 2 блока воздуха, а выше (на +3..+5) есть крыша шатра
        for (y in (task.maxY - 3) downTo (task.minY + 8)) {
            for (dx in -3..3) {
                for (dz in -3..3) {
                    val pos = BlockPos(centerX + dx, y, centerZ + dz)
                    val floor = level.getBlockState(pos.below())
                    if (!floor.isSolidRender(level, pos.below())) continue
                    if (!level.getBlockState(pos).isAir || !level.getBlockState(pos.above()).isAir) continue

                    // Проверяем, что над головой есть крыша башни (а не открытое небо на самом шпиле)
                    var hasRoof = false
                    for (ry in 2..5) {
                        if (!level.getBlockState(pos.above(ry)).isAir) {
                            hasRoof = true
                            break
                        }
                    }
                    if (hasRoof) {
                        return pos
                    }
                }
            }
        }
        return null
    }

    private class OutpostSavedData : SavedData() {
        private val processed = HashSet<Long>()

        fun isProcessed(pos: BlockPos): Boolean = processed.contains(pos.asLong())

        fun markProcessed(pos: BlockPos) {
            if (processed.add(pos.asLong())) {
                setDirty()
            }
        }

        override fun save(tag: CompoundTag, registries: HolderLookup.Provider): CompoundTag {
            val list = ListTag()
            for (v in processed) {
                list.add(LongTag.valueOf(v))
            }
            tag.put("ProcessedOutposts", list)
            return tag
        }

        companion object {
            private const val DATA_NAME = "divinesouls_outpost_illusioners"

            fun get(level: ServerLevel): OutpostSavedData {
                return level.dataStorage.computeIfAbsent(
                    Factory(::OutpostSavedData, ::load),
                    DATA_NAME
                )
            }

            private fun load(tag: CompoundTag, @Suppress("UNUSED_PARAMETER") registries: HolderLookup.Provider): OutpostSavedData {
                val data = OutpostSavedData()
                val list = tag.getList("ProcessedOutposts", Tag.TAG_LONG.toInt())
                for (item in list) {
                    if (item is LongTag) {
                        data.processed.add(item.asLong)
                    }
                }
                return data
            }
        }
    }
}
