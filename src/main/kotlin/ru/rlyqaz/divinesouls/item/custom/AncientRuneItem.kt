package ru.rlyqaz.divinesouls.item.custom

import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.portal.PortalShape

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.21.1 NeoForge)
 * КЛАСС: AncientRuneItem (Древняя Руна)
 * ЛОГИКА: Активация портала в Незер по клику на Обсидиан
 * ====================================================================
 */
class AncientRuneItem(properties: Properties) : Item(properties) {

    override fun useOn(context: UseOnContext): InteractionResult {
        val level = context.level
        val pos = context.clickedPos

        // Проверяем строго на серверной стороне
        if (level is ServerLevel) {
            val blockState = level.getBlockState(pos)

            if (blockState.`is`(Blocks.OBSIDIAN)) {
                val face = context.clickedFace
                val airPos = pos.relative(face)

                if (level.isEmptyBlock(airPos)) {
                    // В 1.21.1 вместо PortalSize используется PortalShape
                    var shape = PortalShape.findEmptyPortalShape(level, airPos, Direction.Axis.X)

                    if (shape.isEmpty) {
                        shape = PortalShape.findEmptyPortalShape(level, airPos, Direction.Axis.Z)
                    }

                    if (shape.isPresent) {
                        // В 1.21.1 метод создания блоков требует ServerLevel
                        shape.get().createPortalBlocks()

                        val player = context.player
                        if (player != null && !player.isCreative) {
                            context.itemInHand.shrink(1)
                        }
                        return InteractionResult.SUCCESS
                    }
                }
            }
        }
        return InteractionResult.PASS
    }
}
