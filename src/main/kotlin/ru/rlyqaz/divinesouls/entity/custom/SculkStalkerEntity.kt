package ru.rlyqaz.divinesouls.entity.custom

import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.Difficulty
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.phys.AABB
import ru.rlyqaz.divinesouls.worldgen.SculkInfectionManager

class SculkStalkerEntity(
    entityType: EntityType<out Monster>,
    level: Level
) : Monster(entityType, level) {

    private var scanCooldown = 60
    private var screechCooldown = 0
    private var memoryTicks = 0

    init {
        xpReward = 14
    }

    companion object {
        fun createAttributes(): AttributeSupplier.Builder {
            return createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 46.0)
                .add(Attributes.MOVEMENT_SPEED, 0.39)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.ARMOR, 3.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4)
                .add(Attributes.FOLLOW_RANGE, 24.0)
        }

        fun checkSculkStalkerSpawnRules(
            type: EntityType<SculkStalkerEntity>,
            level: ServerLevelAccessor,
            spawnType: MobSpawnType,
            pos: BlockPos,
            random: RandomSource
        ): Boolean {
            if (level.difficulty == Difficulty.PEACEFUL) return false
            val serverLevel = level.level
            if (serverLevel.dimension() != Level.OVERWORLD) return false

            val endLevel = serverLevel.server.getLevel(Level.END)
            if (endLevel?.dragonFight?.hasPreviouslyKilledDragon() != true) return false

            // Строго на поверхности Скалк-биома (не под землёй)
            if (!level.canSeeSky(pos)) return false
            if (!SculkInfectionManager.isSculkBiomeAt(serverLevel.seed, pos.x, pos.z)) return false
            if (!level.getBlockState(pos).isAir) return false

            val nearby = level.getEntitiesOfClass(
                SculkStalkerEntity::class.java,
                AABB(pos).inflate(40.0)
            )
            return nearby.size < 2
        }
    }

    override fun registerGoals() {
        goalSelector.addGoal(0, FloatGoal(this))
        goalSelector.addGoal(1, MeleeAttackGoal(this, 1.25, true))
        goalSelector.addGoal(2, WaterAvoidingRandomStrollGoal(this, 0.8))
        goalSelector.addGoal(3, RandomLookAroundGoal(this))

        // Слепой хищник: никаких визуальных таргетов, только ответ на урон + слух + акустический скан
        targetSelector.addGoal(1, HurtByTargetGoal(this))
    }

    override fun customServerAiStep() {
        super.customServerAiStep()
        val serverLevel = level() as? ServerLevel ?: return

        if (screechCooldown != 0) {
            screechCooldown--
        }

        val currentTarget = target
        if (currentTarget == null || !currentTarget.isAlive) {
            val heard = findAudiblePlayer(serverLevel, 18.0)
            if (heard != null) {
                lockOntoPrey(serverLevel, heard, screech = true)
            }
        } else {
            if (currentTarget is Player && currentTarget.isCrouching && distanceToSqr(currentTarget) > 49.0) {
                if (memoryTicks != 0) {
                    memoryTicks--
                } else {
                    target = null
                }
            } else {
                memoryTicks = 110
            }
        }

        if (scanCooldown != 0) {
            scanCooldown--
        } else {
            scanCooldown = 90 + random.nextInt(30)
            val nearbyPrey = serverLevel.getEntitiesOfClass(Player::class.java, boundingBox.inflate(14.0)) { p ->
                p.isAlive && !p.isCreative && !p.isSpectator
            }.minByOrNull { distanceToSqr(it) }

            if (nearbyPrey != null) {
                lockOntoPrey(serverLevel, nearbyPrey, screech = true)
            }
        }
    }

    private fun findAudiblePlayer(level: ServerLevel, radius: Double): Player? {
        val players = level.getEntitiesOfClass(Player::class.java, boundingBox.inflate(radius)) { p ->
            p.isAlive && !p.isCreative && !p.isSpectator
        }
        for (p in players) {
            if (p.isCrouching && p.onGround()) continue
            if (p.deltaMovement.horizontalDistanceSqr() > 0.002 || !p.onGround()) return p
        }
        return null
    }

    private fun lockOntoPrey(level: ServerLevel, player: Player, screech: Boolean) {
        val wasUnaware = target != player
        target = player
        memoryTicks = 110

        if (screech && wasUnaware && screechCooldown == 0) {
            screechCooldown = 100
            level.playSound(
                null, x, y, z,
                SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE,
                0.75f, 1.55f
            )
            level.sendParticles(
                ParticleTypes.SCULK_SOUL,
                x, eyeY, z,
                14, 0.4, 0.4, 0.4, 0.05
            )
            player.addEffect(MobEffectInstance(MobEffects.DARKNESS, 70, 0), this)
        }
    }

    override fun doHurtTarget(entity: Entity): Boolean {
        if (super.doHurtTarget(entity)) {
            if (entity is LivingEntity) {
                entity.addEffect(MobEffectInstance(MobEffects.DARKNESS, 60, 0), this)
                entity.addEffect(MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0), this)
            }
            return true
        }
        return false
    }

    override fun getWalkTargetValue(pos: BlockPos, level: net.minecraft.world.level.LevelReader): Float = 1.0f

    override fun getAmbientSound(): SoundEvent = SoundEvents.WARDEN_AMBIENT

    override fun getHurtSound(damageSource: DamageSource): SoundEvent = SoundEvents.WARDEN_HURT

    override fun getDeathSound(): SoundEvent = SoundEvents.WARDEN_DEATH

    override fun getVoicePitch(): Float = 1.45f
}
