package ru.rlyqaz.divinesouls.event

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.damagesource.DamageTypes
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.tick.PlayerTickEvent
import kotlin.math.sqrt

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.21.1 NeoForge)
 * КЛАСС: EndAuraHandler (Аура Пустоты Дальних Островов)
 * ЛОГИКА: Наносит урон и слепоту игроку, если он пытается сбежать
 *         на дальние острова Края до убийства Эндер-Дракона.
 * ====================================================================
 */
object EndAuraHandler {

    @SubscribeEvent
    fun onPlayerTick(event: PlayerTickEvent.Post) {
        val player = event.entity
        val level = player.level()

        // Работаем строго на стороне сервера, проверяем раз в секунду (20 тиков) для оптимизации
        if (!level.isClientSide && player is ServerPlayer && player.tickCount % 20 == 0 && !player.isCreative && !player.isSpectator) {

            // 1. Проверяем, что игрок находится именно в измерении Энда
            if (level.dimension() == net.minecraft.world.level.Level.END) {
                val serverLevel = level as ServerLevel

                // 2. Проверяем, жив ли ещё Дракон Края
                val isDragonKilled = serverLevel.dragonFight?.saveData()?.previouslyKilled ?: false

                if (!isDragonKilled) {
                    // 3. Вычисляем расстояние от игрока до центра мира (0, 0) по горизонтали
                    val distanceToCenter = sqrt((player.x * player.x) + (player.z * player.z))

                    // 4. Если игрок вышел за пределы Центрального острова (дальше 250 блоков)
                    if (distanceToCenter > 250.0) {

                        // Накладываем Жуткую Слепоту и Тьму на 5 секунд (100 тиков)
                        player.addEffect(MobEffectInstance(MobEffects.BLINDNESS, 100, 0, false, false))
                        player.addEffect(MobEffectInstance(MobEffects.DARKNESS, 100, 0, false, false))

                        // Бьем игрока чистым уроном магии иссушения (2 сердца = 4.0f) сквозь любую броню
                        player.hurt(serverLevel.damageSources().source(DamageTypes.WITHER), 4.0f)

                        // Выводим жуткое предупреждение прямо НАД панелью предметов (ActionBar), чтобы не забивать чат
                        player.sendSystemMessage(
                            Component.literal("§c§lVOID AURA: §4You cannot leave the central island while the Dragon is alive!"),
                            true
                        )
                    }
                }
            }
        }
    }
}
