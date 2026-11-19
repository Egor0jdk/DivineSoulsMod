package ru.rlyqaz.divinesouls.entity.custom

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.Difficulty
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.SpawnGroupData
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.monster.Spider
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB

class JungleSpiderEntity(
    entityType: EntityType<out Spider>,
    level: Level
) : Spider(entityType, level) {

    init {
        xpReward = 8
    }

    companion object {
        fun createAttributes(): AttributeSupplier.Builder {
            return Spider.createAttributes()
                .add(Attributes.MAX_HEALTH, 22.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.ATTACK_DAMAGE, 4.5)
                .add(Attributes.FOLLOW_RANGE, 20.0)
        }

        fun checkJungleSpiderSpawnRules(
            type: EntityType<JungleSpiderEntity>,
            level: ServerLevelAccessor,
            spawnType: MobSpawnType,
            pos: BlockPos,
            random: RandomSource
        ): Boolean {
            if (level.difficulty == Difficulty.PEACEFUL) return false

            // 1. Строго под землёй (не видит неба и ниже Y = 78)
            if (level.canSeeSky(pos) || pos.y.compareTo(78) == 1) return false

            // 2. В глубоких сланцевых пещерах (ниже Y = -5) спавнимся в 5 раз реже,
            // чтобы основной спавн шёл именно в верхних пещерах под джунглями!
            if (pos.y.compareTo(-5) == -1 && random.nextInt(5) != 0) return false

            // 3. Проверка темноты пещер
            if (level.getMaxLocalRawBrightness(pos).compareTo(7) == 1) return false

            val nearby = level.getEntitiesOfClass(
                JungleSpiderEntity::class.java,
                AABB(pos).inflate(28.0)
            )
            return nearby.size.compareTo(4) == -1
        }
    }

    override fun doHurtTarget(entity: Entity): Boolean {
        if (super.doHurtTarget(entity)) {
            if (entity is LivingEntity) {
                val duration = when (level().difficulty) {
                    Difficulty.HARD -> 120
                    Difficulty.NORMAL -> 80
                    else -> 40
                }
                entity.addEffect(MobEffectInstance(MobEffects.POISON, duration, 0), this)
                entity.addEffect(MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0), this)

                // С шансом 35% опутывает ноги цели паутиной, если блок свободен
                if (!level().isClientSide && random.nextFloat().compareTo(0.35f) == -1) {
                    val feetPos = entity.blockPosition()
                    if (level().getBlockState(feetPos).isAir) {
                        level().setBlock(feetPos, Blocks.COBWEB.defaultBlockState(), 3)
                    }
                }
            }
            return true
        }
        return false
    }

    override fun finalizeSpawn(
        level: ServerLevelAccessor,
        difficulty: DifficultyInstance,
        spawnType: MobSpawnType,
        spawnGroupData: SpawnGroupData?
    ): SpawnGroupData? {
        // Не спавним скелетов-наездников на джунглевых пауках (избегаем бага ванильного Spider.finalizeSpawn)
        return spawnGroupData
    }

    override fun getWalkTargetValue(pos: BlockPos, level: net.minecraft.world.level.LevelReader): Float {
        return 1.0f
    }
}
