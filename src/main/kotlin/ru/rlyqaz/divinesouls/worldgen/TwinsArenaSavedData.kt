package ru.rlyqaz.divinesouls.worldgen

import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.LongTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.saveddata.SavedData

class TwinsArenaSavedData : SavedData() {

    private val generatedFortresses = HashSet<Long>()

    fun isFortressProcessed(fortressStartPos: BlockPos): Boolean {
        return generatedFortresses.contains(fortressStartPos.asLong())
    }

    fun markFortressProcessed(fortressStartPos: BlockPos) {
        if (generatedFortresses.add(fortressStartPos.asLong())) {
            this.setDirty()
        }
    }

    override fun save(tag: CompoundTag, registries: HolderLookup.Provider): CompoundTag {
        val list = ListTag()
        for (posLong in generatedFortresses) {
            list.add(LongTag.valueOf(posLong))
        }
        tag.put("GeneratedFortresses", list)
        return tag
    }

    companion object {
        private const val DATA_NAME = "divinesouls_twins_arenas"

        fun get(level: ServerLevel): TwinsArenaSavedData {
            return level.dataStorage.computeIfAbsent(
                Factory(::TwinsArenaSavedData, ::load),
                DATA_NAME
            )
        }

        private fun load(tag: CompoundTag, @Suppress("UNUSED_PARAMETER") registries: HolderLookup.Provider): TwinsArenaSavedData {
            val data = TwinsArenaSavedData()
            val list = tag.getList("GeneratedFortresses", Tag.TAG_LONG.toInt())
            for (item in list) {
                if (item is LongTag) {
                    data.generatedFortresses.add(item.asLong)
                }
            }
            return data
        }
    }
}
