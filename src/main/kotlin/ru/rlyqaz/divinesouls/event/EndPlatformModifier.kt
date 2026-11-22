package ru.rlyqaz.divinesouls.event

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent

object EndPlatformModifier {

    @SubscribeEvent
    fun onPlayerChangedDimension(event: PlayerEvent.PlayerChangedDimensionEvent) {
        if (event.to == Level.END) {
            val player = event.entity
            val level = player.level() as? ServerLevel ?: return

            // Ванильная платформа очищает X: 98..102, Z: -2..2, Y: 49..51 при каждом входе в Энд
            val startX = 98
            val endX = 102
            val startZ = -2
            val endZ = 2
            val platformY = 49

            // 1. ОБСИДИАНОВЫЙ БОРТИК СНАРУЖИ ПЛАТФОРМЫ СО ЩЕЛЬЮ (X: 98..103, Z: -3..3 на Y = 50)
            for (x in startX..(endX + 1)) {
                for (z in (startZ - 1)..(endZ + 1)) {
                    val isOuterWall = (x == endX + 1) || (z == startZ - 1) || (z == endZ + 1)
                    if (isOuterWall) {
                        level.setBlockAndUpdate(BlockPos(x, platformY + 1, z), Blocks.OBSIDIAN.defaultBlockState())
                    }
                }
            }

            // 2. ШИРОКАЯ ЛЕСТНИЦА ВВЕРХ НА 6 БЛОКОВ (X от 97 до 92)
            var currentX = startX - 1
            var currentY = platformY

            for (step in 1..6) {
                for (z in startZ..endZ) {
                    val stepPos = BlockPos(currentX, currentY, z)

                    level.setBlockAndUpdate(stepPos, Blocks.OBSIDIAN.defaultBlockState())

                    if (z == startZ || z == endZ) {
                        level.setBlockAndUpdate(stepPos.above(), Blocks.OBSIDIAN.defaultBlockState())
                    }

                    for (h in 1..3) {
                        val airPos = stepPos.above(h)
                        if (z > startZ && z < endZ) {
                            level.setBlockAndUpdate(airPos, Blocks.AIR.defaultBlockState())
                        }
                    }
                }
                currentX--
                currentY++
            }

            // 3. ШИРОКИЙ ПРЯМОЙ МОСТ ДО ОСТРОВА
            while (currentX > 30) {
                var hitIsland = false

                for (z in startZ..endZ) {
                    if (level.getBlockState(BlockPos(currentX, currentY, z)).`is`(Blocks.END_STONE)) {
                        hitIsland = true
                        break
                    }
                }

                if (hitIsland) {
                    break
                }

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
