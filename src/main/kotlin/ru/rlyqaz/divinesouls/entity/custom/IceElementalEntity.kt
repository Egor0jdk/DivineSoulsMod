package ru.rlyqaz.divinesouls.entity.custom

import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.tags.DamageTypeTags
import net.minecraft.util.RandomSource
import net.minecraft.world.Difficulty
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.control.FlyingMoveControl
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation
import net.minecraft.world.entity.ai.navigation.PathNavigation
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB
import java.util.EnumSet
import kotlin.math.sqrt

class IceElementalEntity(
    entityType: EntityType<out Monster>,
    level: Level
) : Monster(entityType, level) {

    companion object {
        private val CHARGING: EntityDataAccessor<Boolean> =
            SynchedEntityData.defineId(IceElementalEntity::class.java, EntityDataSerializers.BOOLEAN)

        fun createAttributes(): AttributeSupplier.Builder {
            return createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 26.0)
                .add(Attributes.FLYING_SPEED, 0.45)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
        }

        fun checkIceElementalSpawnRules(
            type: EntityType<IceElementalEntity>,
            level: ServerLevelAccessor,
            spawnType: MobSpawnType,
            pos: BlockPos,
            random: RandomSource
        ): Boolean {
            if (level.difficulty == Difficulty.PEACEFUL) return false

            // 1. Строго под открытым небом — ни одной пещеры!
            if (!level.canSeeSky(pos)) return false

            // 2. Спавнимся только у поверхности (в 1–2 блоках над снегом, льдом или землёй)
            val nearSurface = !level.getBlockState(pos.below()).isAir || !level.getBlockState(pos.below(2)).isAir
            if (!nearSurface) return false

            // 3. Не застреваем в твёрдых блоках (слой снега minecraft:snow пропускаем)
            val stateAtPos = level.getBlockState(pos)
            if (stateAtPos.blocksMotion()) return false

            // 4. Не больше 2 элементалей в радиусе 36 блоков
            val nearby = level.getEntitiesOfClass(
                IceElementalEntity::class.java,
                AABB(pos).inflate(36.0)
            )
            return nearby.size < 2
        }
    }

    init {
        moveControl = FlyingMoveControl(this, 15, true)
        isNoGravity = true
        xpReward = 10
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(CHARGING, false)
    }

    fun isChargingAttack(): Boolean = entityData.get(CHARGING)

    fun setChargingAttack(charging: Boolean) {
        entityData.set(CHARGING, charging)
    }

    override fun createNavigation(level: Level): PathNavigation {
        val nav = FlyingPathNavigation(this, level)
        nav.setCanOpenDoors(false)
        nav.setCanFloat(true)
        return nav
    }

    @Suppress("DEPRECATION")
    override fun finalizeSpawn(
        level: ServerLevelAccessor,
        difficulty: net.minecraft.world.DifficultyInstance,
        spawnType: MobSpawnType,
        spawnGroupData: net.minecraft.world.entity.SpawnGroupData?
    ): net.minecraft.world.entity.SpawnGroupData? {
        val data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData)
        setPos(x, y + 1.2, z)
        return data
    }

    override fun registerGoals() {
        goalSelector.addGoal(0, FloatGoal(this))
        goalSelector.addGoal(1, IceVolleyAttackGoal(this))
        goalSelector.addGoal(2, WaterAvoidingRandomFlyingGoal(this, 0.9))
        goalSelector.addGoal(3, LookAtPlayerGoal(this, Player::class.java, 12.0f))
        goalSelector.addGoal(4, RandomLookAroundGoal(this))

        targetSelector.addGoal(1, HurtByTargetGoal(this))
        targetSelector.addGoal(2, NearestAttackableTargetGoal(this, Player::class.java, true))
    }

    // Разрешаем спавн на снегу даже при ярком дневном свете (как у Песчаного краба)
    override fun getWalkTargetValue(pos: BlockPos, level: net.minecraft.world.level.LevelReader): Float {
        return 1.0f
    }

    override fun aiStep() {
        super.aiStep()
        if (level().isClientSide) {
            // При зарядке залпа частиц становится в 2 раза больше, как у разгорающегося ифрита
            val count = if (isChargingAttack()) 5 else 3

            for (i in 0 until count) {
                val offsetX = (random.nextDouble() - 0.5) * 1.4
                val offsetY = 0.2 + random.nextDouble() * 1.1
                val offsetZ = (random.nextDouble() - 0.5) * 1.4

                // 1. Плотные снежинки, заполняющие пространство между головой и кольцами
                level().addParticle(
                    ParticleTypes.SNOWFLAKE,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    (random.nextDouble() - 0.5) * 0.03,
                    -0.015,
                    (random.nextDouble() - 0.5) * 0.03
                )
            }

            // 2. Крупные объёмные снежные/ледяные частицы (аналог крупных клубов дыма у ифрита)
            if (random.nextInt(2) == 0 || isChargingAttack()) {
                level().addParticle(
                    ParticleTypes.ITEM_SNOWBALL,
                    x + (random.nextDouble() - 0.5) * 1.1,
                    y + 0.35 + random.nextDouble() * 0.8,
                    z + (random.nextDouble() - 0.5) * 1.1,
                    0.0,
                    0.02,
                    0.0
                )
            }
        }
    }

    override fun canFreeze(): Boolean = false

    override fun hurt(source: DamageSource, amount: Float): Boolean {
        val finalAmount = if (source.`is`(DamageTypeTags.IS_FIRE)) amount * 2.0f else amount
        return super.hurt(source, finalAmount)
    }

    override fun causeFallDamage(fallDistance: Float, multiplier: Float, source: DamageSource): Boolean = false

    override fun checkFallDamage(y: Double, onGround: Boolean, state: BlockState, pos: BlockPos) {}

    override fun getAmbientSound(): SoundEvent = ru.rlyqaz.divinesouls.sound.ModSounds.ICE_ELEMENTAL_AMBIENT.get()

    override fun getHurtSound(damageSource: DamageSource): SoundEvent = ru.rlyqaz.divinesouls.sound.ModSounds.ICE_ELEMENTAL_HURT.get()

    override fun getDeathSound(): SoundEvent = ru.rlyqaz.divinesouls.sound.ModSounds.ICE_ELEMENTAL_DEATH.get()

    override fun getVoicePitch(): Float = 1.0f

    private class IceVolleyAttackGoal(private val elemental: IceElementalEntity) : Goal() {
        private var attackStep = 0
        private var attackTime = 0

        init {
            flags = EnumSet.of(Flag.MOVE, Flag.LOOK)
        }

        override fun canUse(): Boolean {
            val target = elemental.target
            return target != null && target.isAlive && elemental.canAttack(target)
        }

        override fun start() {
            attackStep = 0
        }

        override fun stop() {
            elemental.setChargingAttack(false)
        }

        override fun tick() {
            val target = elemental.target ?: return
            attackTime--

            val distSq = elemental.distanceToSqr(target)
            val hasLineOfSight = elemental.sensing.hasLineOfSight(target)

            if (distSq < 4.0) {
                if (attackTime <= 0) {
                    attackTime = 20
                    elemental.doHurtTarget(target)
                }
                elemental.moveControl.setWantedPosition(target.x, target.y, target.z, 1.0)
            } else if (distSq < 256.0 && hasLineOfSight) {
                elemental.lookControl.setLookAt(target, 10.0f, 10.0f)
                if (distSq > 64.0) {
                    elemental.moveControl.setWantedPosition(target.x, target.y + 1.5, target.z, 1.0)
                }

                if (attackTime <= 0) {
                    attackStep++
                    if (attackStep == 1) {
                        attackTime = 30
                        elemental.setChargingAttack(true)
                        elemental.playSound(SoundEvents.BREEZE_CHARGE, 1.0f, 1.3f)
                    } else if (attackStep <= 4) {
                        attackTime = 7
                        shootIceShard(target)
                    } else {
                        attackTime = 70
                        attackStep = 0
                        elemental.setChargingAttack(false)
                    }
                }
            } else {
                elemental.moveControl.setWantedPosition(target.x, target.y + 1.0, target.z, 1.0)
            }
        }

        private fun shootIceShard(target: LivingEntity) {
            val level = elemental.level()
            val shard = IceShardEntity(level, elemental, 4.5f)
            shard.setPos(elemental.x, elemental.y + elemental.eyeHeight * 0.8, elemental.z)

            val dx = target.x - elemental.x
            val dy = target.getY(0.5) - shard.y
            val dz = target.z - elemental.z
            val dist = sqrt(dx * dx + dz * dz)

            shard.shoot(dx, dy + dist * 0.08, dz, 1.35f, 4.0f)
            level.addFreshEntity(shard)
            elemental.playSound(ru.rlyqaz.divinesouls.sound.ModSounds.ICE_ELEMENTAL_SHOOT.get(), 1.0f, 1.0f)
        }
    }
}
