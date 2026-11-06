package ru.rlyqaz.divinesouls.entity.custom

import net.minecraft.world.entity.AnimationState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.FlyingMob
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.21.1 NeoForge)
 * КЛАСС: VoidDragonEntity (Логика, ИИ и Анимации Дракончика)
 * ====================================================================
 */
class VoidDragonEntity(entityType: EntityType<out FlyingMob>, level: Level) : FlyingMob(entityType, level) {

    // ВАНИЛЬНЫЕ АНИМАЦИИ: Создаем контейнеры состояний для анимаций из Blockbench
    val idleAnimationState = AnimationState()
    val flyAnimationState = AnimationState()

    override fun registerGoals() {
        this.goalSelector.addGoal(0, FloatGoal(this))
        this.goalSelector.addGoal(1, LookAtPlayerGoal(this, Player::class.java, 16.0f))
        this.goalSelector.addGoal(2, RandomLookAroundGoal(this))
    }

    // Каждый тик проверяем состояние моба, чтобы включать нужную анимацию
    override fun tick() {
        super.tick()

        if (this.level().isClientSide) {
            // Если моб находится в воздухе/летит — запускаем плавные взмахи крыльев
            if (!this.onGround()) {
                this.idleAnimationState.stop()
                this.flyAnimationState.startIfStopped(this.tickCount)
            } else {
                // Если сел на блок (на перспективу) — включаем анимацию покоя
                this.flyAnimationState.stop()
                this.idleAnimationState.startIfStopped(this.tickCount)
            }
        }
    }

    companion object {
        fun createAttributes(): AttributeSupplier.Builder {
            return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.FLYING_SPEED, 0.4)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
        }
    }
}
