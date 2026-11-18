package ru.rlyqaz.divinesouls.entity.custom

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.Mth
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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.phys.Vec3

class SwampSpiritEntity(
    entityType: EntityType<out Monster>,
    level: Level
) : Monster(entityType, level) {

    private var moveDirection: Direction = Direction.NORTH
    private var moveTicksLeft: Int = 0
    private var attackCooldown: Int = 0

    companion object {
        fun createAttributes(): AttributeSupplier.Builder {
            return createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 36.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.ATTACK_DAMAGE, 11.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4)
                .add(Attributes.FOLLOW_RANGE, 24.0)
        }

        fun checkSwampSpiritSpawnRules(
            type: EntityType<SwampSpiritEntity>,
            level: ServerLevelAccessor,
            spawnType: MobSpawnType,
            pos: BlockPos,
            random: RandomSource
        ): Boolean {
            if (level.difficulty == Difficulty.PEACEFUL) return false

            // Получаем измерение Энда, потому что dragonFight хранится только там!
            val endLevel = level.level.server.getLevel(Level.END)
            val dragonFight = endLevel?.dragonFight
            if (dragonFight == null || !dragonFight.hasPreviouslyKilledDragon()) {
                return false
            }

            // Не спавнимся прямо внутри воды/блока — только в воздухе (включая воздух над болотной водой)
            if (!level.getBlockState(pos).isAir || !level.getBlockState(pos.above()).isAir) {
                return false
            }

            // Проверяем темноту (уровень света <= 7)
            if (!Monster.isDarkEnoughToSpawn(level, pos, random)) {
                return false
            }

            // Ограничиваем до 2 духов в радиусе 28 блоков
            val nearbySpirits = level.getEntitiesOfClass(
                SwampSpiritEntity::class.java,
                net.minecraft.world.phys.AABB(pos).inflate(28.0)
            )
            return nearbySpirits.size < 2
        }
    }

    init {
        isNoGravity = true
        xpReward = 12
    }

    override fun registerGoals() {
        targetSelector.addGoal(1, HurtByTargetGoal(this))
        targetSelector.addGoal(2, NearestAttackableTargetGoal(this, Player::class.java, true))
    }

    override fun customServerAiStep() {
        super.customServerAiStep()

        if (attackCooldown > 0) {
            attackCooldown--
        }

        // 1. В customServerAiStep(): упираемся в любой твёрдый блок (включая листву) — сразу меняем ось!
        val nextPos = blockPosition().relative(moveDirection)
        val nextState = level().getBlockState(nextPos)
        val hitSolidBlock = !nextState.isAir && nextState.blocksMotion()

        if (moveTicksLeft <= 0 || hitSolidBlock) {
            pickNextAxisDirection()
            moveTicksLeft = 8 + random.nextInt(7) // Быстрые, но осмысленные зигзаги каждые 0.4–0.7 сек
        } else {
            moveTicksLeft--
        }

        val speed = if (target != null) 0.38 else 0.16
        deltaMovement = Vec3(
            moveDirection.stepX * speed,
            moveDirection.stepY * speed,
            moveDirection.stepZ * speed
        )

        // Разворачиваем модель носом строго по направлению текущего движения
        updateFacingFromDirection()

        // Контактный урон при столкновении с целью в полёте
        val currentTarget = target
        if (currentTarget != null && currentTarget.isAlive && attackCooldown <= 0) {
            if (boundingBox.inflate(0.3).intersects(currentTarget.boundingBox)) {
                if (doHurtTarget(currentTarget)) {
                    attackCooldown = 14
                    if (currentTarget is LivingEntity) {
                        currentTarget.addEffect(MobEffectInstance(MobEffects.DARKNESS, 60, 0))
                    }
                    moveTicksLeft = 0
                }
            }
        }
    }

    // 2. В isDirectionClear(): считаем свободными только воздух и неблокирующие растения (траву/цветы)
    private fun isDirectionClear(dir: Direction): Boolean {
        val checkPos = blockPosition().relative(dir)
        val state = level().getBlockState(checkPos)
        if (!state.isAir && state.blocksMotion()) return false

        // Не опускаемся ниже 1 блока над землёй или водой
        if (dir == Direction.DOWN) {
            val belowTwo = level().getBlockState(blockPosition().below(2))
            if (!belowTwo.isAir && (belowTwo.blocksMotion() || !belowTwo.fluidState.isEmpty)) {
                return false
            }
        }
        return true
    }

    private fun pickNextAxisDirection() {
        val currentTarget = target
        if (currentTarget == null || !currentTarget.isAlive) {
            val groundBelow = level().getBlockState(blockPosition().below())
            if (!groundBelow.isAir && (groundBelow.blocksMotion() || !groundBelow.fluidState.isEmpty)) {
                moveDirection = Direction.UP
                return
            }

            val horizontalDirs = arrayOf(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
                .filter { isDirectionClear(it) }
            moveDirection = if (horizontalDirs.isNotEmpty()) {
                horizontalDirs[random.nextInt(horizontalDirs.size)]
            } else {
                Direction.UP
            }
            return
        }

        val dx = currentTarget.x - x
        val dy = (currentTarget.y + currentTarget.eyeHeight * 0.6) - y
        val dz = currentTarget.z - z

        // Собираем направления строго к игроку, отсортированные по величине дистанции по каждой оси
        val weightedDirs = mutableListOf<Pair<Direction, Double>>()
        if (kotlin.math.abs(dx) > 0.35) {
            weightedDirs.add((if (dx > 0) Direction.EAST else Direction.WEST) to kotlin.math.abs(dx))
        }
        if (kotlin.math.abs(dy) > 0.35) {
            weightedDirs.add((if (dy > 0) Direction.UP else Direction.DOWN) to kotlin.math.abs(dy))
        }
        if (kotlin.math.abs(dz) > 0.35) {
            weightedDirs.add((if (dz > 0) Direction.SOUTH else Direction.NORTH) to kotlin.math.abs(dz))
        }

        // Оставляем только свободные от стволов деревьев направления
        val clearTowardTarget = weightedDirs
            .filter { isDirectionClear(it.first) }
            .sortedByDescending { it.second }
            .map { it.first }

        if (clearTowardTarget.isNotEmpty()) {
            // Выбираем свободную ось к игроку, стараясь сменить ось ради зигзага шалкера
            val perpendicular = clearTowardTarget.filter { it.axis != moveDirection.axis }
            moveDirection = if (perpendicular.isNotEmpty()) {
                perpendicular.first()
            } else {
                clearTowardTarget.first()
            }
            return
        }

        // Если прямой путь по нужным осям перекрыт стволом дерева — облетаем в любую свободную сторону (в приоритете ВВЕРХ!)
        val escapeOrder = listOf(Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.DOWN)
        val freeEscape = escapeOrder.filter { isDirectionClear(it) && it != moveDirection.opposite }
        moveDirection = if (freeEscape.isNotEmpty()) {
            freeEscape.first()
        } else {
            Direction.UP
        }
    }

    private fun updateFacingFromDirection() {
        val vx = moveDirection.stepX.toDouble()
        val vy = moveDirection.stepY.toDouble()
        val vz = moveDirection.stepZ.toDouble()

        val horizDist = kotlin.math.sqrt(vx * vx + vz * vz)

        // Меняем горизонтальный поворот только когда летим по X или Z (при полёте вверх/вниз сохраняем прошлый поворот)
        if (horizDist > 0.01) {
            val targetYaw = (Mth.atan2(vz, vx) * (180.0 / Math.PI)).toFloat() - 90.0f
            yRot = targetYaw
            yRotO = targetYaw
            yBodyRot = targetYaw
            yBodyRotO = targetYaw
            yHeadRot = targetYaw
            yHeadRotO = targetYaw
        }

        val targetPitch = when (moveDirection) {
            Direction.UP -> -90.0f
            Direction.DOWN -> 90.0f
            else -> 0.0f
        }
        xRot = targetPitch
        xRotO = targetPitch
    }

    override fun aiStep() {
        super.aiStep()
        // Призрачный синий шлейф частиц за духом
        if (level().isClientSide && random.nextInt(2) == 0) {
            level().addParticle(
                ParticleTypes.SOUL_FIRE_FLAME,
                x + (random.nextDouble() - 0.5) * 0.35,
                y + 0.25 + (random.nextDouble() - 0.5) * 0.35,
                z + (random.nextDouble() - 0.5) * 0.35,
                0.0,
                0.01,
                0.0
            )
        }
    }

    override fun causeFallDamage(fallDistance: Float, multiplier: Float, source: DamageSource): Boolean = false

    override fun getAmbientSound(): SoundEvent = SoundEvents.VEX_AMBIENT

    override fun getHurtSound(damageSource: DamageSource): SoundEvent = SoundEvents.VEX_HURT

    override fun getDeathSound(): SoundEvent = SoundEvents.VEX_DEATH

    override fun getVoicePitch(): Float = 0.7f
}
