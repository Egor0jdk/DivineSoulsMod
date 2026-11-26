package ru.rlyqaz.divinesouls.entity.custom

import net.minecraft.core.BlockPos
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.chat.Component
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerBossEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.Mth
import net.minecraft.world.BossEvent
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PowerableMob
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
import net.minecraft.world.entity.projectile.LargeFireball
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import java.util.EnumSet
import kotlin.math.cos
import kotlin.math.sin

class MechaWitherEntity(
    entityType: EntityType<MechaWitherEntity>,
    level: Level
) : Monster(entityType, level), PowerableMob {

    private val bossEvent = ServerBossEvent(
        this.displayName,
        BossEvent.BossBarColor.WHITE,
        BossEvent.BossBarOverlay.NOTCHED_10
    )

    // 3 секунды (60 тиков) прогрева при спавне перед включением ИИ
    private var startupTicks = 60
    private var blockBreakTicks = 0

    init {
        this.moveControl = FlyingMoveControl(this, 45, true)
        this.isNoGravity = true
        this.xpReward = 90
    }

    companion object {
        private val DATA_DASHING: EntityDataAccessor<Boolean> =
            SynchedEntityData.defineId(MechaWitherEntity::class.java, EntityDataSerializers.BOOLEAN)

        private val DATA_PHASE: EntityDataAccessor<Int> =
            SynchedEntityData.defineId(MechaWitherEntity::class.java, EntityDataSerializers.INT)

        fun createAttributes(): AttributeSupplier.Builder {
            return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 360.0)
                .add(Attributes.FLYING_SPEED, 0.55)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 13.0)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 48.0)
        }
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(DATA_DASHING, false)
        builder.define(DATA_PHASE, 1)
    }

    fun isBootingUp(): Boolean = startupTicks > 0

    fun isDashing(): Boolean = entityData.get(DATA_DASHING)
    fun setDashing(value: Boolean) = entityData.set(DATA_DASHING, value)

    fun getPhase(): Int = entityData.get(DATA_PHASE)
    fun setPhase(value: Int) = entityData.set(DATA_PHASE, value)

    override fun isPowered(): Boolean = getPhase() == 2

    override fun createNavigation(level: Level): PathNavigation {
        val nav = FlyingPathNavigation(this, level)
        nav.setCanOpenDoors(false)
        nav.setCanFloat(true)
        nav.setCanPassDoors(true)
        return nav
    }

    override fun registerGoals() {
        this.goalSelector.addGoal(0, FloatGoal(this))
        this.goalSelector.addGoal(1, MechaWitherCombatGoal(this))
        this.goalSelector.addGoal(2, LookAtPlayerGoal(this, Player::class.java, 24.0f))
        this.goalSelector.addGoal(3, RandomLookAroundGoal(this))

        this.targetSelector.addGoal(1, HurtByTargetGoal(this))
        this.targetSelector.addGoal(2, NearestAttackableTargetGoal(this, Player::class.java, true))
    }

    override fun hurt(source: DamageSource, amount: Float): Boolean {
        if (startupTicks > 0) return false
        val result = super.hurt(source, amount)
        if (result && !level().isClientSide) {
            blockBreakTicks = 20
        }
        return result
    }

    override fun customServerAiStep() {
        super.customServerAiStep()
        val serverLevel = level() as? ServerLevel ?: return

        val ratio = (health / maxHealth).coerceIn(0.0f, 1.0f)
        bossEvent.progress = ratio

        // 3-секундный прогрев реактора при спавне
        if (startupTicks > 0) {
            startupTicks--
            deltaMovement = Vec3(0.0, Mth.sin(tickCount * 0.25f) * 0.02, 0.0)
            if (startupTicks % 6 == 0) {
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y + 1.8, z, 10, 0.6, 0.8, 0.6, 0.12)
            }
            if (startupTicks == 0) {
                serverLevel.playSound(null, x, y, z, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.3f, 1.15f)
                serverLevel.sendParticles(ParticleTypes.EXPLOSION, x, y + 1.8, z, 2, 0.4, 0.4, 0.4, 0.0)
            }
            return
        }

        if (horizontalCollision) {
            blockBreakTicks = 12
        }

        if (blockBreakTicks > 0) {
            blockBreakTicks--
            if (tickCount % 3 == 0 && net.neoforged.neoforge.event.EventHooks.canEntityGrief(serverLevel, this)) {
                breakSurroundingBlocks(serverLevel)
            }
        }

        val newPhase = when {
            ratio > 0.666f -> 1
            ratio > 0.333f -> 2
            else -> 3
        }

        val oldPhase = getPhase()
        if (newPhase != oldPhase) {
            setPhase(newPhase)
            if (newPhase == 2) {
                serverLevel.playSound(null, x, y, z, SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.5f, 1.1f)
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y + 1.5, z, 40, 1.0, 1.2, 1.0, 0.2)
            } else if (newPhase == 3) {
                serverLevel.playSound(null, x, y, z, SoundEvents.SHIELD_BREAK, SoundSource.HOSTILE, 1.6f, 0.6f)
                serverLevel.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.2f, 0.8f)
                serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y + 1.5, z, 1, 0.0, 0.0, 0.0, 0.0)
            }
        }

        if (newPhase == 2 && tickCount % 30 == 0 && isAlive) {
            val phase2Cap = maxHealth * 0.665f
            if (health < phase2Cap) {
                heal(1.0f)
                serverLevel.sendParticles(ParticleTypes.WAX_ON, x, y + 1.6, z, 6, 0.6, 0.8, 0.6, 0.05)
            }
        }

        if (newPhase == 3 && tickCount % 16 == 0) {
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 1.6, z, 1, 0.4, 0.5, 0.4, 0.02)
            if (tickCount % 32 == 0) {
                serverLevel.sendParticles(ParticleTypes.LAVA, x, y + 1.4, z, 1, 0.35, 0.45, 0.35, 0.05)
                serverLevel.sendParticles(
                    BlockParticleOption(ParticleTypes.BLOCK, Blocks.IRON_BLOCK.defaultBlockState()),
                    x, y + 1.5, z, 3, 0.4, 0.5, 0.4, 0.10
                )
            }
        }
    }

    override fun startSeenByPlayer(player: ServerPlayer) {
        super.startSeenByPlayer(player)
        bossEvent.addPlayer(player)
    }

    override fun stopSeenByPlayer(player: ServerPlayer) {
        super.stopSeenByPlayer(player)
        bossEvent.removePlayer(player)
    }

    override fun setCustomName(name: Component?) {
        super.setCustomName(name)
        bossEvent.name = displayName
    }

    override fun causeFallDamage(fallDistance: Float, multiplier: Float, source: DamageSource): Boolean = false

    override fun checkFallDamage(y: Double, onGround: Boolean, state: BlockState, pos: BlockPos) {}

    override fun getAmbientSound(): SoundEvent = SoundEvents.WITHER_AMBIENT

    override fun getHurtSound(damageSource: DamageSource): SoundEvent = SoundEvents.IRON_GOLEM_HURT

    override fun getDeathSound(): SoundEvent = SoundEvents.WITHER_DEATH

    override fun getVoicePitch(): Float = 0.6f

    private fun breakSurroundingBlocks(level: ServerLevel) {
        val box = boundingBox.inflate(0.85, 0.35, 0.85)
        val minX = Mth.floor(box.minX)
        val minY = Mth.floor(box.minY)
        val minZ = Mth.floor(box.minZ)
        val maxX = Mth.floor(box.maxX)
        val maxY = Mth.floor(box.maxY)
        val maxZ = Mth.floor(box.maxZ)

        var brokeAny = false
        for (bx in minX..maxX) {
            for (by in minY..maxY) {
                for (bz in minZ..maxZ) {
                    val pos = BlockPos(bx, by, bz)
                    val state = level.getBlockState(pos)
                    if (state.isAir || !state.fluidState.isEmpty) continue
                    if (state.`is`(net.minecraft.tags.BlockTags.WITHER_IMMUNE)) continue
                    if (state.getDestroySpeed(level, pos) < 0.0f) continue

                    level.destroyBlock(pos, true, this)
                    brokeAny = true
                }
            }
        }
        if (brokeAny) {
            level.playSound(null, x, y + 1.5, z, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 1.2f, 0.85f)
        }
    }

    private class MechaWitherCombatGoal(private val boss: MechaWitherEntity) : Goal() {
        // 0 = очередь лазеров, 1 = подготовка и рывки, 2 = ракетный залп (только 3-я фаза)
        private var attackStage = 0
        private var stageTimer = 25
        private var burstCount = 0
        private var dashesLeftInCycle = 1

        private var isDashingNow = false
        private var dashTicks = 0
        private var dashDir = Vec3.ZERO
        private var hitInDash = false

        private var anchorAngle = 0.0
        private var wallEscapeCooldown = 0

        init {
            flags = EnumSet.of(Flag.MOVE, Flag.LOOK)
        }

        override fun canUse(): Boolean {
            if (boss.isBootingUp()) return false
            val t = boss.target
            return t != null && t.isAlive
        }

        override fun start() {
            attackStage = 0
            stageTimer = 20
            burstCount = 0
            isDashingNow = false
            boss.setDashing(false)
            val target = boss.target
            anchorAngle = if (target != null) {
                Mth.atan2(boss.z - target.z, boss.x - target.x)
            } else {
                boss.random.nextDouble() * Math.PI * 2.0
            }
        }

        override fun stop() {
            isDashingNow = false
            boss.setDashing(false)
        }

        override fun tick() {
            if (boss.isBootingUp()) return
            val target = boss.target ?: return
            val serverLevel = boss.level() as? ServerLevel ?: return
            val phase = boss.getPhase()

            if (wallEscapeCooldown > 0) {
                wallEscapeCooldown--
            }

            // АКТИВНЫЙ РЫВОК ПРЯМО В ИГРОКА
            if (isDashingNow) {
                dashTicks++
                val dashSpeed = if (phase == 3) 1.15 else 1.42
                boss.deltaMovement = dashDir.scale(dashSpeed)

                val yaw = (Mth.atan2(dashDir.z, dashDir.x) * (180.0 / Math.PI)).toFloat() - 90.0f
                boss.yRot = yaw
                boss.yBodyRot = yaw
                boss.yHeadRot = yaw

                serverLevel.sendParticles(ParticleTypes.FLAME, boss.x, boss.y + 1.4, boss.z, 5, 0.35, 0.5, 0.35, 0.03)

                if (!hitInDash && boss.boundingBox.inflate(0.6).intersects(target.boundingBox)) {
                    hitInDash = true
                    val dashDmg = if (phase == 3) 15.5f else 19.0f
                    target.hurt(boss.damageSources().mobAttack(boss), dashDmg)
                    target.push(dashDir.x * 1.3, 0.42, dashDir.z * 1.3)
                    target.hurtMarked = true
                    serverLevel.playSound(null, target.x, target.y, target.z, SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.2f, 0.7f)
                }

                if (boss.horizontalCollision || dashTicks >= 8) {
                    isDashingNow = false
                    boss.setDashing(false)
                    boss.deltaMovement = boss.deltaMovement.scale(0.12)
                    dashesLeftInCycle--

                    if (dashesLeftInCycle > 0) {
                        // Пауза между рывками в серии: 0.8 сек во 2-й фазе и 1.0 сек в 3-й фазе
                        stageTimer = if (phase == 3) 20 else 16
                        boss.playSound(SoundEvents.BEACON_POWER_SELECT, 1.2f, 1.15f)
                    } else {
                        val sideSign = if (boss.random.nextBoolean()) 1.0 else -1.0
                        val shiftRad = Math.toRadians(60.0 + boss.random.nextDouble() * 55.0) * sideSign
                        anchorAngle += shiftRad

                        if (phase == 3) {
                            attackStage = 2
                            burstCount = 0
                            stageTimer = 22
                        } else {
                            attackStage = 0
                            burstCount = 0
                            stageTimer = 26
                        }
                    }
                }
                return
            }

            // Если при зависании упёрлись в стену — сразу меняем угол, чтобы босса нельзя было зажать!
            if (boss.horizontalCollision && wallEscapeCooldown == 0) {
                val escapeSign = if (boss.random.nextBoolean()) 1.0 else -1.0
                anchorAngle += Math.toRadians(95.0 + boss.random.nextDouble() * 50.0) * escapeSign
                wallEscapeCooldown = 15
            }

            // Плавное инерционное следование с покачиванием (как у ванильного Визера)
            boss.lookControl.setLookAt(target, 35.0f, 35.0f)
            val faceYaw = (Mth.atan2(target.z - boss.z, target.x - boss.x) * (180.0 / Math.PI)).toFloat() - 90.0f
            boss.yRot = faceYaw
            boss.yBodyRot = faceYaw
            boss.yHeadRot = faceYaw

            val t = boss.tickCount.toFloat()
            val swayAngle = anchorAngle + Mth.sin(t * 0.055f) * 0.22
            val bobY = Mth.sin(t * 0.09f) * 0.45
            val anchorDist = 8.5

            val desiredPos = Vec3(
                target.x + cos(swayAngle) * anchorDist,
                target.y + 1.0 + bobY,
                target.z + sin(swayAngle) * anchorDist
            )
            val toAnchor = desiredPos.subtract(boss.position())
            boss.deltaMovement = boss.deltaMovement.scale(0.82).add(toAnchor.scale(0.036))

            if (stageTimer > 0) {
                stageTimer--
                if (attackStage == 1 && stageTimer % 4 == 0) {
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, boss.x, boss.y + 1.6, boss.z, 8, 0.5, 0.6, 0.5, 0.1)
                }
                return
            }

            when (attackStage) {
                // СТАДИЯ 0: Очередь лазеров (в 1 и 2 фазе: 5 выстрелов через 6 тиков; в 3 фазе: 3 выстрела через 12 тиков — как 1, 3 и 5!)
                0 -> {
                    val maxLasers = if (phase == 3) 3 else 5
                    if (burstCount < maxLasers) {
                        fireLaserBeam(serverLevel, target, phase)
                        burstCount++
                        stageTimer = if (phase == 3) 12 else 6
                    } else {
                        attackStage = 1
                        // 1-я фаза -> 1 рывок, 2-я -> 2 рывка, 3-я -> 3 рывка
                        dashesLeftInCycle = phase.coerceIn(1, 3)
                        stageTimer = if (phase == 3) 18 else 14
                        boss.playSound(SoundEvents.BEACON_POWER_SELECT, 1.3f, 0.65f)
                    }
                }

                // СТАДИЯ 1: Рывок строго в текущую точку игрока (target.y + 0.1, без задирания вверх!)
                1 -> {
                    val toTarget = Vec3(target.x - boss.x, (target.y + 0.1) - boss.y, target.z - boss.z)
                    dashDir = if (toTarget.lengthSqr() > 0.01) toTarget.normalize() else boss.lookAngle
                    isDashingNow = true
                    dashTicks = 0
                    hitInDash = false
                    boss.setDashing(true)
                    boss.playSound(SoundEvents.BLAZE_SHOOT, 1.4f, 0.55f)
                }

                // СТАДИЯ 2: Ракетный залп из боковых голов кастомными 3D-ракетами (только 3-я фаза)
                2 -> {
                    if (burstCount < 4) {
                        fireSideRocket(serverLevel, target, leftSide = (burstCount % 2 == 0))
                        burstCount++
                        stageTimer = 14
                    } else {
                        attackStage = 0
                        burstCount = 0
                        stageTimer = 28
                    }
                }
            }
        }

        private fun fireLaserBeam(level: ServerLevel, target: LivingEntity, phase: Int) {
            val look = boss.lookAngle
            val spawnX = boss.x + look.x * 0.9
            val spawnY = boss.y + 2.65
            val spawnZ = boss.z + look.z * 0.9

            val dmg = if (phase == 3) 3.5f else 4.5f
            val boltSpeed = 0.72f

            val bolt = MechaLaserBoltEntity(level, boss, dmg)
            bolt.setPos(spawnX, spawnY, spawnZ)

            val dx = target.x - spawnX
            val dy = target.getY(0.5) - spawnY
            val dz = target.z - spawnZ
            bolt.shoot(dx, dy, dz, boltSpeed, 0.0f)
            level.addFreshEntity(bolt)

            level.playSound(
                null,
                spawnX,
                spawnY,
                spawnZ,
                SoundEvents.BREEZE_SHOOT,
                SoundSource.HOSTILE,
                1.2f,
                1.65f
            )
        }

        private fun fireSideRocket(level: ServerLevel, target: LivingEntity, leftSide: Boolean) {
            val sideOffset = boss.lookAngle.cross(Vec3(0.0, 1.0, 0.0)).normalize().scale(if (leftSide) 1.15 else -1.15)
            val spawnX = boss.x + sideOffset.x
            val spawnY = boss.y + 2.35
            val spawnZ = boss.z + sideOffset.z

            val rocket = MechaRocketEntity(level, boss, 11.0f, target)
            rocket.setPos(spawnX, spawnY, spawnZ)

            val dx = target.x - spawnX
            val dy = target.getY(0.5) - spawnY
            val dz = target.z - spawnZ
            rocket.shoot(dx, dy, dz, 0.68f, 1.0f)
            level.addFreshEntity(rocket)

            level.playSound(null, spawnX, spawnY, spawnZ, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.HOSTILE, 1.3f, 0.75f)
        }
    }
}
