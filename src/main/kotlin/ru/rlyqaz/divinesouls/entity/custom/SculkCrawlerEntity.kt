package ru.rlyqaz.divinesouls.entity.custom

import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
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
import net.minecraft.world.entity.monster.Spider
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.phys.AABB
import ru.rlyqaz.divinesouls.worldgen.SculkInfectionManager

class SculkCrawlerEntity(
    entityType: EntityType<out Spider>,
    level: Level
) : Spider(entityType, level) {

    private var echolocationCooldown = 50
    private var memoryTicks = 0

    var echoWaveTicks: Int
        get() = entityData.get(DATA_ECHO_TICKS)
        set(value) = entityData.set(DATA_ECHO_TICKS, value)

    init {
        xpReward = 10
    }

    companion object {
        private val DATA_ECHO_TICKS: EntityDataAccessor<Int> =
            SynchedEntityData.defineId(SculkCrawlerEntity::class.java, EntityDataSerializers.INT)

        fun createAttributes(): AttributeSupplier.Builder {
            return Spider.createAttributes()
                .add(Attributes.MAX_HEALTH, 26.0)
                .add(Attributes.MOVEMENT_SPEED, 0.36)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.FOLLOW_RANGE, 20.0)
        }

        fun checkSculkCrawlerSpawnRules(
            type: EntityType<SculkCrawlerEntity>,
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

            val inSculkSurface = SculkInfectionManager.isSculkBiomeAt(serverLevel, pos.x, pos.z)
            val inDeepDark = level.getBiome(pos).`is`(Biomes.DEEP_DARK)
            if (!inSculkSurface && !inDeepDark) return false

            if (!level.getBlockState(pos).isAir) return false

            if (inDeepDark) {
                if (level.getMaxLocalRawBrightness(pos).compareTo(7) == 1) return false
                // В Тёмных глубинах нет конкурентов — прореживаем попытки спавна на 75%
                if (random.nextInt(4) != 0) return false
            }

            // Жёсткий лимит: не больше 2 ползунов в радиусе 42 блоков
            val nearby = level.getEntitiesOfClass(
                SculkCrawlerEntity::class.java,
                AABB(pos).inflate(42.0)
            )
            return nearby.size.compareTo(2) == -1
        }
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(DATA_ECHO_TICKS, 0)
    }

    override fun registerGoals() {
        goalSelector.addGoal(0, FloatGoal(this))
        goalSelector.addGoal(1, MeleeAttackGoal(this, 1.25, true))
        goalSelector.addGoal(2, WaterAvoidingRandomStrollGoal(this, 0.85))
        goalSelector.addGoal(3, RandomLookAroundGoal(this))

        // Слепой моб: только ответ на удар + слух + эхо-локация
        targetSelector.addGoal(1, HurtByTargetGoal(this))
    }

    private fun isValidSculkPrey(entity: LivingEntity): Boolean {
        if (!entity.isAlive) return false
        if (entity is SculkCrawlerEntity || entity is SculkStalkerEntity || entity is net.minecraft.world.entity.monster.warden.Warden) return false
        if (entity is net.minecraft.world.entity.decoration.ArmorStand) return false
        if (entity is Player && (entity.isCreative || entity.isSpectator)) return false
        return true
    }

    override fun customServerAiStep() {
        super.customServerAiStep()
        val serverLevel = level() as? ServerLevel ?: return

        if (echoWaveTicks != 0) {
            echoWaveTicks--
        }

        // 1. Слух: засекаем любых живых существ рядом, которые не крадутся на Shift и двигаются/прыгают
        if (target == null || !target!!.isAlive) {
            val heardPrey = findAudiblePrey(serverLevel, 14.0)
            if (heardPrey != null) {
                alertToTarget(serverLevel, heardPrey, playPing = false)
            }
        } else {
            val currentTarget = target
            if (currentTarget is Player && currentTarget.isCrouching && distanceToSqr(currentTarget).compareTo(36.0) == 1) {
                if (memoryTicks != 0) {
                    memoryTicks--
                } else {
                    target = null
                }
            } else {
                memoryTicks = 80
            }
        }

        // 2. Активная эхо-локация каждые ~3.5–4.5 секунды (засекает всех живых существ, как Варден!)
        if (echolocationCooldown != 0) {
            echolocationCooldown--
        } else {
            echolocationCooldown = 70 + random.nextInt(20)
            performEcholocation(serverLevel)
        }
    }

    private fun findAudiblePrey(level: ServerLevel, radius: Double): LivingEntity? {
        val box = boundingBox.inflate(radius)
        val entities = level.getEntitiesOfClass(LivingEntity::class.java, box) { isValidSculkPrey(it) }
        for (e in entities) {
            if (e.isCrouching && e.onGround()) continue
            val isMoving = e.deltaMovement.horizontalDistanceSqr().compareTo(0.002) == 1 || !e.onGround()
            if (isMoving) return e
        }
        return null
    }

    private fun performEcholocation(level: ServerLevel) {
        level.playSound(
            null, x, y, z,
            SoundEvents.SCULK_CLICKING, SoundSource.HOSTILE,
            1.1f, 1.25f
        )

        echoWaveTicks = 14

        val pulseRadius = 13.5
        val detected = level.getEntitiesOfClass(LivingEntity::class.java, boundingBox.inflate(pulseRadius)) { e ->
            isValidSculkPrey(e) && distanceToSqr(e).compareTo(pulseRadius * pulseRadius) != 1
        }.minByOrNull { distanceToSqr(it) }

        if (detected != null) {
            alertToTarget(level, detected, playPing = true)
        }
    }

    private fun alertToTarget(level: ServerLevel, prey: LivingEntity, playPing: Boolean) {
        target = prey
        memoryTicks = 90
        if (playPing) {
            level.playSound(
                null, x, y, z,
                SoundEvents.WARDEN_TENDRIL_CLICKS, SoundSource.HOSTILE,
                1.0f, 1.5f
            )
            level.sendParticles(
                ParticleTypes.SCULK_SOUL,
                x, y + 0.7, z,
                6, 0.25, 0.25, 0.25, 0.03
            )
        }
    }

    override fun doHurtTarget(entity: Entity): Boolean {
        if (super.doHurtTarget(entity)) {
            if (entity is LivingEntity) {
                entity.addEffect(MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 0), this)
            }
            return true
        }
        return false
    }

    override fun getWalkTargetValue(pos: BlockPos, level: net.minecraft.world.level.LevelReader): Float = 1.0f

    override fun getAmbientSound(): SoundEvent = SoundEvents.SCULK_CLICKING

    override fun getHurtSound(damageSource: DamageSource): SoundEvent = SoundEvents.SCULK_BLOCK_HIT

    override fun getDeathSound(): SoundEvent = SoundEvents.SCULK_SHRIEKER_BREAK

    override fun finalizeSpawn(
        level: ServerLevelAccessor,
        difficulty: net.minecraft.world.DifficultyInstance,
        spawnType: MobSpawnType,
        spawnGroupData: net.minecraft.world.entity.SpawnGroupData?
    ): net.minecraft.world.entity.SpawnGroupData? {
        return spawnGroupData
    }
}
