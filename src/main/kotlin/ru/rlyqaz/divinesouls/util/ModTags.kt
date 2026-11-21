package ru.rlyqaz.divinesouls.util

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import ru.rlyqaz.divinesouls.DivineSoulsMod

object ModTags {

    object Blocks {
        val END_LOGS: TagKey<Block> = createTag("end_logs")
        val SCULK_SURFACE_BLOCKS: TagKey<Block> = createTag("sculk_surface_blocks")

        private fun createTag(name: String): TagKey<Block> {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, name))
        }
    }

    object Items {
        val MAGIC_STAFFS: TagKey<Item> = createTag("magic_staffs")
        val SOULS: TagKey<Item> = createTag("souls")
        val STAT_UPGRADES: TagKey<Item> = createTag("stat_upgrades")

        private fun createTag(name: String): TagKey<Item> {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, name))
        }
    }
}
