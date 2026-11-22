package ru.rlyqaz.divinesouls.event

import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.phys.AABB
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.tick.LevelTickEvent
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.ModEntities
import ru.rlyqaz.divinesouls.entity.custom.PurpurGuardianEntity
import kotlin.math.cos
import kotlin.math.sin

@EventBusSubscriber(modid = DivineSoulsMod.MOD_ID)
object DragonFightEvents {

    private var guardiansSpawnedForCurrentDragon = false

    @SubscribeEvent
    fun onEndLevelTick(event: LevelTickEvent.Post) {
        val level = event.level as? ServerLevel ?: return
        if (level.dimension() != Level.END) return
        if (level.gameTime % 40L != 0L) return

        val arenaBox = AABB(-150.0, 20.0, -150.0, 150.0, 200.0, 150.0)
        val dragons = level.getEntitiesOfClass(EnderDragon::class.java, arenaBox) { it.isAlive }

        if (dragons.isEmpty()) {
            // Бой закончен — сбрасываем флаг, чтобы при следующем возрождении Дракона стражи снова появились!
            guardiansSpawnedForCurrentDragon = false
            return
        }

        val playersOnIsland = level.players().filter { p ->
            p.isAlive && !p.isSpectator && arenaBox.contains(p.position())
        }
        if (playersOnIsland.isEmpty()) return

        val existingGuardians = level.getEntitiesOfClass(PurpurGuardianEntity::class.java, arenaBox)
        if (existingGuardians.isNotEmpty()) {
            guardiansSpawnedForCurrentDragon = true
            return
        }

        if (!guardiansSpawnedForCurrentDragon) {
            guardiansSpawnedForCurrentDragon = true
            spawnThreeGuardians(level)
        }
    }

    private fun spawnThreeGuardians(level: ServerLevel) {
        val angles = listOf(0.0, (2.0 * Math.PI) / 3.0, (4.0 * Math.PI) / 3.0)
        for (angle in angles) {
            val radius = 24.0
            val sx = (cos(angle) * radius).toInt()
            val sz = (sin(angle) * radius).toInt()
            val sy = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, sx, sz)
            if (sy <= 40) continue

            val spawnPos = BlockPos(sx, sy, sz)
            val guardian = ModEntities.PURPUR_GUARDIAN.get().create(level) ?: continue
            guardian.moveTo(sx + 0.5, sy.toDouble(), sz + 0.5, level.random.nextFloat() * 360.0f, 0.0f)
            net.neoforged.neoforge.event.EventHooks.finalizeMobSpawn(
                guardian,
                level,
                level.getCurrentDifficultyAt(spawnPos),
                MobSpawnType.EVENT,
                null
            )
            level.addFreshEntity(guardian)

            level.sendParticles(ParticleTypes.PORTAL, sx + 0.5, sy + 1.0, sz + 0.5, 25, 0.5, 0.8, 0.5, 0.1)
            level.playSound(null, spawnPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0f, 0.8f)
        }
    }
}
