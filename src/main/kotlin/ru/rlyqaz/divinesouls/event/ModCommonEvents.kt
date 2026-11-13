package ru.rlyqaz.divinesouls.event

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.monster.Ghast
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.PickaxeItem
import net.minecraft.world.item.Tiers
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent
import net.neoforged.neoforge.event.level.BlockEvent
import ru.rlyqaz.divinesouls.item.ModItems

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.21.1 NeoForge)
 * КЛАСС: ModCommonEvents (Сборник общих игровых механик и дропа)
 * ====================================================================
 */
object ModCommonEvents {

    // 1. СБАЛАНСИРОВАННЫЙ ДРОП ДУШИ ПОЛЁТА С ГАСТОВ
    @SubscribeEvent
    fun onGhastDeath(event: LivingDeathEvent) {
        val entity = event.entity
        val level = entity.level()

        if (!level.isClientSide && entity is Ghast) {
            // Выставляем комфортный шанс 65% на выпадение строго 1 Души Полёта
            if (entity.random.nextFloat() < 0.65f) {
                entity.spawnAtLocation(ItemStack(ModItems.SOUL_OF_FLIGHT.get(), 1))
            }
        }
    }

    // 2. ХАРДКОРНОЕ РАЗРУШЕНИЕ ПЛАЧУЩЕГО ОБСИДИАНА АЛМАЗНОЙ КИРКОЙ
    @SubscribeEvent
    fun onCryingObsidianBreak(event: BlockEvent.BreakEvent) {
        val level = event.level
        val pos = event.pos
        val state = event.state
        val player = event.player

        if (!level.isClientSide && state.`is`(Blocks.CRYING_OBSIDIAN)) {
            if (player.isCreative) return

            val mainHandItem = player.mainHandItem
            val item = mainHandItem.item

            if (item is PickaxeItem && item.tier == Tiers.DIAMOND) {
                event.isCanceled = true //

                val serverLevel = level as ServerLevel
                serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 3) //

                // Эффект ломания блока
                serverLevel.levelEvent(2001, pos, Block.getId(state))

                // Штраф прочности кирки
                mainHandItem.hurtAndBreak(1, player, EquipmentSlot.MAINHAND)
            }
        }
    }

    // Размножение Дёрна Края костной мукой (как Нилий в Незере)
    @SubscribeEvent
    fun onBonemealEndStone(event: net.neoforged.neoforge.event.entity.player.BonemealEvent) {
        val level = event.level
        val pos = event.pos
        val state = event.state

        if (state.`is`(net.minecraft.world.level.block.Blocks.END_STONE)) {
            // Проверяем, есть ли хотя бы один блок Дёрна Края рядом (север, юг, запад, восток)
            val hasNeighborEndGrass = net.minecraft.core.Direction.Plane.HORIZONTAL.any { dir ->
                level.getBlockState(pos.relative(dir)).`is`(ru.rlyqaz.divinesouls.block.ModBlocks.END_GRASS_BLOCK.get())
            }

            if (hasNeighborEndGrass) {
                if (!level.isClientSide) {
                    level.setBlockAndUpdate(pos, ru.rlyqaz.divinesouls.block.ModBlocks.END_GRASS_BLOCK.get().defaultBlockState())
                }
                // Помечаем событие как успешное (костная мука потратится, появятся зелёные частицы)
                event.isCanceled = true
                event.setSuccessful(true)
            }
        }
    }
}
