package ru.rlyqaz.divinesouls.util

import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import ru.rlyqaz.divinesouls.DivineSoulsMod

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.21.1 NeoForge)
 * КЛАСС: ModTags (Кастомные теги блоков и предметов)
 * ====================================================================
 */
object ModTags {

    object Blocks {
        // Пример создания кастомного тега для твоих блоков Края (если понадобится в будущем)
        val END_LOGS: TagKey<Block> = createTag("end_logs")

        private fun createTag(name: String): TagKey<Block> {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, name))
        }
    }

    object Items {
        // Пример создания кастомного тега для предметов
        val TRANSFORMABLE_ITEMS: TagKey<Item> = createTag("transformable_items")

        private fun createTag(name: String): TagKey<Item> {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, name))
        }
    }
}
