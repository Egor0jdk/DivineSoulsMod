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
import net.minecraft.world.entity.ai.control.FlyingMoveControl
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
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

class VoidDragonEntity(
    entityType: EntityType<out Monster>,
    level: Level
) : Monster(entityType, level) {

    companion object {
        fun createAttributes(): AttributeSupplier.Builder {
            return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 42.0)
                .add(Attributes.FLYING_SPEED, 0.95)
                .add(Attributes.MOVEMENT_SPEED, 0.42)
                .add(Attributes.ATTACK_DAMAGE, 7.5)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
        }

        fun checkVoidDragonSpawnRules(
            type: EntityType<VoidDragonEntity>,
            level: ServerLevelAccessor,
            spawnType: MobSpawnType,
            pos: BlockPos,
            random: RandomSource
        ): Boolean {
            if (level.difficulty == Difficulty.PEACEFUL) return false

            // 1. Строго после победы над Эндер-драконом
            val endLevel = level.level.server.getLevel(Level.END)
            if (endLevel?.dragonFight?.hasPreviouslyKilledDragon() != true) return false

            // 2. Только на дальних островах (дальше 300 блоков от центра 0, 0)
            val distSq = pos.x.toDouble() * pos.x.toDouble() + pos.z.toDouble() * pos.z.toDouble()
            if (distSq.compareTo(300.0 * 300.0) == -1) return false

            // 3. Не застреваем в блоках и не спавнимся над самой бездной слишком низко
            if (pos.y.compareTo(45) == -1 || !level.getBlockState(pos).isAir) return false

            val nearby = level.getEntitiesOfClass(
                VoidDragonEntity::class.java,
                AABB(pos).inflate(48.0)
            )
            return nearby.size.compareTo(2) == -1
        }
    }

    init {
        moveControl = FlyingMoveControl(this, 65, true)
        isNoGravity = true
        xpReward = 14
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
        goalSelector.addGoal(1, FellBeastSwoopGoal(this))
        goalSelector.addGoal(2, HighAltitudeHoverGoal(this))
        goalSelector.addGoal(3, LookAtPlayerGoal(this, Player::class.java, 16.0f))
        goalSelector.addGoal(4, RandomLookAroundGoal(this))

        targetSelector.addGoal(1, HurtByTargetGoal(this))
        targetSelector.addGoal(2, NearestAttackableTargetGoal(this, Player::class.java, true))
    }

    override fun aiStep() {
        super.aiStep()
        if (level().isClientSide && random.nextInt(2) == 0) {
            level().addParticle(
                ParticleTypes.PORTAL,
                x + (random.nextDouble() - 0.5) * 0.9,
                y + 0.3 + random.nextDouble() * 0.6,
                z + (random.nextDouble() - 0.5) * 0.9,
                (random.nextDouble() - 0.5) * 0.08,
                -0.05,
                (random.nextDouble() - 0.5) * 0.08
            )
        }
    }

    override fun doHurtTarget(entity: Entity): Boolean {
        if (super.doHurtTarget(entity)) {
            if (entity is LivingEntity) {
                entity.addEffect(MobEffectInstance(MobEffects.DARKNESS, 60, 0), this)
            }
            return true
        }
        return false
    }

    override fun canBeAffected(effectInstance: MobEffectInstance): Boolean {
        if (effectInstance.effect == MobEffects.LEVITATION) return false
        return super.canBeAffected(effectInstance)
    }

    override fun causeFallDamage(fallDistance: Float, multiplier: Float, source: DamageSource): Boolean = false

    override fun checkFallDamage(y: Double, onGround: Boolean, state: BlockState, pos: BlockPos) {}

    override fun getAmbientSound(): SoundEvent = SoundEvents.ENDER_DRAGON_AMBIENT

    override fun getHurtSound(damageSource: DamageSource): SoundEvent = SoundEvents.ENDER_DRAGON_HURT

    override fun getDeathSound(): SoundEvent = SoundEvents.PHANTOM_DEATH

    override fun getVoicePitch(): Float = 1.45f

    override fun getSoundVolume(): Float = 0.7f

    /**
     * Боевой ИИ Крылатой Твари:
     * 1. Кружит на высоте 4.5 блоков над игроком.
     * 2. Периодически выдыхает конус пустотного пламени (1.5 сек, можно увернуться).
     * 3. Совершает пикирующий набег по прямой дуге (можно уклониться рывком) и плавно уходит вверх.
     */
    private class FellBeastSwoopGoal(private val dragon: VoidDragonEntity) : Goal() {
        private var isSwooping = false
        private var swoopCooldown = 45
        private var swoopTicks = 0
        private var diveDirection = Vec3.ZERO

        private var climbTicks = 0
        private var climbDirection = Vec3.ZERO

        private var breathTicks = 0
        private var breathCooldown = 85
        private var breathDir = Vec3.ZERO

        private var orbitAngle = 0.0
        private var biteCooldown = 0

        init {
            flags = EnumSet.of(Flag.MOVE, Flag.LOOK)
        }

        override fun canUse(): Boolean {
            val target = dragon.target
            return target != null && target.isAlive
        }

        override fun start() {
            isSwooping = false
            climbTicks = 0
            breathTicks = 0
            swoopCooldown = 40
            orbitAngle = dragon.random.nextDouble() * Math.PI * 2.0
        }

        override fun tick() {
            val target = dragon.target ?: return

            if (biteCooldown != 0) {
                biteCooldown--
            }

            // Укус в воздухе, если игрок подлетел вплотную (когда дракон не дышит пламенем)
            if (breathTicks == 0 && biteCooldown == 0 && dragon.boundingBox.inflate(0.45).intersects(target.boundingBox)) {
                dragon.doHurtTarget(target)
                biteCooldown = 20
                if (isSwooping) {
                    startForwardClimb(target)
                    return
                }
            }

            // ФАЗА 1: Выдох струи Пустоты (30 тиков = 1.5 сек)
            if (breathTicks != 0) {
                breathTicks--
                val desiredDir = Vec3(target.x - dragon.x, target.getY(0.5) - (dragon.eyeY - 0.1), target.z - dragon.z).normalize()
                breathDir = breathDir.lerp(desiredDir, 0.08).normalize()

                val yaw = (net.minecraft.util.Mth.atan2(breathDir.z, breathDir.x) * (180.0 / Math.PI)).toFloat() - 90.0f
                dragon.yRot = yaw
                dragon.yBodyRot = yaw
                dragon.yHeadRot = yaw

                dragon.deltaMovement = dragon.deltaMovement.scale(0.65)
                emitVoidBreathCone(breathDir)
                return
            }

            // ФАЗА 2: Плавная U-образная дуга пикирования с упреждением по движению игрока
            if (isSwooping) {
                swoopTicks++

                // Целимся с упреждением (куда идёт игрок), чтобы нельзя было уйти простым шагом назад
                val leadPos = Vec3(
                    target.x + target.deltaMovement.x * 6.0,
                    target.y + 0.55,
                    target.z + target.deltaMovement.z * 6.0
                )
                val toTarget = leadPos.subtract(dragon.position())

                // Первые 20 тиков мягко доводим по дуге, в последние 6 тиков начинаем выравнивать нос горизонтально!
                if (swoopTicks.compareTo(21) == -1) {
                    if (toTarget.lengthSqr().compareTo(0.01) == 1) {
                        diveDirection = diveDirection.lerp(toTarget.normalize(), 0.26).normalize()
                    }
                } else {
                    val levelOut = Vec3(diveDirection.x, 0.15, diveDirection.z).normalize()
                    diveDirection = diveDirection.lerp(levelOut, 0.35).normalize()
                }

                val diveVel = diveDirection.scale(0.70)
                dragon.deltaMovement = diveVel
                val yaw = (net.minecraft.util.Mth.atan2(diveVel.z, diveVel.x) * (180.0 / Math.PI)).toFloat() - 90.0f
                dragon.yRot = yaw
                dragon.yBodyRot = yaw

                val hitFloor = dragon.onGround() || dragon.verticalCollision ||
                        !dragon.level().getBlockState(dragon.blockPosition().below()).isAir
                if (dragon.horizontalCollision || hitFloor || swoopTicks.compareTo(28) == 1) {
                    startForwardClimb(target)
                }
                return
            }

            // ФАЗА 3: Плавный дугообразный выход вверх по ходу полёта (12 тиков)
            if (climbTicks != 0) {
                climbTicks--
                if (dragon.horizontalCollision) {
                    climbTicks = 0
                    dragon.deltaMovement = Vec3(0.0, 0.28, 0.0)
                    return
                }

                // Постепенно загибаем вектор от горизонтального к подъёму вверх (плавная дуга!)
                val progress = (12 - climbTicks) / 12.0
                val upY = 0.20 + progress * 0.55
                val curvedDir = Vec3(climbDirection.x, upY, climbDirection.z).normalize()

                val climbVel = curvedDir.scale(0.44)
                dragon.deltaMovement = climbVel
                val yaw = (net.minecraft.util.Mth.atan2(climbVel.z, climbVel.x) * (180.0 / Math.PI)).toFloat() - 90.0f
                dragon.yRot = yaw
                dragon.yBodyRot = yaw
                return
            }

            // ФАЗА 4: Кружение на высоте 4.5 блока над игроком
            dragon.lookControl.setLookAt(target, 35.0f, 35.0f)
            orbitAngle += 0.11
            val radius = 6.5
            val orbitX = target.x + kotlin.math.cos(orbitAngle) * radius
            val orbitZ = target.z + kotlin.math.sin(orbitAngle) * radius
            val orbitY = target.y + 4.5

            dragon.moveControl.setWantedPosition(orbitX, orbitY, orbitZ, 1.25)

            if (breathCooldown != 0) {
                breathCooldown--
            } else if (dragon.sensing.hasLineOfSight(target) && dragon.distanceToSqr(target).compareTo(196.0) == -1) {
                breathTicks = 30
                breathCooldown = 115 + dragon.random.nextInt(30)
                breathDir = Vec3(target.x - dragon.x, target.getY(0.5) - (dragon.eyeY - 0.1), target.z - dragon.z).normalize()
                dragon.playSound(SoundEvents.ENDER_DRAGON_SHOOT, 0.9f, 1.35f)
                return
            }

            if (swoopCooldown != 0) {
                swoopCooldown--
            } else {
                isSwooping = true
                swoopTicks = 0
                val toTarget = Vec3(target.x - dragon.x, (target.y + 0.5) - dragon.y, target.z - dragon.z)
                diveDirection = if (toTarget.lengthSqr().compareTo(0.01) == 1) toTarget.normalize() else dragon.lookAngle
                dragon.playSound(SoundEvents.ENDER_DRAGON_GROWL, 0.75f, 1.5f)
            }
        }

        private fun startForwardClimb(target: LivingEntity) {
            isSwooping = false
            swoopTicks = 0
            swoopCooldown = 65 + dragon.random.nextInt(30)

            val horiz = Vec3(diveDirection.x, 0.0, diveDirection.z)
            val forward = if (horiz.lengthSqr().compareTo(0.001) == 1) horiz.normalize() else dragon.lookAngle
            climbDirection = forward
            climbTicks = 12

            orbitAngle = net.minecraft.util.Mth.atan2(
                (dragon.z + forward.z * 5.0) - target.z,
                (dragon.x + forward.x * 5.0) - target.x
            )
            dragon.moveControl.setWantedPosition(
                dragon.x + forward.x * 5.0,
                target.y + 4.5,
                dragon.z + forward.z * 5.0,
                1.25
            )
        }

        private fun emitVoidBreathCone(dir: Vec3) {
            val level = dragon.level() as? ServerLevel ?: return
            val mouthPos = Vec3(dragon.x, dragon.eyeY - 0.1, dragon.z).add(dir.scale(0.9))
            val maxRange = 9.5

            // Выпускаем направленную струю из пасти вперёд (при count = 0 параметры xDist/yDist/zDist задают вектор скорости!)
            for (i in 0 until 8) {
                val spreadX = (dragon.random.nextDouble() - 0.5) * 0.28
                val spreadY = (dragon.random.nextDouble() - 0.5) * 0.28
                val spreadZ = (dragon.random.nextDouble() - 0.5) * 0.28
                val speed = 0.55 + dragon.random.nextDouble() * 0.35

                val vx = (dir.x + spreadX) * speed
                val vy = (dir.y + spreadY) * speed
                val vz = (dir.z + spreadZ) * speed

                level.sendParticles(ParticleTypes.DRAGON_BREATH, mouthPos.x, mouthPos.y, mouthPos.z, 0, vx, vy, vz, 1.0)
                if (i % 2 == 0) {
                    level.sendParticles(ParticleTypes.PORTAL, mouthPos.x, mouthPos.y, mouthPos.z, 0, vx * 1.15, vy * 1.15, vz * 1.15, 1.0)
                }
            }

            if (breathTicks % 8 != 0) return

            val scanBox = dragon.boundingBox.expandTowards(dir.scale(maxRange)).inflate(1.5)
            val candidates = level.getEntitiesOfClass(LivingEntity::class.java, scanBox) { e ->
                e != dragon && e !is VoidDragonEntity && e.isAlive
            }

            for (victim in candidates) {
                val toVictim = victim.position().add(0.0, victim.bbHeight * 0.5, 0.0).subtract(mouthPos)
                val dist = toVictim.length()
                if (dist.compareTo(maxRange) == 1 || dist.compareTo(0.2) == -1) continue

                val alignment = toVictim.normalize().dot(dir)
                if (alignment.compareTo(0.92) == 1) {
                    victim.hurt(level.damageSources().indirectMagic(dragon, dragon), 3.0f)
                    victim.addEffect(MobEffectInstance(MobEffects.DARKNESS, 40, 0), dragon)
                }
            }
        }
    }

    /**
     * Непрерывное патрулирование на высоте 7–16 блоков над поверхностью островов
     */
    private class HighAltitudeHoverGoal(private val dragon: VoidDragonEntity) : Goal() {
        private var waypoint = Vec3.ZERO
        private var repathTimer = 0

        init {
            flags = EnumSet.of(Flag.MOVE)
        }

        override fun canUse(): Boolean = dragon.target == null

        override fun canContinueToUse(): Boolean = dragon.target == null

        override fun start() {
            pickNewWaypoint()
        }

        override fun tick() {
            if (repathTimer != 0) {
                repathTimer--
            }

            if (repathTimer == 0 || dragon.position().distanceToSqr(waypoint).compareTo(9.0) == -1 || dragon.horizontalCollision) {
                pickNewWaypoint()
            }

            dragon.moveControl.setWantedPosition(waypoint.x, waypoint.y, waypoint.z, 1.1)
        }

        private fun pickNewWaypoint() {
            repathTimer = 70 + dragon.random.nextInt(50)
            val level = dragon.level()

            // Ищем точку над сушей (4 попытки), чтобы дракон красиво патрулировал над островами
            for (attempt in 0 until 4) {
                val candX = dragon.x + (dragon.random.nextDouble() - 0.5) * 32.0
                val candZ = dragon.z + (dragon.random.nextDouble() - 0.5) * 32.0
                val groundY = level.getHeight(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    candX.toInt(),
                    candZ.toInt()
                )
                if (groundY.compareTo(10) == 1 || attempt == 3) {
                    val baseY = if (groundY.compareTo(10) == 1) groundY.toDouble() else 66.0
                    val desiredY = baseY + 7.0 + dragon.random.nextDouble() * 9.0
                    waypoint = Vec3(candX, desiredY, candZ)
                    return
                }
            }
        }
    }
}
