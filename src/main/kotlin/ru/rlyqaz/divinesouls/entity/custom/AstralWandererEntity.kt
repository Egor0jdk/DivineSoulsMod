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
import net.minecraft.world.phys.Vec3
import java.util.EnumSet
import kotlin.math.cos
import kotlin.math.sin

class AstralWandererEntity(
    entityType: EntityType<out Monster>,
    level: Level
) : Monster(entityType, level) {

    private var teleportCooldown = 0

    init {
        moveControl = FlyingMoveControl(this, 25, true)
        isNoGravity = true
        xpReward = 15
    }

    companion object {
        fun createAttributes(): AttributeSupplier.Builder {
            return createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 42.0)
                .add(Attributes.FLYING_SPEED, 0.95)
                .add(Attributes.MOVEMENT_SPEED, 0.42)
                .add(Attributes.ATTACK_DAMAGE, 9.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
        }

        fun checkAstralWandererSpawnRules(
            type: EntityType<AstralWandererEntity>,
            level: ServerLevelAccessor,
            spawnType: MobSpawnType,
            pos: BlockPos,
            random: RandomSource
        ): Boolean {
            if (level.difficulty == Difficulty.PEACEFUL) return false
            val serverLevel = level.level
            if (serverLevel.dimension() != Level.OVERWORLD) return false

            // Строго в Хардмоде (после победы над Эндер-драконом)
            val endLevel = serverLevel.server.getLevel(Level.END)
            if (endLevel?.dragonFight?.hasPreviouslyKilledDragon() != true) return false

            // Духи света не спавнятся в заражённом Скалк-биоме
            if (ru.rlyqaz.divinesouls.worldgen.SculkInfectionManager.isSculkBiomeAt(serverLevel.seed, pos.x, pos.z)) {
                return false
            }

            // Только ночью под открытым небом
            if (!level.canSeeSky(pos) || !Monster.isDarkEnoughToSpawn(level, pos, random)) return false
            if (!level.getBlockState(pos).isAir) return false

            // Запрещаем спавн над водой: в пределах 4 блоков вниз должна быть твёрдая земля, а не река/озеро
            if (!level.getFluidState(pos).isEmpty || !level.getFluidState(pos.below()).isEmpty) return false
            var hasSolidGroundBelow = false
            for (dy in 1..4) {
                val belowState = level.getBlockState(pos.below(dy))
                if (!belowState.fluidState.isEmpty) return false
                if (!belowState.isAir) {
                    hasSolidGroundBelow = true
                    break
                }
            }
            if (!hasSolidGroundBelow) return false

            val nearby = level.getEntitiesOfClass(
                AstralWandererEntity::class.java,
                AABB(pos).inflate(44.0)
            )
            return nearby.size.compareTo(2) == -1
        }
    }
    override fun createNavigation(level: Level): PathNavigation {
        val nav = FlyingPathNavigation(this, level)
        nav.setCanOpenDoors(false)
        nav.setCanFloat(true)
        nav.setCanPassDoors(true)
        return nav
    }

    override fun registerGoals() {
        goalSelector.addGoal(0, FloatGoal(this))
        goalSelector.addGoal(1, AstralPulseAttackGoal(this))
        goalSelector.addGoal(2, WaterAvoidingRandomFlyingGoal(this, 1.0))
        goalSelector.addGoal(3, LookAtPlayerGoal(this, Player::class.java, 12.0f))
        goalSelector.addGoal(4, RandomLookAroundGoal(this))

        targetSelector.addGoal(1, HurtByTargetGoal(this))
        targetSelector.addGoal(2, NearestAttackableTargetGoal(this, Player::class.java, true))
    }

    override fun customServerAiStep() {
        super.customServerAiStep()
        if (teleportCooldown != 0) {
            teleportCooldown--
        }
    }

    override fun aiStep() {
        super.aiStep()
        if (level().isClientSide && random.nextInt(3) == 0) {
            level().addParticle(
                ParticleTypes.END_ROD,
                x + (random.nextDouble() - 0.5) * 0.7,
                y + 0.3 + random.nextDouble() * 1.3,
                z + (random.nextDouble() - 0.5) * 0.7,
                0.0,
                -0.015,
                0.0
            )
        }
    }

    override fun hurt(source: DamageSource, amount: Float): Boolean {
        val hurtSuccess = super.hurt(source, amount)
        if (hurtSuccess && !level().isClientSide && isAlive && teleportCooldown == 0 && random.nextFloat().compareTo(0.55f) == -1) {
            val attacker = source.entity as? LivingEntity ?: target
            if (attacker != null) {
                blinkBehindTarget(attacker)
            }
        }
        return hurtSuccess
    }

    private fun blinkBehindTarget(targetEntity: LivingEntity) {
        val serverLevel = level() as? ServerLevel ?: return
        val angle = random.nextDouble() * Math.PI * 2.0
        val dist = 5.0 + random.nextDouble() * 3.0
        val destX = targetEntity.x + cos(angle) * dist
        val destZ = targetEntity.z + sin(angle) * dist
        val destY = targetEntity.y + 1.5

        val destPos = BlockPos.containing(destX, destY, destZ)
        if (!serverLevel.getBlockState(destPos).isAir || !serverLevel.getBlockState(destPos.above()).isAir) return

        serverLevel.sendParticles(ParticleTypes.END_ROD, x, y + 1.0, z, 18, 0.35, 0.6, 0.35, 0.08)
        serverLevel.playSound(null, x, y, z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.9f, 1.35f)

        teleportTo(destX, destY, destZ)
        teleportCooldown = 50

        serverLevel.sendParticles(ParticleTypes.END_ROD, x, y + 1.0, z, 18, 0.35, 0.6, 0.35, 0.08)
        serverLevel.playSound(null, x, y, z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.1f, 1.4f)
    }

    override fun causeFallDamage(fallDistance: Float, multiplier: Float, source: DamageSource): Boolean = false

    override fun checkFallDamage(y: Double, onGround: Boolean, state: BlockState, pos: BlockPos) {}

    override fun getAmbientSound(): SoundEvent = SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM

    override fun getHurtSound(damageSource: DamageSource): SoundEvent = SoundEvents.ALLAY_HURT

    override fun getDeathSound(): SoundEvent = SoundEvents.ALLAY_DEATH

    override fun getVoicePitch(): Float = 0.65f

    private class AstralPulseAttackGoal(private val wanderer: AstralWandererEntity) : Goal() {
        private var attackCooldown = 25

        init {
            flags = EnumSet.of(Flag.MOVE, Flag.LOOK)
        }

        override fun canUse(): Boolean {
            val target = wanderer.target
            return target != null && target.isAlive
        }

        override fun tick() {
            val target = wanderer.target ?: return
            val serverLevel = wanderer.level() as? ServerLevel ?: return

            wanderer.lookControl.setLookAt(target, 30.0f, 30.0f)
            val distSq = wanderer.distanceToSqr(target)

            // Если игрок далеко (> 15 блоков) и телепорт свободен — мгновенный блинк ближе на огневую дистанцию!
            if (distSq.compareTo(225.0) == 1 && wanderer.teleportCooldown == 0 && wanderer.sensing.hasLineOfSight(target)) {
                wanderer.blinkBehindTarget(target)
                return
            }

            // Быстрое сближение на скорости 1.75 или отлёт, если игрок подошёл вплотную
            if (distSq.compareTo(144.0) == 1) {
                wanderer.moveControl.setWantedPosition(target.x, target.y + 2.0, target.z, 1.75)
            } else if (distSq.compareTo(25.0) == -1) {
                val away = wanderer.position().subtract(target.position()).normalize()
                wanderer.moveControl.setWantedPosition(
                    wanderer.x + away.x * 6.0,
                    target.y + 2.2,
                    wanderer.z + away.z * 6.0,
                    1.65
                )
            }

            if (attackCooldown != 0) {
                attackCooldown--
                return
            }

            // Стреляем астральным лучом уже с 22 блоков (484.0)
            if (distSq.compareTo(484.0) != 1 && wanderer.sensing.hasLineOfSight(target)) {
                attackCooldown = 45
                fireAstralBeam(serverLevel, target)
            }
        }

        private fun fireAstralBeam(level: ServerLevel, target: LivingEntity) {
            val start = wanderer.eyePosition.subtract(0.0, 0.2, 0.0)
            val end = Vec3(target.x, target.getY(0.5), target.z)
            val steps = (start.distanceTo(end) * 3.0).toInt().coerceAtLeast(4)

            for (i in 1..steps) {
                val p = start.lerp(end, i.toDouble() / steps.toDouble())
                level.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0.03, 0.03, 0.03, 0.0)
            }

            target.hurt(level.damageSources().indirectMagic(wanderer, wanderer), 9.0f)
            target.addEffect(MobEffectInstance(MobEffects.GLOWING, 80, 0), wanderer)

            level.playSound(
                null, wanderer.x, wanderer.y, wanderer.z,
                SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE,
                0.8f, 1.65f
            )
        }
    }
}
