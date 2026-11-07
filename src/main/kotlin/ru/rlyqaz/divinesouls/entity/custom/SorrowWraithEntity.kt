package ru.rlyqaz.divinesouls.entity.custom

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.control.FlyingMoveControl
import net.minecraft.world.entity.ai.goal.*
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation
import net.minecraft.world.entity.ai.navigation.PathNavigation
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import ru.rlyqaz.divinesouls.item.ModItems

class SorrowWraithEntity(entityType: EntityType<SorrowWraithEntity>, level: Level) : Monster(entityType, level) {

    init {
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.LAVA, 0.0f)
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.DAMAGE_FIRE, 0.0f)
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.STICKY_HONEY, 0.0f)
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.DANGER_OTHER, 0.0f)
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.DAMAGE_OTHER, 0.0f)

        // Подняли скорость реакции контроллера до 65 градусов, чтобы ИИ быстрее соображал на стрейфах
        this.moveControl = FlyingMoveControl(this, 65, true)
    }

    override fun aiStep() {
        super.aiStep()
        if (!this.level().isClientSide) {
            val blockBelow = this.level().getBlockState(this.blockPosition())
            val blockUnderFeet = this.level().getBlockState(this.blockPosition().below())

            if (blockBelow.`is`(net.minecraft.world.level.block.Blocks.SOUL_SAND) ||
                blockBelow.`is`(net.minecraft.world.level.block.Blocks.SOUL_SOIL) ||
                blockUnderFeet.`is`(net.minecraft.world.level.block.Blocks.SOUL_SAND) ||
                blockUnderFeet.`is`(net.minecraft.world.level.block.Blocks.SOUL_SOIL)) {

                if (this.deltaMovement.y < 0.1) {
                    this.deltaMovement = this.deltaMovement.add(0.0, 0.08, 0.0)
                }
            }
        }
    }

    override fun dropCustomDeathLoot(level: net.minecraft.server.level.ServerLevel, source: net.minecraft.world.damagesource.DamageSource, hitByPlayer: Boolean) {
        super.dropCustomDeathLoot(level, source, hitByPlayer)
        if (!this.level().isClientSide) {
            if (this.random.nextFloat() < 0.45f) {
                this.spawnAtLocation(net.minecraft.world.item.ItemStack(ModItems.BURNING_SOUL.get(), 1))
            }
        }
    }

    override fun registerGoals() {
        this.goalSelector.addGoal(1, FloatGoal(this))
        this.goalSelector.addGoal(2, MeleeAttackGoal(this, 1.0, false))
        this.goalSelector.addGoal(3, WaterAvoidingRandomStrollGoal(this, 0.8))
        this.goalSelector.addGoal(4, LookAtPlayerGoal(this, Player::class.java, 8.0f))
        this.goalSelector.addGoal(5, RandomLookAroundGoal(this))

        this.targetSelector.addGoal(1, NearestAttackableTargetGoal(this, Player::class.java, true))
    }

    override fun createNavigation(level: Level): PathNavigation {
        val navigation = FlyingPathNavigation(this, level)
        navigation.setCanOpenDoors(false)
        navigation.setCanPassDoors(false)
        return navigation
    }

    override fun travel(travelVector: net.minecraft.world.phys.Vec3) {
        if (this.isControlledByLocalInstance) {
            if (this.isInWater) {
                this.moveRelative(0.02f, travelVector)
                this.move(net.minecraft.world.entity.MoverType.SELF, this.deltaMovement)
                this.deltaMovement = this.deltaMovement.scale(0.8)
            } else if (this.isInLava) {
                this.moveRelative(0.02f, travelVector)
                this.move(net.minecraft.world.entity.MoverType.SELF, this.deltaMovement)
                this.deltaMovement = this.deltaMovement.scale(0.5)
            } else {
                val baseSpeed = this.getAttributeValue(Attributes.MOVEMENT_SPEED).toFloat()
                this.moveRelative(baseSpeed, travelVector)
                this.move(net.minecraft.world.entity.MoverType.SELF, this.deltaMovement)

                // ИСПРАВЛЕНИЕ ЗАНОСА: Заменили 0.91 на 0.75!
                // Теперь игра мгновенно тушит лишнюю инерцию скольжения в воздухе.
                this.deltaMovement = this.deltaMovement.scale(0.75)
            }
        }
        this.calculateEntityAnimation(false)
    }

    override fun causeFallDamage(fallDistance: Float, multiplier: Float, source: net.minecraft.world.damagesource.DamageSource): Boolean {
        return false
    }

    // Переключаем эмбиент: рандомим между твоим первым и вторым кастомным звуком!
    override fun getAmbientSound(): net.minecraft.sounds.SoundEvent? {
        return if (this.random.nextBoolean()) ru.rlyqaz.divinesouls.sound.ModSounds.SORROW_WRAITH_AMBIENT_1.get()
        else ru.rlyqaz.divinesouls.sound.ModSounds.SORROW_WRAITH_AMBIENT_2.get()
    }

    override fun getHurtSound(damageSource: net.minecraft.world.damagesource.DamageSource): net.minecraft.sounds.SoundEvent? {
        return ru.rlyqaz.divinesouls.sound.ModSounds.SORROW_WRAITH_HURT.get()
    }

    override fun getDeathSound(): net.minecraft.sounds.SoundEvent? {
        return ru.rlyqaz.divinesouls.sound.ModSounds.SORROW_WRAITH_DEATH.get()
    }

    override fun getSoundVolume(): Float {
        return 0.6f
    }

    companion object {
        fun createAttributes(): AttributeSupplier.Builder {
            return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.16)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                // ИСПРАВЛЕНИЕ МАНЕВРЕННОСТИ: Подняли скорость полёта с 0.12 до 0.28!
                // Вместе с низким трением это заставит его резко и чётко закладывать виражи за игроком.
                .add(Attributes.FLYING_SPEED, 0.28)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0)
        }
    }
}
