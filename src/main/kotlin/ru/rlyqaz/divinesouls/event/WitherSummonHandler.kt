package ru.rlyqaz.divinesouls.event

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.BlockTags
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.boss.wither.WitherBoss
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.monster.WitherSkeleton
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent
import net.neoforged.neoforge.event.level.BlockEvent
import ru.rlyqaz.divinesouls.block.ModBlocks
import ru.rlyqaz.divinesouls.item.ModItems

object WitherSummonHandler {

    // 1. Заменяем дроп с обычных визер-скелетов (25% шанс на Иссушённую кость + бонус от Добычи)
    @SubscribeEvent
    fun onWitherSkeletonDrops(event: LivingDropsEvent) {
        val entity = event.entity
        if (entity.javaClass == WitherSkeleton::class.java) {
            event.drops.removeIf { itemEntity ->
                itemEntity.item.`is`(Items.WITHER_SKELETON_SKULL)
            }

            var lootingLevel = 0
            val attacker = event.source.entity
            if (attacker is LivingEntity) {
                val enchantmentRegistry = entity.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                val lootingHolder = enchantmentRegistry.getHolderOrThrow(Enchantments.LOOTING)
                lootingLevel = EnchantmentHelper.getItemEnchantmentLevel(lootingHolder, attacker.mainHandItem)
            }

            val dropChance = 0.25f + (lootingLevel * 0.05f)

            if (entity.level().random.nextFloat() < dropChance) {
                val boneStack = ItemStack(ModItems.WITHER_BONE.get(), 1)
                event.drops.add(ItemEntity(entity.level(), entity.x, entity.y, entity.z, boneStack))
            }
        }
    }

    // 2. Перехватываем установку черепа или Блока Печати / Механического ядра Иссушения
    @SubscribeEvent
    fun onBlockPlaced(event: BlockEvent.EntityPlaceEvent) {
        val level = event.level as? Level ?: return
        if (level.isClientSide) return

        val placedState = event.placedBlock
        val pos = event.pos

        val isWitherSkull = placedState.`is`(Blocks.WITHER_SKELETON_SKULL) || placedState.`is`(Blocks.WITHER_SKELETON_WALL_SKULL)
        val isSealBlock = placedState.`is`(ModBlocks.WITHER_SEAL_BLOCK.get())

        if (isWitherSkull || isSealBlock) {
            if (isWitherSkull) {
                val possibleCenters = listOf(pos, pos.north(), pos.south(), pos.east(), pos.west())
                for (center in possibleCenters) {
                    if (trySpawnCustomWither(level, center)) return
                }
            } else {
                trySpawnCustomWither(level, pos)
            }
            return
        }

        // Призыв Мех-Визера (строго в Верхнем мире!)
        val isSkeletonSkull = placedState.`is`(Blocks.SKELETON_SKULL) || placedState.`is`(Blocks.SKELETON_WALL_SKULL)
        val isMechaCore = placedState.`is`(ModBlocks.MECHA_WITHER_CORE_BLOCK.get())

        if (isSkeletonSkull || isMechaCore) {
            if (level.dimension() != Level.OVERWORLD) return
            if (isSkeletonSkull) {
                val possibleCenters = listOf(pos, pos.north(), pos.south(), pos.east(), pos.west())
                for (center in possibleCenters) {
                    if (trySpawnMechaWither(level, center)) return
                }
            } else {
                trySpawnMechaWither(level, pos)
            }
        }
    }

    private fun trySpawnMechaWither(level: Level, corePos: BlockPos): Boolean {
        if (level.dimension() != Level.OVERWORLD) return false
        if (!level.getBlockState(corePos).`is`(ModBlocks.MECHA_WITHER_CORE_BLOCK.get())) return false

        val abyssBlock = ModBlocks.ABYSSAL_BLOCK.get()
        val midBase = corePos.below()
        val bottomBase = midBase.below()
        if (!level.getBlockState(midBase).`is`(abyssBlock)) return false
        if (!level.getBlockState(bottomBase).`is`(abyssBlock)) return false

        val axes = listOf(Pair(Direction.NORTH, Direction.SOUTH), Pair(Direction.WEST, Direction.EAST))

        for ((dir1, dir2) in axes) {
            val sideBase1 = midBase.relative(dir1)
            val sideBase2 = midBase.relative(dir2)
            val skullPos1 = corePos.relative(dir1)
            val skullPos2 = corePos.relative(dir2)

            val hasArms = level.getBlockState(sideBase1).`is`(abyssBlock) &&
                    level.getBlockState(sideBase2).`is`(abyssBlock)

            val hasSkulls = isSkeletonSkull(level, skullPos1) && isSkeletonSkull(level, skullPos2)

            if (hasArms && hasSkulls) {
                val blocksToClear = listOf(corePos, midBase, bottomBase, sideBase1, sideBase2, skullPos1, skullPos2)
                for (clearPos in blocksToClear) {
                    level.setBlock(clearPos, Blocks.AIR.defaultBlockState(), 3)
                    level.levelEvent(2001, clearPos, Block.getId(abyssBlock.defaultBlockState()))
                }

                val mechaWither = ru.rlyqaz.divinesouls.entity.ModEntities.MECHA_WITHER.get().create(level)
                if (mechaWither != null) {
                    mechaWither.moveTo(
                        corePos.x + 0.5,
                        bottomBase.y + 0.55,
                        corePos.z + 0.5,
                        0.0f,
                        0.0f
                    )
                    mechaWither.setPersistenceRequired()
                    level.addFreshEntity(mechaWither)

                    level.playSound(
                        null, corePos.x.toDouble(), corePos.y.toDouble(), corePos.z.toDouble(),
                        SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.3f, 0.55f
                    )
                    level.playSound(
                        null, corePos.x.toDouble(), corePos.y.toDouble(), corePos.z.toDouble(),
                        SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.4f, 0.7f
                    )
                }
                return true
            }
        }
        return false
    }

    private fun isSkeletonSkull(level: Level, pos: BlockPos): Boolean {
        val state = level.getBlockState(pos)
        return state.`is`(Blocks.SKELETON_SKULL) || state.`is`(Blocks.SKELETON_WALL_SKULL)
    }

    // 3. Проверка новой конструкции с Блоком Печати и выдача достижения игрокам рядом
    private fun trySpawnCustomWither(level: Level, sealPos: BlockPos): Boolean {
        if (!level.getBlockState(sealPos).`is`(ModBlocks.WITHER_SEAL_BLOCK.get())) return false

        val midBase = sealPos.below()
        val bottomBase = midBase.below()
        if (!level.getBlockState(midBase).`is`(BlockTags.WITHER_SUMMON_BASE_BLOCKS)) return false
        if (!level.getBlockState(bottomBase).`is`(BlockTags.WITHER_SUMMON_BASE_BLOCKS)) return false

        val axes = listOf(Pair(Direction.NORTH, Direction.SOUTH), Pair(Direction.WEST, Direction.EAST))

        for ((dir1, dir2) in axes) {
            val sideBase1 = midBase.relative(dir1)
            val sideBase2 = midBase.relative(dir2)
            val skullPos1 = sealPos.relative(dir1)
            val skullPos2 = sealPos.relative(dir2)

            val hasArms = level.getBlockState(sideBase1).`is`(BlockTags.WITHER_SUMMON_BASE_BLOCKS) &&
                    level.getBlockState(sideBase2).`is`(BlockTags.WITHER_SUMMON_BASE_BLOCKS)

            val hasSkulls = isWitherSkull(level, skullPos1) && isWitherSkull(level, skullPos2)

            if (hasArms && hasSkulls) {
                val blocksToClear = listOf(sealPos, midBase, bottomBase, sideBase1, sideBase2, skullPos1, skullPos2)
                for (clearPos in blocksToClear) {
                    level.setBlock(clearPos, Blocks.AIR.defaultBlockState(), 3)
                    level.levelEvent(2001, clearPos, Block.getId(Blocks.SOUL_SAND.defaultBlockState()))
                }

                val wither: WitherBoss? = EntityType.WITHER.create(level)
                if (wither != null) {
                    wither.moveTo(
                        sealPos.x + 0.5,
                        bottomBase.y + 0.55,
                        sealPos.z + 0.5,
                        0.0f,
                        0.0f
                    )
                    wither.makeInvulnerable()
                    level.addFreshEntity(wither)

                    // Выдаём игрокам рядом ванильное достижение "Withering Heights"
                    val server = level.server
                    val advancement = server?.advancements?.get(ResourceLocation.withDefaultNamespace("nether/summon_wither"))
                    if (advancement != null) {
                        val nearbyPlayers = level.getEntitiesOfClass(ServerPlayer::class.java, AABB(sealPos).inflate(50.0))
                        for (player in nearbyPlayers) {
                            player.advancements.award(advancement, "summoned")
                        }
                    }

                    level.playSound(
                        null, sealPos.x.toDouble(), sealPos.y.toDouble(), sealPos.z.toDouble(),
                        SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0f, 1.0f
                    )
                }
                return true
            }
        }
        return false
    }

    private fun isWitherSkull(level: Level, pos: BlockPos): Boolean {
        val state = level.getBlockState(pos)
        return state.`is`(Blocks.WITHER_SKELETON_SKULL) || state.`is`(Blocks.WITHER_SKELETON_WALL_SKULL)
    }
}
