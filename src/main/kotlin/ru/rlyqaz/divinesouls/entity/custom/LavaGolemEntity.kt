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
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.pathfinder.PathType
import net.minecraft.world.phys.AABB
import kotlin.math.cos
import kotlin.math.sin

class LavaGolemEntity(
    entityType: EntityType<out Monster>,
    level: Level
) : Monster(entityType, level) {

    private var slamCooldown = 60
    private var slamTriggered = false

    var slamAnimTicks: Int
        get() = entityData.get(DATA_SLAM_TICKS)
        set(value) = entityData.set(DATA_SLAM_TICKS, value)

    init {
        xpReward = 22
        setPathfindingMalus(PathType.LAVA, 0.0f)
        setPathfindingMalus(PathType.DANGER_FIRE, 0.0f)
        setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0f)
    }

    companion object {
        private val DATA_SLAM_TICKS: EntityDataAccessor<Int> =
            SynchedEntityData.defineId(LavaGolemEntity::class.java, EntityDataSerializers.INT)

        fun createAttributes(): AttributeSupplier.Builder {
            return createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 72.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.ATTACK_DAMAGE, 13.0)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85)
                .add(Attributes.FOLLOW_RANGE, 24.0)
        }

        fun checkLavaGolemSpawnRules(
            type: EntityType<LavaGolemEntity>,
            level: ServerLevelAccessor,
            spawnType: MobSpawnType,
            pos: BlockPos,
            random: RandomSource
        ): Boolean {
            if (level.difficulty == Difficulty.PEACEFUL) return false
            val serverLevel = level.level
            if (serverLevel.dimension() != Level.NETHER) return false

            // Строго в Хардмоде (после победы над Эндер-драконом)
            val endLevel = serverLevel.server.getLevel(Level.END)
            if (endLevel?.dragonFight?.hasPreviouslyKilledDragon() != true) return false

            // Не спавнимся в Долине душ и Базальтовых дельтах
            val biome = level.getBiome(pos)
            if (biome.`is`(Biomes.SOUL_SAND_VALLEY) || biome.`is`(Biomes.BASALT_DELTAS)) return false

            val groundState = level.getBlockState(pos.below())
            if (!groundState.isSolidRender(level, pos.below())) return false

            val nearby = level.getEntitiesOfClass(
                LavaGolemEntity::class.java,
                AABB(pos).inflate(48.0)
            )
            return nearby.size.compareTo(2) == -1
        }
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(DATA_SLAM_TICKS, 0)
    }

    override fun registerGoals() {
        goalSelector.addGoal(1, MeleeAttackGoal(this, 1.15, true))
        goalSelector.addGoal(2, WaterAvoidingRandomStrollGoal(this, 0.85))
        goalSelector.addGoal(3, LookAtPlayerGoal(this, Player::class.java, 10.0f))
        goalSelector.addGoal(4, RandomLookAroundGoal(this))

        targetSelector.addGoal(1, HurtByTargetGoal(this))
        targetSelector.addGoal(2, NearestAttackableTargetGoal(this, Player::class.java, true))
    }

    override fun fireImmune(): Boolean = true

    override fun isSensitiveToWater(): Boolean = true

    override fun customServerAiStep() {
        super.customServerAiStep()
        val serverLevel = level() as? ServerLevel ?: return

        if (slamAnimTicks > 0) {
            // Замираем на время замаха и удара, чтобы не скользить по полу
            navigation.stop()
            slamAnimTicks--

            // Ровно в момент удара кулаками о землю (на 6-м тике из 20) выпускаем огненную волну!
            if (slamAnimTicks == 6 && !slamTriggered) {
                slamTriggered = true
                performMagmaSlam(serverLevel)
            }
        }

        if (slamCooldown > 0) {
            slamCooldown--
            return
        }

        val currentTarget = target
        if (currentTarget != null && currentTarget.isAlive && distanceToSqr(currentTarget) <= 25.0) {
            slamCooldown = 100
            slamAnimTicks = 20 // 14 тиков замах вверх + удар на 14-м тике + 6 тиков доводка
            slamTriggered = false
            playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.1f, 0.65f)
        }
    }

    private fun performMagmaSlam(level: ServerLevel) {
        level.playSound(
            null, x, y, z,
            SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE,
            0.9f, 0.75f
        )

        // Огненное кольцо частиц вокруг голема
        for (i in 0 until 28) {
            val angle = (i.toDouble() / 28.0) * Math.PI * 2.0
            val px = x + cos(angle) * 2.8
            val pz = z + sin(angle) * 2.8
            level.sendParticles(
                ParticleTypes.FLAME,
                px, y + 0.25, pz,
                2, cos(angle) * 0.25, 0.08, sin(angle) * 0.25, 0.05
            )
            if (i % 2 == 0) {
                level.sendParticles(
                    ParticleTypes.LAVA,
                    px, y + 0.3, pz,
                    1, 0.1, 0.1, 0.1, 0.02
                )
            }
        }

        val victims = level.getEntitiesOfClass(LivingEntity::class.java, boundingBox.inflate(4.5, 1.5, 4.5)) { e ->
            e != this && e !is LavaGolemEntity && e.isAlive
        }
        for (victim in victims) {
            victim.hurt(level.damageSources().mobAttack(this), 8.0f)
            victim.igniteForSeconds(4.0f)
            victim.push(0.0, 0.42, 0.0)
            victim.hurtMarked = true
        }
    }

    override fun doHurtTarget(entity: Entity): Boolean {
        if (super.doHurtTarget(entity)) {
            entity.igniteForSeconds(3.0f)
            entity.push(0.0, 0.32, 0.0)
            return true
        }
        return false
    }

    override fun getWalkTargetValue(pos: BlockPos, level: LevelReader): Float = 1.0f

    override fun getAmbientSound(): SoundEvent = SoundEvents.IRON_GOLEM_STEP

    override fun getHurtSound(damageSource: DamageSource): SoundEvent = SoundEvents.IRON_GOLEM_HURT

    override fun getDeathSound(): SoundEvent = SoundEvents.IRON_GOLEM_DEATH

    override fun getVoicePitch(): Float = 0.65f
}
