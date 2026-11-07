package ru.rlyqaz.divinesouls.event

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.Level
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent

object EndPlatformModifier {

    @SubscribeEvent
    fun onPlayerChangedDimension(event: PlayerEvent.PlayerChangedDimensionEvent) {
        if (event.to == Level.END) {
            val player = event.entity
            val level = player.level() as? ServerLevel ?: return

            // Координаты ванильной платформы 5x5 на Y: 49
            val startX = 98  // 100 - 2
            val endX = 102    // 100 + 2
            val startZ = -2   // 0 - 2
            val endZ = 2      // 0 + 2
            val platformY = 49

            // 1. ГЕНЕРИРУЕМ БОКОВЫЕ ОБСИДИАНОВЫЕ БОРТИКИ (Закрываем сторону +X)
            for (x in startX..endX) {
                for (z in startZ..endZ) {
                    if (x == endX || z == startZ || z == endZ) {
                        level.setBlockAndUpdate(BlockPos(x, platformY + 1, z), Blocks.OBSIDIAN.defaultBlockState())
                    }
                }
            }

            // 2. СТРОИМ ШИРОКУЮ ЛЕСТНИЦУ ВВЕРХ НА 6 БЛОКОВ (Всегда пробивает скалу!)
            var currentX = startX - 1
            var currentY = platformY

            for (step in 1..6) {
                for (z in startZ..endZ) {
                    val stepPos = BlockPos(currentX, currentY, z)

                    // Лестница нагло заменяет любой эндер-няк под собой обсидианом!
                    level.setBlockAndUpdate(stepPos, Blocks.OBSIDIAN.defaultBlockState())

                    // Бортики по краям лестницы
                    if (z == startZ || z == endZ) {
                        level.setBlockAndUpdate(stepPos.above(), Blocks.OBSIDIAN.defaultBlockState())
                    }

                    // ЖЕСТКИЙ RPG-ФИКС: Вырезаем туннель воздуха строго над ступенями (3 блока в высоту),
                    // чтобы игрок никогда не задохнулся в скале острова при подъёме!
                    for (h in 1..3) {
                        val airPos = stepPos.above(h)
                        // Прорубаем воздух по всей ширине прохода (между заборчиками)
                        if (z > startZ && z < endZ) {
                            level.setBlockAndUpdate(airPos, Blocks.AIR.defaultBlockState())
                        }
                    }
                }
                currentX--
                currentY++
            }

            // 3. СТРОИМ ШИРОКИЙ ПРЯМОЙ МОСТ (Останавливается строго при касании острова)
            while (currentX > 30) {
                var hitIsland = false

                // Проверяем передний край моста по всей ширине на наличие эндер-няка
                for (z in startZ..endZ) {
                    if (level.getBlockState(BlockPos(currentX, currentY, z)).`is`(Blocks.END_STONE)) {
                        hitIsland = true
                        break
                    }
                }

                // Как только мост коснулся острова — МГНОВЕННЫЙ СТОП! Остров не заменяется.
                if (hitIsland) {
                    break
                }

                // Строим секцию открытого моста
                for (z in startZ..endZ) {
                    val bridgePos = BlockPos(currentX, currentY, z)

                    level.setBlockAndUpdate(bridgePos, Blocks.OBSIDIAN.defaultBlockState())

                    if (z == startZ || z == endZ) {
                        level.setBlockAndUpdate(bridgePos.above(), Blocks.OBSIDIAN.defaultBlockState())
                    }
                }
                currentX--
            }
        }
    }
}
