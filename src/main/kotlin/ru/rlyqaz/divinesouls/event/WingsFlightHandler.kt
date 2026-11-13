package ru.rlyqaz.divinesouls.event

import net.minecraft.client.Minecraft
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Items
import net.minecraft.world.phys.Vec3
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import net.neoforged.neoforge.event.tick.PlayerTickEvent
import ru.rlyqaz.divinesouls.item.ModItems
import top.theillusivec4.curios.api.CuriosApi
import java.util.HashMap
import java.util.UUID

object WingsFlightHandler {

    private val flightTicksMap = HashMap<UUID, Int>()

    private var wasForwardDown = false
    private var lastForwardTapTime = 0L
    private var dashCooldownTicks = 0

    private enum class WingTier(val maxTicks: Int, val liftSpeed: Double, val hasDash: Boolean) {
        NONE(0, 0.0, false),
        WIND(40, 0.22, false),    // Крылья ветра: 2 сек подъёма, без рывка
        ELYTRA(100, 0.28, true)   // Переработанные Элитры: 5 сек подъёма + рывок
    }

    // Блокируем попытку надеть элитры правым кликом в руке
    @SubscribeEvent
    fun onRightClickElytra(event: PlayerInteractEvent.RightClickItem) {
        if (event.itemStack.`is`(Items.ELYTRA)) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onPlayerTick(event: PlayerTickEvent.Post) {
        val player: Player = event.entity
        if (!player.level().isClientSide) return

        val uuid = player.uuid
        val tier = getEquippedWingTier(player)

        if (tier == WingTier.NONE) return

        if (player.isFallFlying) {
            player.stopFallFlying()
        }

        if (dashCooldownTicks > 0) {
            dashCooldownTicks--
        }

        val options = Minecraft.getInstance().options

        // Рывок на двойное нажатие W (работает и на земле, и в воздухе)
        if (tier.hasDash) {
            val isForwardDown = options.keyUp.isDown

            if (isForwardDown && !wasForwardDown) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastForwardTapTime < 300L && dashCooldownTicks == 0) {
                    performDash(player)
                    dashCooldownTicks = 40
                    lastForwardTapTime = 0L
                } else {
                    lastForwardTapTime = currentTime
                }
            }
            wasForwardDown = isForwardDown
        }

        // Полёт и перезарядка
        if (player.onGround() || player.isInWater || player.isInLava) {
            flightTicksMap[uuid] = tier.maxTicks
        } else {
            val isJumping = options.keyJump.isDown

            if (isJumping) {
                val remainingTicks = flightTicksMap.getOrDefault(uuid, 0)
                val currentMotion: Vec3 = player.deltaMovement

                if (remainingTicks > 0) {
                    player.deltaMovement = Vec3(currentMotion.x, tier.liftSpeed, currentMotion.z)
                    flightTicksMap[uuid] = remainingTicks - 1
                } else {
                    if (currentMotion.y < -0.08) {
                        player.deltaMovement = Vec3(currentMotion.x, -0.08, currentMotion.z)
                    }
                }
            }
        }
    }

    private fun performDash(player: Player) {
        val lookVec = player.lookAngle
        val verticalBoost = if (player.onGround()) 0.15 else (lookVec.y * 0.5) + 0.1
        val dashVelocity = Vec3(lookVec.x * 0.95, verticalBoost, lookVec.z * 0.95)

        player.deltaMovement = dashVelocity

        // Отправляем сигнал на сервер: если в руках Кристальный щит — тараним мобов на пути!
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(
            ru.rlyqaz.divinesouls.network.DashAttackPayload
        )

        player.level().playLocalSound(
            player.x, player.y, player.z,
            SoundEvents.BREEZE_JUMP, SoundSource.PLAYERS,
            0.8f, 1.2f, false
        )
    }

    @SubscribeEvent
    fun onPlayerHurt(event: LivingIncomingDamageEvent) {
        val entity = event.entity
        if (entity is Player && getEquippedWingTier(entity) != WingTier.NONE) {
            if (event.source.`is`(DamageTypes.FALL)) {
                event.isCanceled = true
            }
        }
    }

    private fun getEquippedWingTier(player: Player): WingTier {
        val curiosOptional = CuriosApi.getCuriosInventory(player)
        if (curiosOptional.isPresent) {
            val inventory = curiosOptional.get()
            if (inventory.findFirstCurio(Items.ELYTRA).isPresent) {
                return WingTier.ELYTRA
            }
            if (inventory.findFirstCurio(ModItems.WINGS_OF_WIND.get()).isPresent) {
                return WingTier.WIND
            }
        }
        return WingTier.NONE
    }
}
