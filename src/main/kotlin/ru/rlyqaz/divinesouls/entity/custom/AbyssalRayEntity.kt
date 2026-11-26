package ru.rlyqaz.divinesouls.entity.custom

import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.Mth
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
import net.minecraft.world.entity.ai.control.FlyingMoveControl
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
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

class AbyssalRayEntity(
    entityType: EntityType<AbyssalRayEntity>,
    level: Level
) : Monster(entityType, level) {

    init {
        this.moveControl = FlyingMoveControl(this, 35, true)
        this.isNoGravity = true
        this.xpReward = 24
    }

    companion object {
        fun createAttributes(): AttributeSupplier.Builder {
            return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 74.0)
                .add(Attributes.FLYING_SPEED, 0.65)
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.ATTACK_DAMAGE, 14.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75)
                .add(Attributes.FOLLOW_RANGE, 40.0)
        }

        fun checkAbyssalRaySpawnRules(
            type: EntityType<AbyssalRayEntity>,
            level: ServerLevelAccessor,
            spawnType: MobSpawnType,
            pos: BlockPos,
            random: RandomSource
        ): Boolean {
            if (level.difficulty == Difficulty.PEACEFUL) return false
            if (pos.y < 45 || pos.y > 225) return false

            for (dx in -1..1) {
                for (dy in 0..1) {
                    for (dz in -1..1) {
                        if (!level.getBlockState(pos.offset(dx, dy, dz)).isAir) return false
                    }
                }
            }

            val nearby = level.getEntitiesOfClass(
                AbyssalRayEntity::class.java,
                AABB(pos).inflate(64.0)
            )
            return nearby.size < 2
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
        this.goalSelector.addGoal(0, FloatGoal(this))
        this.goalSelector.addGoal(1, RayGlideAttackGoal(this))
        this.goalSelector.addGoal(2, RaySoarPatrolGoal(this))
        this.goalSelector.addGoal(3, LookAtPlayerGoal(this, Player::class.java, 16.0f))
        this.goalSelector.addGoal(4, RandomLookAroundGoal(this))

        this.targetSelector.addGoal(1, HurtByTargetGoal(this))
        this.targetSelector.addGoal(2, NearestAttackableTargetGoal(this, Player::class.java, true))
    }

    override fun aiStep() {
        super.aiStep()
        if (this.level().isClientSide && this.random.nextInt(2) == 0) {
            this.level().addParticle(
                ParticleTypes.SOUL_FIRE_FLAME,
                this.x + (this.random.nextDouble() - 0.5) * 1.8,
                this.y + 0.25,
                this.z + (this.random.nextDouble() - 0.5) * 1.8,
                0.0,
                -0.01,
                0.0
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

    override fun causeFallDamage(fallDistance: Float, multiplier: Float, source: DamageSource): Boolean = false

    override fun checkFallDamage(y: Double, onGround: Boolean, state: BlockState, pos: BlockPos) {}

    override fun getAmbientSound(): SoundEvent = SoundEvents.PHANTOM_FLAP

    override fun getHurtSound(damageSource: DamageSource): SoundEvent = SoundEvents.ELDER_GUARDIAN_HURT

    override fun getDeathSound(): SoundEvent = SoundEvents.ELDER_GUARDIAN_DEATH

    override fun getVoicePitch(): Float = 0.55f

    override fun getSoundVolume(): Float = 0.85f

    private class RayGlideAttackGoal(private val ray: AbyssalRayEntity) : Goal() {
        private var orbitAngle = 0.0
        private var swoopCooldown = 50
        private var isSwooping = false
        private var swoopTicks = 0
        private var swoopDir = Vec3.ZERO
        private var hitInPass = false

        init {
            flags = EnumSet.of(Flag.MOVE, Flag.LOOK)
        }

        override fun canUse(): Boolean {
            val t = ray.target
            return t != null && t.isAlive
        }

        override fun start() {
            isSwooping = false
            swoopCooldown = 45
            orbitAngle = ray.random.nextDouble() * Math.PI * 2.0
        }

        override fun tick() {
            val target = ray.target ?: return
            val serverLevel = ray.level() as? ServerLevel ?: return

            if (isSwooping) {
                swoopTicks++
                val vel = swoopDir.scale(0.82)
                ray.deltaMovement = vel

                val yaw = (Mth.atan2(vel.z, vel.x) * (180.0 / Math.PI)).toFloat() - 90.0f
                ray.yRot = yaw
                ray.yBodyRot = yaw
                ray.yHeadRot = yaw

                if (!hitInPass && ray.boundingBox.inflate(0.55).intersects(target.boundingBox)) {
                    hitInPass = true
                    ray.doHurtTarget(target)
                    target.push(swoopDir.x * 1.25, 0.42, swoopDir.z * 1.25)
                    target.hurtMarked = true
                    serverLevel.playSound(
                        null, target.x, target.y, target.z,
                        SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.HOSTILE,
                        1.1f, 0.75f
                    )
                }

                if (ray.horizontalCollision || ray.verticalCollision || swoopTicks >= 26) {
                    isSwooping = false
                    swoopCooldown = 65 + ray.random.nextInt(30)
                    orbitAngle = Mth.atan2(ray.z - target.z, ray.x - target.x)
                }
                return
            }

            ray.lookControl.setLookAt(target, 25.0f, 25.0f)
            orbitAngle += 0.075
            val radius = 8.5
            val destX = target.x + cos(orbitAngle) * radius
            val destZ = target.z + sin(orbitAngle) * radius
            val destY = target.y + 3.5

            ray.moveControl.setWantedPosition(destX, destY, destZ, 1.25)

            if (swoopCooldown > 0) {
                swoopCooldown--
            } else if (ray.sensing.hasLineOfSight(target) && ray.distanceToSqr(target) <= 324.0) {
                isSwooping = true
                swoopTicks = 0
                hitInPass = false
                val lead = Vec3(
                    target.x + target.deltaMovement.x * 5.0 - ray.x,
                    (target.y + 0.6) - ray.y,
                    target.z + target.deltaMovement.z * 5.0 - ray.z
                )
                swoopDir = if (lead.lengthSqr() > 0.01) lead.normalize() else ray.lookAngle
                ray.playSound(SoundEvents.PHANTOM_SWOOP, 1.2f, 0.6f)
            }
        }
    }

    private class RaySoarPatrolGoal(private val ray: AbyssalRayEntity) : Goal() {
        private var waypoint = Vec3.ZERO
        private var timer = 0

        init {
            flags = EnumSet.of(Flag.MOVE)
        }

        override fun canUse(): Boolean = ray.target == null

        override fun canContinueToUse(): Boolean = ray.target == null

        override fun start() {
            pickWaypoint()
        }

        override fun tick() {
            if (timer > 0) timer--
            if (timer == 0 || ray.position().distanceToSqr(waypoint) < 16.0 || ray.horizontalCollision) {
                pickWaypoint()
            }
            ray.moveControl.setWantedPosition(waypoint.x, waypoint.y, waypoint.z, 1.0)
        }

        private fun pickWaypoint() {
            timer = 90 + ray.random.nextInt(60)
            val dx = (ray.random.nextDouble() - 0.5) * 44.0
            val dy = (ray.random.nextDouble() - 0.5) * 16.0
            val dz = (ray.random.nextDouble() - 0.5) * 44.0
            val targetY = (ray.y + dy).coerceIn(55.0, 215.0)
            waypoint = Vec3(ray.x + dx, targetY, ray.z + dz)
        }
    }
}
