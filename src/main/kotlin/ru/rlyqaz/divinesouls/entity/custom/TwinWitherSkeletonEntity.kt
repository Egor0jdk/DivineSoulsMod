package ru.rlyqaz.divinesouls.entity.custom

import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.chat.Component
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerBossEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.BossEvent
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.InteractionHand
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.SpawnGroupData
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.monster.WitherSkeleton
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import ru.rlyqaz.divinesouls.item.ModItems
import ru.rlyqaz.divinesouls.item.ModWeapons
import java.util.EnumSet
import java.util.UUID

class TwinWitherSkeletonEntity(
    entityType: EntityType<out WitherSkeleton>,
    level: Level
) : WitherSkeleton(entityType, level) {

    private val bossEvent = ServerBossEvent(
        this.displayName,
        BossEvent.BossBarColor.RED,
        BossEvent.BossBarOverlay.NOTCHED_10
    )

    private var waveTicksRemaining = 0
    private var waveOrigin = Vec3.ZERO
    private var waveDirection = Vec3.ZERO
    private val hitEntitiesInWave = HashSet<UUID>()

    init {
        this.xpReward = 50
    }

    companion object {
        // Синхронизируемый счётчик замаха для плавной анимации на клиенте
        private val DATA_SLAM_WINDUP: EntityDataAccessor<Int> = SynchedEntityData.defineId(
            TwinWitherSkeletonEntity::class.java,
            EntityDataSerializers.INT
        )

        fun createAttributes(): AttributeSupplier.Builder {
            return WitherSkeleton.createAttributes()
                .add(Attributes.MAX_HEALTH, 140.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.FOLLOW_RANGE, 32.0)
        }
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(DATA_SLAM_WINDUP, 0)
    }

    fun getSlamWindupTicks(): Int = this.entityData.get(DATA_SLAM_WINDUP)

    fun setSlamWindupTicks(ticks: Int) {
        this.entityData.set(DATA_SLAM_WINDUP, ticks)
    }

    override fun registerGoals() {
        super.registerGoals()
        this.goalSelector.addGoal(1, FireWaveSlamGoal(this))
    }

    // Блокируем обычную ближнюю атаку, пока босс замахивается для огненной волны
    override fun doHurtTarget(target: net.minecraft.world.entity.Entity): Boolean {
        if (getSlamWindupTicks() > 0) {
            return false
        }
        return super.doHurtTarget(target)
    }

    override fun tick() {
        super.tick()

        val windup = getSlamWindupTicks()
        if (!this.level().isClientSide && windup > 0) {
            val serverLevel = this.level() as ServerLevel
            serverLevel.sendParticles(
                ParticleTypes.FLAME,
                this.x, this.y + 2.8, this.z,
                3, 0.4, 0.3, 0.4, 0.02
            )
        }

        // Полёт 3D-волны пламени (бьёт и по земле, и в воздух в сторону игрока)
        if (!this.level().isClientSide && waveTicksRemaining > 0) {
            val serverLevel = this.level() as ServerLevel
            val step = 13 - waveTicksRemaining
            val distance = step * 1.25 // Общая дальность ~15 блоков

            val currentPos = waveOrigin.add(waveDirection.scale(distance))

            serverLevel.sendParticles(
                ParticleTypes.FLAME,
                currentPos.x, currentPos.y, currentPos.z,
                14, 0.35, 0.35, 0.35, 0.03
            )
            serverLevel.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                currentPos.x, currentPos.y, currentPos.z,
                5, 0.25, 0.25, 0.25, 0.02
            )

            // Объёмный 3D-хитбокс (2.2 x 2.4 x 2.2 блока), чтобы нельзя было легко перелететь на крыльях
            val hitBox = AABB(
                currentPos.x - 1.1, currentPos.y - 1.0, currentPos.z - 1.1,
                currentPos.x + 1.1, currentPos.y + 1.4, currentPos.z + 1.1
            )

            val targets = serverLevel.getEntitiesOfClass(LivingEntity::class.java, hitBox) { entity ->
                entity != this && entity.isAlive && !hitEntitiesInWave.contains(entity.uuid)
            }

            for (target in targets) {
                hitEntitiesInWave.add(target.uuid)
                // Чистый огненный урон (3.5f = 1.75 сердца) + поджог на 3 секунды, без Иссушения
                target.hurt(this.damageSources().magic(), 3.5f)
                if (target.remainingFireTicks < 60) {
                    target.remainingFireTicks = 60
                }
            }

            waveTicksRemaining--
        }
    }

    fun launchFireWave(target: LivingEntity) {
        // Считаем полный 3D-вектор от груди босса к центру тела игрока (включая высоту Y!)
        val startPos = Vec3(this.x, this.y + 1.4, this.z)
        val targetPos = Vec3(target.x, target.y + target.bbHeight * 0.5, target.z)
        val vecToTarget = targetPos.subtract(startPos)

        if (vecToTarget.lengthSqr() < 0.001) return

        this.waveDirection = vecToTarget.normalize()
        this.waveOrigin = startPos
        this.waveTicksRemaining = 12
        this.hitEntitiesInWave.clear()

        this.swing(InteractionHand.MAIN_HAND, true)

        this.level().playSound(
            null, this.x, this.y, this.z,
            SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE,
            1.2f, 0.7f
        )
        this.level().playSound(
            null, this.x, this.y, this.z,
            SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.HOSTILE,
            1.1f, 0.55f
        )
    }

    override fun populateDefaultEquipmentSlots(random: RandomSource, difficulty: DifficultyInstance) {
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack(ModWeapons.WITHER_MACE.get()))
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0f)
    }

    override fun finalizeSpawn(
        level: ServerLevelAccessor,
        difficulty: DifficultyInstance,
        spawnType: MobSpawnType,
        spawnGroupData: SpawnGroupData?
    ): SpawnGroupData? {
        val data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData)
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack(ModWeapons.WITHER_MACE.get()))
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0f)
        return data
    }

    override fun dropCustomDeathLoot(level: ServerLevel, damageSource: DamageSource, recentlyHit: Boolean) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit)
        this.spawnAtLocation(ItemStack(Items.WITHER_SKELETON_SKULL, 2))
        this.spawnAtLocation(ItemStack(ModItems.WITHER_SEAL.get(), 1))

        if (this.random.nextFloat() < 0.50f) {
            this.spawnAtLocation(ItemStack(ModWeapons.WITHER_MACE.get(), 1))
        }
    }

    override fun customServerAiStep() {
        super.customServerAiStep()
        this.bossEvent.progress = this.health / this.maxHealth
    }

    override fun startSeenByPlayer(player: ServerPlayer) {
        super.startSeenByPlayer(player)
        this.bossEvent.addPlayer(player)
    }

    override fun stopSeenByPlayer(player: ServerPlayer) {
        super.stopSeenByPlayer(player)
        this.bossEvent.removePlayer(player)
    }

    override fun setCustomName(name: Component?) {
        super.setCustomName(name)
        this.bossEvent.name = this.displayName
    }

    // ИИ-цель: полный замах булавой (1.25 сек) -> удар по земле и пуск огненной волны
    private class FireWaveSlamGoal(private val boss: TwinWitherSkeletonEntity) : Goal() {
        private var cooldown = 50
        private var windupTicks = 0
        private val maxWindup = 15 // Быстрый и чёткий замах: 15 тиков (0.75 секунды)

        init {
            // Блокируем движение и взгляд, чтобы ванильный ИИ не сбивал анимацию
            this.flags = EnumSet.of(Flag.MOVE, Flag.LOOK)
        }

        override fun canUse(): Boolean {
            if (windupTicks > 0) return false
            val target = boss.target ?: return false
            if (!target.isAlive) return false

            if (cooldown > 0) {
                cooldown--
                return false
            }
            // Дальность до 16 блоков в любом направлении (включая высоту)
            return boss.distanceToSqr(target) <= 256.0
        }

        override fun start() {
            windupTicks = maxWindup
            boss.setSlamWindupTicks(windupTicks)
            boss.navigation.stop()

            boss.level().playSound(
                null, boss.x, boss.y, boss.z,
                SoundEvents.WITHER_SKELETON_AMBIENT, SoundSource.HOSTILE,
                1.3f, 0.5f
            )
        }

        override fun canContinueToUse(): Boolean {
            return windupTicks > 0
        }

        override fun requiresUpdateEveryTick(): Boolean = true

        override fun tick() {
            val target = boss.target
            boss.navigation.stop()
            if (target != null) {
                boss.lookControl.setLookAt(target, 30.0f, 30.0f)
            }

            windupTicks--
            boss.setSlamWindupTicks(windupTicks)

            if (windupTicks <= 0) {
                if (target != null && target.isAlive) {
                    boss.launchFireWave(target)
                }
                cooldown = 90 // Перезарядка 4.5 секунды
            }
        }

        override fun stop() {
            windupTicks = 0
            boss.setSlamWindupTicks(0)
        }
    }
}