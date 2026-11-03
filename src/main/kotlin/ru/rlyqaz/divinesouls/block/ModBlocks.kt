package ru.rlyqaz.divinesouls.block

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import net.neoforged.neoforge.registries.DeferredRegister
import ru.rlyqaz.divinesouls.DivineSoulsMod
import java.util.function.Supplier

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.21.1 NeoForge)
 * КЛАСС: ModBlocks (Блоки Ксен-биомов и Кристальная руда)
 * ====================================================================
 */
object ModBlocks {
    private val REGISTRY: DeferredRegister.Blocks = DivineSoulsMod.BLOCKS

    // Формы для кустов и саженцев (SAPLING_SHAPE — видимая моделька, EMPTY — проходимая коллизия)
    private val SAPLING_SHAPE: VoxelShape = Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0)

    // 1. Кристальная руда Энда
    val END_CRYSTAL_ORE: Supplier<Block> = registerBlock("end_crystal_ore") {
        Block(BlockBehaviour.Properties.of().destroyTime(5.0f).explosionResistance(6.0f).sound(SoundType.STONE))
    }

    // 2. Дёрн Ксена
    val END_GRASS_BLOCK: Supplier<Block> = registerBlock("end_grass_block") {
        Block(BlockBehaviour.Properties.of().destroyTime(0.6f).sound(SoundType.GRASS))
    }

    // 3. Кастомная трава Ксена (Края)
    val END_BUSH_GRASS: Supplier<Block> = registerBlock("end_bush_grass") {
        object : FlowerBlock(MobEffects.DIG_SPEED, 0.0f, BlockBehaviour.Properties.of().instabreak().sound(SoundType.GRASS)) {
            override fun mayPlaceOn(state: BlockState, level: BlockGetter, pos: BlockPos): Boolean = state.`is`(END_GRASS_BLOCK.get())
            override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = SAPLING_SHAPE
            override fun getCollisionShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = Shapes.empty()

            // Нативный метод заменяемости блока для 1.21.1
            override fun canBeReplaced(state: BlockState, context: net.minecraft.world.item.context.BlockPlaceContext): Boolean {
                return true
            }
        }
    }

    // 4. Иссушенный куст Края
    val WITHERED_END_BUSH: Supplier<Block> = registerBlock("wither_end_bush") {
        object : FlowerBlock(MobEffects.WITHER, 5.0f, BlockBehaviour.Properties.of().instabreak().sound(SoundType.GRASS)) {
            override fun mayPlaceOn(state: BlockState, level: BlockGetter, pos: BlockPos): Boolean = state.`is`(Blocks.END_STONE)
            override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = SAPLING_SHAPE
            override fun getCollisionShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = Shapes.empty()

            override fun canBeReplaced(state: BlockState, context: net.minecraft.world.item.context.BlockPlaceContext): Boolean {
                return true
            }
        }
    }

    // 5. Дерево Края
    val END_WOOD_LOG: Supplier<Block> = registerBlock("end_wood_log") {
        RotatedPillarBlock(BlockBehaviour.Properties.of().destroyTime(2.0f).sound(SoundType.WOOD))
    }

    // 6. Доски дерева Края
    val END_WOOD_PLANKS: Supplier<Block> = registerBlock("end_wood_planks") {
        Block(BlockBehaviour.Properties.of().destroyTime(2.0f).sound(SoundType.WOOD))
    }

    // 7. Листва дерева Края
    val END_WOOD_LEAVES: Supplier<Block> = registerBlock("end_wood_leaves") {
        LeavesBlock(BlockBehaviour.Properties.of().destroyTime(0.2f).randomTicks().sound(SoundType.GRASS).noOcclusion())
    }

    // 8. Саженец дерева Края
    val END_SAPLING: Supplier<Block> = registerBlock("end_sapling") {
        object : Block(BlockBehaviour.Properties.of().randomTicks().instabreak().sound(SoundType.GRASS)), BonemealableBlock {
            override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = SAPLING_SHAPE
            override fun getCollisionShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = Shapes.empty()

            override fun canBeReplaced(state: BlockState, context: net.minecraft.world.item.context.BlockPlaceContext): Boolean {
                return true
            }

            override fun randomTick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
                if (level.getBlockState(pos.below()).`is`(END_GRASS_BLOCK.get())) {
                    if (random.nextInt(7) == 0) {
                        ru.rlyqaz.divinesouls.worldgen.EndTree.growTree(level, pos, random)
                    }
                }
            }

            override fun isValidBonemealTarget(level: net.minecraft.world.level.LevelReader, pos: BlockPos, state: BlockState): Boolean {
                return level.getBlockState(pos.below()).`is`(END_GRASS_BLOCK.get())
            }

            override fun isBonemealSuccess(level: net.minecraft.world.level.Level, random: RandomSource, pos: BlockPos, state: BlockState): Boolean {
                return true
            }

            override fun performBonemeal(level: ServerLevel, random: RandomSource, pos: BlockPos, state: BlockState) {
                ru.rlyqaz.divinesouls.worldgen.EndTree.growTree(level, pos, random)
            }
        }
    }

    // ====================================================================
    // АВТОМАТИЧЕСКАЯ РЕГИСТРАЦИЯ БЛОКОВ И ИХ ПРЕДМЕТОВ (BLOCK ITEMS)
    // ====================================================================
    private fun <T : Block> registerBlock(name: String, blockSupplier: () -> T): Supplier<T> {
        val block = REGISTRY.register(name, blockSupplier)

        // Явно передаем Supplier<Item>, чтобы убрать неопределенность перегрузки методов NeoForge
        DivineSoulsMod.ITEMS.register(name, java.util.function.Supplier<Item> {
            BlockItem(block.get(), Item.Properties())
        })

        return block
    }
    // Этот метод связывает модуль блоков с главным классом через шину NeoForge
    fun register(eventBus: net.neoforged.bus.api.IEventBus) {
        // Оставляем пустым, вызов просто триггерит загрузку статических полей класса
    }
}
