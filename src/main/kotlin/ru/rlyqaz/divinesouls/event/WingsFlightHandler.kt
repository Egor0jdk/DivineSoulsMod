package ru.rlyqaz.divinesouls.event

import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent
import net.neoforged.neoforge.event.tick.PlayerTickEvent
import ru.rlyqaz.divinesouls.item.ModItems

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.21.1 NeoForge)
 * КЛАСС: WingsFlightHandler (Физика и логика полёта Крыльев Ветра)
 * ЛОГИКА: Тот самый первый рабочий полет + блокировка урона через ивент
 * ====================================================================
 */
object WingsFlightHandler {

    private val flightTicksMap = java.util.HashMap<java.util.UUID, Int>()
    private const val MAX_FLIGHT_TIME = 40

    // ТОТ САМЫЙ ПЕРВЫЙ, ИДЕАЛЬНО РАБОЧИЙ ПОЛЁТ И ПАРАШЮТ
    @SubscribeEvent
    fun onPlayerTick(event: PlayerTickEvent.Post) {
        val player: Player = event.entity
        val uuid = player.uuid

        // Физику обрабатываем строго на клиенте, как в самом первом рабочем коде
        if (player.level().isClientSide && hasWingsInInventory(player)) {

            if (player.onGround() || player.isInWater || player.isInLava) {
                flightTicksMap[uuid] = MAX_FLIGHT_TIME
            } else {
                val isJumping = net.minecraft.client.Minecraft.getInstance().options.keyJump.isDown

                if (isJumping) {
                    val remainingTicks = flightTicksMap.getOrDefault(uuid, 0)

                    if (remainingTicks > 0) {
                        val currentMotion: Vec3 = player.deltaMovement

                        // УСКОРЯЕМ ВЗЛЁТ: Подняли с 0.15 до 0.22 для сочной динамики!
                        player.deltaMovement = Vec3(currentMotion.x, 0.22, currentMotion.z)

                        flightTicksMap[uuid] = remainingTicks - 1
                    } else {
                        // Твой идеальный плавный парашют
                        val currentMotion: Vec3 = player.deltaMovement
                        if (currentMotion.y < -0.08) {
                            player.deltaMovement = Vec3(currentMotion.x, -0.08, currentMotion.z)
                        }
                    }
                }
            }
        }
    }

    // ЖЕЛЕЗОБЕТОННЫЙ ФИКС УРОНА НА СЕРВЕРЕ 1.21.1: Просто отменяем урон от падения!
    @SubscribeEvent
    fun onPlayerHurt(event: LivingIncomingDamageEvent) {
        val entity = event.getEntity()
        if (entity is Player && hasWingsInInventory(entity)) {
            // Если тип урона — падение с высоты (FALL), намертво глушим его!
            if (event.getSource().`is`(net.minecraft.world.damagesource.DamageTypes.FALL)) {
                event.isCanceled = true
            }
        }
    }

    private fun hasWingsInInventory(player: Player): Boolean {
        return player.inventory.contains(net.minecraft.world.item.ItemStack(ModItems.WINGS_OF_WIND.get()))
    }
}
