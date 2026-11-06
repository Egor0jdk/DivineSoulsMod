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
 * ЛОГИКА: Активация портала в Незер по клику на Обсидиан со звуком Энда
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
                    var shape = PortalShape.findEmptyPortalShape(level, airPos, Direction.Axis.X)

                    if (shape.isEmpty) {
                        shape = PortalShape.findEmptyPortalShape(level, airPos, Direction.Axis.Z)
                    }

                    if (shape.isPresent) {
                        shape.get().createPortalBlocks()

                        // БАГФИКС: Бахаем каноничный звук Энд-портала при успешной активации Незера!
                        level.playSound(
                            null,
                            airPos,
                            net.minecraft.sounds.SoundEvents.END_PORTAL_SPAWN,
                            net.minecraft.sounds.SoundSource.BLOCKS,
                            1.0f,
                            1.0f
                        )

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
