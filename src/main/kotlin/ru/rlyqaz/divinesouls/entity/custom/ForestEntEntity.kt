package ru.rlyqaz.divinesouls.entity.custom

import net.minecraft.core.BlockPos
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.tags.ItemTags
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
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB

class ForestEntEntity(
    entityType: EntityType<out Monster>,
    level: Level
) : Monster(entityType, level) {

    companion object {
        private val IS_AWAKE: EntityDataAccessor<Boolean> = SynchedEntityData.defineId(
            ForestEntEntity::class.java,
            EntityDataSerializers.BOOLEAN
        )

        private val SLAM_ANIM_TICKS: EntityDataAccessor<Int> = SynchedEntityData.defineId(
            ForestEntEntity::class.java,
            EntityDataSerializers.INT
        )

        fun createAttributes(): AttributeSupplier.Builder {
            return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 55.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75)
                .add(Attributes.FOLLOW_RANGE, 20.0)
        }

        fun checkForestEntSpawnRules(
            type: EntityType<ForestEntEntity>,
            level: ServerLevelAccessor,
            spawnType: MobSpawnType,
            pos: BlockPos,
            random: RandomSource
        ): Boolean {
            if (level.difficulty == Difficulty.PEACEFUL) return false
            if (!level.canSeeSky(pos)) return false

            val ground = level.getBlockState(pos.below())
            if (!ground.`is`(Blocks.GRASS_BLOCK) && !ground.`is`(Blocks.DIRT) && !ground.`is`(Blocks.PODZOL)) {
                return false
            }

            // Проверяем, что вокруг нет других энтов в радиусе 48 блоков (чтобы лес не превратился в армию)
            val nearby = level.getEntitiesOfClass(
                ForestEntEntity::class.java,
                AABB(pos).inflate(48.0)
            )
            return nearby.isEmpty()
        }
    }

    private var rootSlamCooldown = 60
    var wakeTicks = 0

    var isAwake: Boolean
        get() = entityData.get(IS_AWAKE)
        set(value) {
            entityData.set(IS_AWAKE, value)
            refreshDimensions()
        }

    var slamAnimTicks: Int
        get() = entityData.get(SLAM_ANIM_TICKS)
        set(value) = entityData.set(SLAM_ANIM_TICKS, value)

    init {
        xpReward = 15
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(IS_AWAKE, false)
        builder.define(SLAM_ANIM_TICKS, 0)
    }

    override fun registerGoals() {
        goalSelector.addGoal(0, FloatGoal(this))
        goalSelector.addGoal(1, object : MeleeAttackGoal(this, 1.15, true) {
            override fun canUse(): Boolean = isAwake && super.canUse()
            override fun canContinueToUse(): Boolean = isAwake && super.canContinueToUse()
        })
        goalSelector.addGoal(2, object : WaterAvoidingRandomStrollGoal(this, 0.8) {
            override fun canUse(): Boolean = isAwake && super.canUse()
            override fun canContinueToUse(): Boolean = isAwake && super.canContinueToUse()
        })
        goalSelector.addGoal(3, object : LookAtPlayerGoal(this, Player::class.java, 8.0f) {
            override fun canUse(): Boolean = isAwake && super.canUse()
        })
        goalSelector.addGoal(4, object : RandomLookAroundGoal(this) {
            override fun canUse(): Boolean = isAwake && super.canUse()
        })

        targetSelector.addGoal(1, HurtByTargetGoal(this))
        targetSelector.addGoal(2, object : NearestAttackableTargetGoal<Player>(this, Player::class.java, true) {
            override fun canUse(): Boolean = isAwake && super.canUse()
            override fun canContinueToUse(): Boolean = isAwake && super.canContinueToUse()
        })
    }

    fun wakeUp(targetPlayer: LivingEntity? = null) {
        if (isAwake) return
        isAwake = true
        if (targetPlayer != null) {
            this.target = targetPlayer
        }

        if (!level().isClientSide) {
            playSound(SoundEvents.WOOD_BREAK, 1.4f, 0.65f)
            playSound(SoundEvents.RAVAGER_ROAR, 0.7f, 0.55f)
            val serverLevel = level() as ServerLevel
            serverLevel.sendParticles(
                BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_LEAVES.defaultBlockState()),
                x, y + 1.8, z,
                35, 0.8, 1.0, 0.8, 0.15
            )
            serverLevel.sendParticles(
                BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_LOG.defaultBlockState()),
                x, y + 1.0, z,
                20, 0.5, 0.8, 0.5, 0.1
            )
        }
    }

    override fun tick() {
        super.tick()

        if (isAwake && wakeTicks.compareTo(24) == -1) {
            wakeTicks++
        }

        if (!level().isClientSide) {
            if (slamAnimTicks != 0) {
                slamAnimTicks--
                // Ровно на 6-м тике замаха (когда руки обрушиваются в землю) один раз наносим урон!
                if (slamAnimTicks == 6 && isAwake) {
                    performRootSlam()
                }
            }

            if (!isAwake) {
                val centerX = kotlin.math.floor(x) + 0.5
                val centerZ = kotlin.math.floor(z) + 0.5
                if (x != centerX || z != centerZ) {
                    setPos(centerX, y, centerZ)
                }
                deltaMovement = deltaMovement.multiply(0.0, 1.0, 0.0)
                yRot = 0.0f
                yBodyRot = 0.0f
                yHeadRot = 0.0f
            } else {
                val currentTarget = this.target
                if (currentTarget != null && currentTarget.isAlive && slamAnimTicks == 0) {
                    if (rootSlamCooldown != 0) {
                        rootSlamCooldown--
                    } else if (distanceToSqr(currentTarget).compareTo(22.0) == -1) {
                        // Запускаем замах (20 тиков) и сразу уводим атаку на перезарядку (90 тиков)
                        slamAnimTicks = 20
                        rootSlamCooldown = 90
                    }
                }
            }
        }
    }

    private fun performRootSlam() {
        val serverLevel = level() as ServerLevel
        playSound(SoundEvents.ROOTED_DIRT_BREAK, 1.3f, 0.7f)
        serverLevel.sendParticles(
            BlockParticleOption(ParticleTypes.BLOCK, Blocks.ROOTED_DIRT.defaultBlockState()),
            x, y + 0.2, z,
            30, 1.8, 0.2, 1.8, 0.12
        )

        val area = boundingBox.inflate(3.5, 1.0, 3.5)
        val enemies = serverLevel.getEntitiesOfClass(LivingEntity::class.java, area) { entity ->
            entity != this && entity !is ForestEntEntity && entity.isAlive
        }
        for (enemy in enemies) {
            enemy.hurt(damageSources().mobAttack(this), 4.5f)
            enemy.addEffect(MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 1), this)
            enemy.push(0.0, 0.28, 0.0)
        }
    }

    override fun hurt(source: DamageSource, amount: Float): Boolean {
        val attacker = source.entity as? LivingEntity
        if (!isAwake) {
            wakeUp(attacker)
        }

        // Топоры наносят Лесному энту +50% урона!
        var finalAmount = amount
        if (attacker != null && attacker.mainHandItem.`is`(ItemTags.AXES)) {
            finalAmount *= 1.5f
            if (!level().isClientSide) {
                playSound(SoundEvents.AXE_STRIP, 1.2f, 0.8f)
            }
        }

        return super.hurt(source, finalAmount)
    }

    override fun doHurtTarget(entity: Entity): Boolean {
        if (super.doHurtTarget(entity)) {
            if (entity is LivingEntity) {
                entity.addEffect(MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0), this)
            }
            return true
        }
        return false
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        super.addAdditionalSaveData(compound)
        compound.putBoolean("IsAwake", isAwake)
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {
        super.readAdditionalSaveData(compound)
        isAwake = compound.getBoolean("IsAwake")
    }

    override fun getDefaultDimensions(pose: net.minecraft.world.entity.Pose): net.minecraft.world.entity.EntityDimensions {
        return if (isAwake) {
            net.minecraft.world.entity.EntityDimensions.scalable(1.35f, 3.4f)
        } else {
            net.minecraft.world.entity.EntityDimensions.scalable(1.0f, 4.0f)
        }
    }

    override fun onSyncedDataUpdated(key: EntityDataAccessor<*>) {
        super.onSyncedDataUpdated(key)
        if (key == IS_AWAKE) {
            refreshDimensions()
        }
    }

    // 1. Разрешаем естественный спавн при ярком дневном свете в лесу
    override fun getWalkTargetValue(pos: BlockPos, level: net.minecraft.world.level.LevelReader): Float {
        return 1.0f
    }

    // 1. Твёрдая коллизия как у блока/шалкера (нельзя зайти внутрь ствола)
    override fun canBeCollidedWith(): Boolean = !isAwake

    // 2. Спящего энта невозможно сдвинуть с места, пробудившийся толкается как обычный моб
    override fun isPushable(): Boolean = isAwake

    override fun push(x: Double, y: Double, z: Double) {
        if (isAwake) {
            super.push(x, y, z)
        }
    }

    override fun getAmbientSound(): SoundEvent = SoundEvents.WOOD_STEP

    override fun getHurtSound(damageSource: DamageSource): SoundEvent = SoundEvents.WOOD_HIT

    override fun getDeathSound(): SoundEvent = SoundEvents.WOOD_BREAK

    override fun playStepSound(pos: BlockPos, block: BlockState) {
        playSound(SoundEvents.WOOD_STEP, 0.6f, 0.7f)
    }
}
