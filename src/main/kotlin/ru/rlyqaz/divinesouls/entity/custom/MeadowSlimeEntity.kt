package ru.rlyqaz.divinesouls.entity.custom

import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.util.RandomSource
import net.minecraft.world.Difficulty
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.SpawnGroupData
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.monster.Slime
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.level.block.Blocks

class MeadowSlimeEntity(
    entityType: EntityType<out Slime>,
    level: Level
) : Slime(entityType, level) {

    companion object {
        fun createAttributes(): AttributeSupplier.Builder {
            return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
        }

        // Правило спавна: днём на траве в мирной или выше сложности
        fun checkMeadowSlimeSpawnRules(
            entityType: EntityType<MeadowSlimeEntity>,
            level: ServerLevelAccessor,
            spawnType: MobSpawnType,
            pos: BlockPos,
            random: RandomSource
        ): Boolean {
            if (level.difficulty == Difficulty.PEACEFUL) return false
            // Спавнится только при дневном свете и под открытым небом на траве
            val isDaylight = level.getRawBrightness(pos, 0) >= 9
            val onGrass = level.getBlockState(pos.below()).`is`(Blocks.GRASS_BLOCK)
            return isDaylight && onGrass && checkMobSpawnRules(entityType, level, spawnType, pos, random)
        }
    }

    // При естественном спавне делаем его мелким (1) или средним (2), чтобы не душил игрока на старте
    override fun finalizeSpawn(
        level: ServerLevelAccessor,
        difficulty: DifficultyInstance,
        spawnType: MobSpawnType,
        spawnGroupData: SpawnGroupData?
    ): SpawnGroupData? {
        val data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData)
        val chosenSize = if (this.random.nextFloat() < 0.7f) 1 else 2
        this.setSize(chosenSize, true)
        return data
    }

    // Частицы при прыжках (травяные/зелёные брызги)
    override fun getParticleType(): ParticleOptions {
        return ParticleTypes.ITEM_SLIME
    }

    // Разрешаем нанесение урона игроку на любом размере (в ваниле мелкий слизень размера 1 не бьёт!)
    override fun isDealsDamage(): Boolean {
        return this.isEffectiveAi
    }

    // Отключаем ванильную таблицу лута слизня, чтобы не заваливать инвентарь слизью
    override fun getDefaultLootTable(): net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> {
        return net.minecraft.world.level.storage.loot.BuiltInLootTables.EMPTY
    }

    // Редкий дроп: 1 сгусток слизи с шансом 15% (только при окончательной смерти мелкого слизня)
    override fun dropCustomDeathLoot(
        level: net.minecraft.server.level.ServerLevel,
        damageSource: net.minecraft.world.damagesource.DamageSource,
        recentlyHit: Boolean
    ) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit)
        if (this.size == 1 && this.random.nextFloat() < 0.15f) {
            this.spawnAtLocation(net.minecraft.world.item.Items.SLIME_BALL)
        }
    }

    // Усиливаем урон на +1.0f: мелкий слизень (size 1) теперь сносит ровно 1 сердце (2.0 урона), а средний — 1.5 сердца
    override fun getAttackDamage(): Float {
        return super.getAttackDamage() + 1.0f
    }
}
