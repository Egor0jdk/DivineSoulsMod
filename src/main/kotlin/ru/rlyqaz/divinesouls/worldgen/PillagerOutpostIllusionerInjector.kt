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
import net.minecraft.world.entity.monster.Illusioner
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.structure.BuiltinStructures
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.phys.AABB
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

            if (!allLoaded) continue

            // Проверяем, нет ли уже Иллюзора в пределах этого аванпоста
            val outpostBox = AABB(
                task.minX.toDouble(), task.minY.toDouble(), task.minZ.toDouble(),
                task.maxX.toDouble(), task.maxY.toDouble() + 16.0, task.maxZ.toDouble()
            )
            if (level.getEntitiesOfClass(Illusioner::class.java, outpostBox).isNotEmpty()) {
                savedData.markProcessed(task.outpostOrigin)
                continue
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
                    savedData.markProcessed(task.outpostOrigin)
                    DivineSoulsMod.LOGGER.info("Divine Souls: Spawned Illusioner at Pillager Outpost top room $topRoomPos")
                }
            }
        }
    }

    private fun findTopTowerRoom(level: ServerLevel, task: PendingOutpost): BlockPos? {
        // Ищем сверху вниз по всей площади башни: пол из досок/брёвен/булыжника аванпоста, 2 блока воздуха и крышу шатра сверху
        for (y in (task.maxY + 4) downTo (task.minY + 6)) {
            for (x in task.minX..task.maxX) {
                for (z in task.minZ..task.maxZ) {
                    val pos = BlockPos(x, y, z)
                    val floor = level.getBlockState(pos.below())
                    val isTowerFloor = floor.`is`(Blocks.DARK_OAK_PLANKS) ||
                            floor.`is`(Blocks.DARK_OAK_LOG) ||
                            floor.`is`(Blocks.COBBLESTONE) ||
                            floor.`is`(Blocks.MOSSY_COBBLESTONE)
                    if (!isTowerFloor) continue

                    if (!level.getBlockState(pos).isAir || !level.getBlockState(pos.above()).isAir) continue

                    var hasRoof = false
                    for (ry in 2..7) {
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
