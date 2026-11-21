package ru.rlyqaz.divinesouls.entity.custom

import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.RandomSource
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.SpawnGroupData
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
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor
import ru.rlyqaz.divinesouls.item.ModWeapons

class CaveGoblinEntity(
    entityType: EntityType<out Monster>,
    level: Level
) : Monster(entityType, level) {

    companion object {
        fun createAttributes(): AttributeSupplier.Builder {
            return createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 18.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.ARMOR, 1.0)
                .add(Attributes.FOLLOW_RANGE, 18.0)
        }

        fun checkCaveGoblinSpawnRules(
            type: EntityType<CaveGoblinEntity>,
            level: ServerLevelAccessor,
            spawnType: MobSpawnType,
            pos: BlockPos,
            random: RandomSource
        ): Boolean {
            // 1. Строго под землёй (ниже Y=45 и без прямого неба)
            if (pos.y >= 45 || level.canSeeSky(pos)) return false

            // 2. В Тёмных глубинах никто лишний не спавнится (сохраняем ванильную тишину для Вардена и ползунов)
            if (level.getBiome(pos).`is`(net.minecraft.world.level.biome.Biomes.DEEP_DARK)) return false

            // 3. Не спавним толпами: максимум 2 гоблина в радиусе 36 блоков
            val nearby = level.getEntitiesOfClass(
                CaveGoblinEntity::class.java,
                net.minecraft.world.phys.AABB(pos).inflate(36.0)
            )
            if (nearby.size >= 2) return false

            return checkMonsterSpawnRules(type, level, spawnType, pos, random)
        }
    }

    override fun registerGoals() {
        goalSelector.addGoal(0, FloatGoal(this))
        goalSelector.addGoal(1, MeleeAttackGoal(this, 1.15, false))
        goalSelector.addGoal(2, WaterAvoidingRandomStrollGoal(this, 0.9))
        goalSelector.addGoal(3, LookAtPlayerGoal(this, Player::class.java, 8.0f))
        goalSelector.addGoal(4, RandomLookAroundGoal(this))

        targetSelector.addGoal(1, HurtByTargetGoal(this))
        targetSelector.addGoal(2, NearestAttackableTargetGoal(this, Player::class.java, true))
    }

    override fun populateDefaultEquipmentSlots(random: RandomSource, difficulty: DifficultyInstance) {
        super.populateDefaultEquipmentSlots(random, difficulty)
        // С шансом 75% пещерный гоблин спавнится с дубинкой с каменными шипами в правой руке
        if (random.nextFloat() < 0.75f) {
            setItemSlot(EquipmentSlot.MAINHAND, ItemStack(ModWeapons.SPIKED_STONE_CLUB.get()))
        }
    }

    @Suppress("DEPRECATION")
    override fun finalizeSpawn(
        level: ServerLevelAccessor,
        difficulty: DifficultyInstance,
        spawnType: MobSpawnType,
        spawnGroupData: SpawnGroupData?
    ): SpawnGroupData? {
        val data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData)
        populateDefaultEquipmentSlots(level.random, difficulty)
        // Небольшой шанс выпадения самой дубинки из руки (12%)
        setDropChance(EquipmentSlot.MAINHAND, 0.12f)
        return data
    }

    override fun getAmbientSound(): SoundEvent = SoundEvents.PILLAGER_AMBIENT

    override fun getHurtSound(damageSource: DamageSource): SoundEvent = SoundEvents.PILLAGER_HURT

    override fun getDeathSound(): SoundEvent = SoundEvents.PILLAGER_DEATH

    override fun getVoicePitch(): Float = 1.45f

    override fun doHurtTarget(target: net.minecraft.world.entity.Entity): Boolean {
        val hasClub = mainHandItem.`is`(ModWeapons.SPIKED_STONE_CLUB.get())
        val finalDamage = if (hasClub) 5.5f else 3.0f

        val hurtSuccess = target.hurt(damageSources().mobAttack(this), finalDamage)
        if (hurtSuccess && target is net.minecraft.world.entity.LivingEntity) {
            if (hasClub) {
                val look = lookAngle
                target.knockback(0.35, -look.x, -look.z)
                if (random.nextFloat() < 0.30f) {
                    target.addEffect(
                        net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                            40,
                            0
                        )
                    )
                }
            }
        }
        return hurtSuccess
    }
}
