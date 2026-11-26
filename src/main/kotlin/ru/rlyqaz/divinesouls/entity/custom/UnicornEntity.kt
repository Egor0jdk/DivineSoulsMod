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
import net.minecraft.util.Mth
import net.minecraft.util.RandomSource
import net.minecraft.world.Difficulty
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.EnumSet

class UnicornEntity(
    entityType: EntityType<out Monster>,
    level: Level
) : Monster(entityType, level) {

    init {
        xpReward = 14
    }

    companion object {
        // 0 = сближение, 1 = подготовка (опустил рог), 2 = таранный рывок, 3 = короткая передышка
        private val DATA_CHARGE_STATE: EntityDataAccessor<Int> =
            SynchedEntityData.defineId(UnicornEntity::class.java, EntityDataSerializers.INT)

        fun createAttributes(): AttributeSupplier.Builder {
            return createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 44.0)
                .add(Attributes.MOVEMENT_SPEED, 0.31)
                .add(Attributes.ATTACK_DAMAGE, 11.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.55)
                .add(Attributes.STEP_HEIGHT, 1.1)
                .add(Attributes.FOLLOW_RANGE, 26.0)
        }

        fun checkUnicornSpawnRules(
            type: EntityType<UnicornEntity>,
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

            if (!level.canSeeSky(pos) || level.getRawBrightness(pos, 0).compareTo(9) == -1) return false
            if (!level.getBlockState(pos.below()).`is`(Blocks.GRASS_BLOCK)) return false

            val nearby = level.getEntitiesOfClass(
                UnicornEntity::class.java,
                AABB(pos).inflate(48.0)
            )
            return nearby.isEmpty()
        }
    }

    var chargeState: Int
        get() = entityData.get(DATA_CHARGE_STATE)
        set(value) = entityData.set(DATA_CHARGE_STATE, value)

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(DATA_CHARGE_STATE, 0)
    }

    override fun registerGoals() {
        goalSelector.addGoal(0, FloatGoal(this))
        goalSelector.addGoal(1, UnicornCombatGoal(this))
        goalSelector.addGoal(2, WaterAvoidingRandomStrollGoal(this, 0.9))
        goalSelector.addGoal(3, LookAtPlayerGoal(this, Player::class.java, 10.0f))
        goalSelector.addGoal(4, RandomLookAroundGoal(this))

        targetSelector.addGoal(1, HurtByTargetGoal(this))
        targetSelector.addGoal(2, NearestAttackableTargetGoal(this, Player::class.java, true))
    }

    override fun getWalkTargetValue(pos: BlockPos, level: LevelReader): Float = 1.0f

    override fun getAmbientSound(): SoundEvent = SoundEvents.HORSE_AMBIENT

    override fun getHurtSound(damageSource: DamageSource): SoundEvent = SoundEvents.HORSE_HURT

    override fun getDeathSound(): SoundEvent = SoundEvents.HORSE_DEATH

    override fun getVoicePitch(): Float = 1.15f

    private class UnicornCombatGoal(private val unicorn: UnicornEntity) : Goal() {
        private var timer = 0
        private var biteCooldown = 0
        private var chargeDir = Vec3.ZERO
        private var hitDuringCharge = false

        init {
            flags = EnumSet.of(Flag.MOVE, Flag.LOOK)
        }

        override fun canUse(): Boolean {
            val target = unicorn.target
            return target != null && target.isAlive
        }

        override fun start() {
            unicorn.chargeState = 0
            timer = 0
            biteCooldown = 0
        }

        override fun stop() {
            unicorn.chargeState = 0
            unicorn.navigation.stop()
        }

        private fun tryCloseBite(target: LivingEntity, serverLevel: ServerLevel): Boolean {
            if (biteCooldown > 0) return false
            if (unicorn.distanceToSqr(target) > 6.76) return false // Радиус укуса ~2.6 блока

            biteCooldown = 18
            unicorn.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true)
            target.hurt(unicorn.damageSources().mobAttack(unicorn), 7.0f)

            val pushVec = Vec3(target.x - unicorn.x, 0.0, target.z - unicorn.z)
            if (pushVec.lengthSqr() > 0.001) {
                val norm = pushVec.normalize()
                target.push(norm.x * 0.65, 0.3, norm.z * 0.65)
                target.hurtMarked = true
            }

            serverLevel.playSound(
                null, unicorn.x, unicorn.y, unicorn.z,
                SoundEvents.HORSE_ANGRY, SoundSource.HOSTILE,
                1.0f, 1.35f
            )
            return true
        }

        override fun tick() {
            val target = unicorn.target ?: return
            val serverLevel = unicorn.level() as? ServerLevel ?: return

            if (biteCooldown > 0) {
                biteCooldown--
            }

            val distSq = unicorn.distanceToSqr(target)

            when (unicorn.chargeState) {
                // ФАЗА 0: Сближение с целью (если вплотную — кусает; если на дистанции 3..14 блоков — начинает подготовку к рывку)
                0 -> {
                    unicorn.lookControl.setLookAt(target, 30.0f, 30.0f)

                    if (distSq <= 6.76) {
                        unicorn.navigation.stop()
                        tryCloseBite(target, serverLevel)
                    } else if (distSq > 196.0) {
                        unicorn.navigation.moveTo(target, 1.2)
                    } else {
                        unicorn.navigation.stop()
                        unicorn.chargeState = 1
                        timer = 18
                        unicorn.playSound(SoundEvents.HORSE_ANGRY, 1.1f, 1.2f)
                    }
                }

                // ФАЗА 1: Подготовка к рывку (если игрок подошёл вплотную — кусает и сбрасывает подготовку, чтобы начать её заново когда игрок отойдёт!)
                1 -> {
                    unicorn.navigation.stop()
                    unicorn.lookControl.setLookAt(target, 45.0f, 45.0f)

                    if (distSq <= 6.76) {
                        tryCloseBite(target, serverLevel)
                        unicorn.chargeState = 0
                        timer = 0
                        return
                    }

                    val toTarget = Vec3(target.x - unicorn.x, 0.0, target.z - unicorn.z)
                    if (toTarget.lengthSqr() > 0.001) {
                        chargeDir = toTarget.normalize()
                        val yaw = (Mth.atan2(chargeDir.z, chargeDir.x) * (180.0 / Math.PI)).toFloat() - 90.0f
                        unicorn.yRot = yaw
                        unicorn.yBodyRot = yaw
                        unicorn.yHeadRot = yaw
                    }

                    if (timer % 4 == 0) {
                        serverLevel.sendParticles(
                            ParticleTypes.END_ROD,
                            unicorn.x, unicorn.y + 0.3, unicorn.z,
                            3, 0.35, 0.1, 0.35, 0.02
                        )
                    }

                    timer--
                    if (timer <= 0) {
                        unicorn.chargeState = 2
                        timer = 22
                        hitDuringCharge = false
                        serverLevel.playSound(
                            null, unicorn.x, unicorn.y, unicorn.z,
                            SoundEvents.HORSE_GALLOP, SoundSource.HOSTILE,
                            1.2f, 1.3f
                        )
                    }
                }

                // ФАЗА 2: Таранный рывок (благодаря STEP_HEIGHT = 1.1 легко залетает на уступы высотой 1 блок!)
                2 -> {
                    val speed = 0.90
                    unicorn.deltaMovement = Vec3(
                        chargeDir.x * speed,
                        unicorn.deltaMovement.y,
                        chargeDir.z * speed
                    )
                    val yaw = (Mth.atan2(chargeDir.z, chargeDir.x) * (180.0 / Math.PI)).toFloat() - 90.0f
                    unicorn.yRot = yaw
                    unicorn.yBodyRot = yaw
                    unicorn.yHeadRot = yaw

                    serverLevel.sendParticles(
                        ParticleTypes.CLOUD,
                        unicorn.x, unicorn.y + 0.2, unicorn.z,
                        2, 0.2, 0.05, 0.2, 0.01
                    )

                    if (!hitDuringCharge) {
                        val goreBox = unicorn.boundingBox.expandTowards(chargeDir.scale(0.95)).inflate(0.4)
                        val victims = serverLevel.getEntitiesOfClass(LivingEntity::class.java, goreBox) { e ->
                            e != unicorn && e !is UnicornEntity && e.isAlive
                        }
                        for (victim in victims) {
                            hitDuringCharge = true
                            victim.hurt(unicorn.damageSources().mobAttack(unicorn), 11.5f)
                            victim.push(chargeDir.x * 1.15, 0.45, chargeDir.z * 1.15)
                            victim.hurtMarked = true
                            serverLevel.playSound(
                                null, victim.x, victim.y, victim.z,
                                SoundEvents.TRIDENT_HIT, SoundSource.HOSTILE,
                                1.1f, 1.25f
                            )
                        }
                    }

                    timer--
                    // Прерываем рывок только по таймеру, при попадании или при упоре в стену 2+ блока (когда STEP_HEIGHT не справился)
                    val hitTallWall = unicorn.horizontalCollision && unicorn.deltaMovement.horizontalDistanceSqr() < 0.04
                    if (timer <= 0 || hitTallWall || hitDuringCharge) {
                        unicorn.chargeState = 3
                        timer = 25
                        unicorn.deltaMovement = unicorn.deltaMovement.scale(0.25)
                        unicorn.playSound(SoundEvents.HORSE_BREATHE, 1.0f, 0.9f)
                    }
                }

                // ФАЗА 3: Короткая передышка (1.25 сек), но если игрок лезет вплотную — кусает!
                3 -> {
                    unicorn.navigation.stop()
                    unicorn.lookControl.setLookAt(target, 20.0f, 20.0f)

                    if (distSq <= 6.76) {
                        tryCloseBite(target, serverLevel)
                    }

                    timer--
                    if (timer <= 0) {
                        unicorn.chargeState = 0
                    }
                }
            }
        }
    }
}
