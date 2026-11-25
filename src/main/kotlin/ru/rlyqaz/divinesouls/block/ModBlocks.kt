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
import net.minecraft.sounds.SoundEvents
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
        Block(
            BlockBehaviour.Properties.of()
                .destroyTime(12.0f)
                .explosionResistance(6.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE)
        )
    }

    // Блок Самоцвета Энда (9 самоцветов Энда)
    val END_CRYSTAL_BLOCK: Supplier<Block> = registerBlock("end_crystal_block") {
        Block(
            BlockBehaviour.Properties.of()
                .mapColor(net.minecraft.world.level.material.MapColor.COLOR_PURPLE)
                .strength(6.0f, 12.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL)
                .lightLevel { 6 }
        )
    }

    // 2. Дёрн Края (Полный аналог Нилия: звуки камня/травы, требует кирку, превращается в эндерняк под сплошным блоком)
    val END_GRASS_BLOCK: Supplier<Block> = registerBlock("end_grass_block") {
        val endGrassSound = SoundType(
            1.0f,
            1.0f,
            SoundEvents.STONE_BREAK,
            SoundEvents.GRASS_STEP,
            SoundEvents.STONE_PLACE,
            SoundEvents.STONE_HIT,
            SoundEvents.GRASS_FALL
        )
        object : Block(
            BlockBehaviour.Properties.of()
                .strength(3.0f, 9.0f)
                .requiresCorrectToolForDrops()
                .randomTicks() // Включаем случайные тики для проверки света сверху
                .sound(endGrassSound)
        ) {
            override fun randomTick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
                val abovePos = pos.above()
                val aboveState = level.getBlockState(abovePos)
                // Если сверху стоит сплошной блок, перекрывающий свет — дёрн умирает и становится обычным эндерняком
                if (aboveState.isSolidRender(level, abovePos)) {
                    level.setBlockAndUpdate(pos, Blocks.END_STONE.defaultBlockState())
                }
            }
        }
    }

    // 3. Кастомная трава Ксена (Края)
    val END_BUSH_GRASS: Supplier<Block> = registerBlock("end_bush_grass") {
        object : FlowerBlock(MobEffects.DIG_SPEED, 0.0f, BlockBehaviour.Properties.of().instabreak().sound(SoundType.GRASS)) {
            override fun mayPlaceOn(state: BlockState, level: BlockGetter, pos: BlockPos): Boolean = state.`is`(END_GRASS_BLOCK.get())
            override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = SAPLING_SHAPE
            override fun getCollisionShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = Shapes.empty()
            override fun canBeReplaced(state: BlockState, context: net.minecraft.world.item.context.BlockPlaceContext): Boolean = true

            // ТОЧЕЧНЫЙ ФИКС ТРАВЫ-ПРИЗРАКА: Уничтожает куст, если опора под ним исчезла
            override fun canSurvive(state: BlockState, level: net.minecraft.world.level.LevelReader, pos: BlockPos): Boolean {
                return this.mayPlaceOn(level.getBlockState(pos.below()), level, pos.below())
            }
        }
    }

    // 4. Иссушенный куст Края
    val WITHERED_END_BUSH: Supplier<Block> = registerBlock("wither_end_bush") {
        object : FlowerBlock(MobEffects.WITHER, 5.0f, BlockBehaviour.Properties.of().instabreak().sound(SoundType.GRASS)) {
            override fun mayPlaceOn(state: BlockState, level: BlockGetter, pos: BlockPos): Boolean = state.`is`(Blocks.END_STONE)
            override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = SAPLING_SHAPE
            override fun getCollisionShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = Shapes.empty()
            override fun canBeReplaced(state: BlockState, context: net.minecraft.world.item.context.BlockPlaceContext): Boolean = true

            override fun canSurvive(state: BlockState, level: net.minecraft.world.level.LevelReader, pos: BlockPos): Boolean {
                return this.mayPlaceOn(level.getBlockState(pos.below()), level, pos.below())
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

    // 8. Саженец дерева Края (Переписан на SaplingBlock — убирает траву-призрак из коробки)
    val END_SAPLING: Supplier<Block> = registerBlock("end_sapling") {
        object : SaplingBlock(null, BlockBehaviour.Properties.of().randomTicks().instabreak().sound(SoundType.GRASS)), BonemealableBlock {
            override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = SAPLING_SHAPE
            override fun getCollisionShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = Shapes.empty()
            override fun canBeReplaced(state: BlockState, context: net.minecraft.world.item.context.BlockPlaceContext): Boolean = true

            // Разрешаем саженцу расти строго на твоем дёрне Ксена
            override fun mayPlaceOn(state: BlockState, level: BlockGetter, pos: BlockPos): Boolean {
                return state.`is`(END_GRASS_BLOCK.get())
            }

            override fun randomTick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
                if (level.getBlockState(pos.below()).`is`(END_GRASS_BLOCK.get())) {
                    if (random.nextInt(7) == 0) {
                        // Передаем pos дважды, соответствуя нашей новой сигнатуре оптимизации чанков (level, pos, originPos, random)
                        ru.rlyqaz.divinesouls.worldgen.EndTree.growTree(level, pos, random)
                    }
                }
            }

            override fun isValidBonemealTarget(level: net.minecraft.world.level.LevelReader, pos: BlockPos, state: BlockState): Boolean {
                return level.getBlockState(pos.below()).`is`(END_GRASS_BLOCK.get())
            }

            override fun isBonemealSuccess(level: net.minecraft.world.level.Level, random: RandomSource, pos: BlockPos, state: BlockState): Boolean {
                return random.nextFloat() < 0.45f
            }

            override fun performBonemeal(level: ServerLevel, random: RandomSource, pos: BlockPos, state: BlockState) {
                // Передаем pos дважды под костную муку (убирает ClassCastException и вылеты сервера)
                ru.rlyqaz.divinesouls.worldgen.EndTree.growTree(level, pos, random)

            }
        }
    }

    // 9. Ритуальный блок Печати Иссушения (заменяет центральный череп при призыве Иссушителя)
    val WITHER_SEAL_BLOCK: Supplier<Block> = registerBlock("wither_seal_block") {
        Block(
            BlockBehaviour.Properties.of()
                .strength(5.0f, 12.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE)
                .lightLevel { 7 } // Лёгкое зловещее свечение в темноте
        )
    }

    // 10. Скалковый дёрн (добывается мотыгой, при разрушении даёт опыт)
    val SCULK_GRASS_BLOCK: Supplier<Block> = registerBlock("sculk_grass_block") {
        ru.rlyqaz.divinesouls.block.custom.SculkGrassBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)
                .randomTicks()
                .mapColor(net.minecraft.world.level.material.MapColor.COLOR_BLACK)
                .sound(SoundType.SCULK)
        )
    }

    // 11. Скалковое бревно
    val SCULK_LOG: Supplier<Block> = registerBlock("sculk_log") {
        ru.rlyqaz.divinesouls.block.custom.SculkLogBlock(
            BlockBehaviour.Properties.of()
                .mapColor(net.minecraft.world.level.material.MapColor.COLOR_BLACK)
                .instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASS)
                .strength(2.0f)
                .sound(SoundType.WOOD)
                .ignitedByLava()
        )
    }

    // 12. Скалковые доски
    val SCULK_PLANKS: Supplier<Block> = registerBlock("sculk_planks") {
        Block(
            BlockBehaviour.Properties.of()
                .mapColor(net.minecraft.world.level.material.MapColor.COLOR_CYAN)
                .instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASS)
                .strength(2.0f, 3.0f)
                .sound(SoundType.WOOD)
                .ignitedByLava()
        )
    }

    // 13. Скалковая листва
    val SCULK_LEAVES: Supplier<Block> = registerBlock("sculk_leaves") {
        ru.rlyqaz.divinesouls.block.custom.SculkLeavesBlock(
            BlockBehaviour.Properties.of()
                .mapColor(net.minecraft.world.level.material.MapColor.COLOR_CYAN)
                .strength(0.2f)
                .randomTicks()
                .sound(SoundType.SCULK_VEIN)
                .noOcclusion()
                .isValidSpawn { _, _, _, _ -> false }
                .isSuffocating { _, _, _ -> false }
                .isViewBlocking { _, _, _ -> false }
                .ignitedByLava()
                .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
        )
    }

    // 14. Скалковая трава
    val SCULK_GRASS: Supplier<Block> = registerBlock("sculk_grass") {
        ru.rlyqaz.divinesouls.block.custom.SculkTallGrassBlock(
            BlockBehaviour.Properties.of()
                .mapColor(net.minecraft.world.level.material.MapColor.COLOR_CYAN)
                .replaceable()
                .noCollission()
                .instabreak()
                .sound(SoundType.SCULK_VEIN)
                .offsetType(BlockBehaviour.OffsetType.XYZ)
                .ignitedByLava()
                .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
        )
    }

    // 15. Скалковый цветок (мягко светится в темноте)
    val SCULK_FLOWER: Supplier<Block> = registerBlock("sculk_flower") {
        ru.rlyqaz.divinesouls.block.custom.SculkFlowerBlock(
            BlockBehaviour.Properties.of()
                .mapColor(net.minecraft.world.level.material.MapColor.COLOR_LIGHT_BLUE)
                .noCollission()
                .instabreak()
                .sound(SoundType.SCULK_VEIN)
                .lightLevel { 7 }
                .offsetType(BlockBehaviour.OffsetType.XZ)
                .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
        )
    }

    // 16. Красный кристалл жизни (генерируется на поверхности дальних островов Энда)
    val LIFE_CRYSTAL_CLUSTER: Supplier<Block> = registerBlock("life_crystal_cluster") {
        object : Block(
            BlockBehaviour.Properties.of()
                .mapColor(net.minecraft.world.level.material.MapColor.COLOR_RED)
                .strength(1.5f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.AMETHYST_CLUSTER)
                .lightLevel { 9 }
                .noOcclusion()
                .noCollission()
        ) {
            private val SHAPE: VoxelShape = box(3.0, 0.0, 3.0, 13.0, 13.0, 13.0)

            override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = SHAPE

            override fun canSurvive(state: BlockState, level: net.minecraft.world.level.LevelReader, pos: BlockPos): Boolean {
                val below = pos.below()
                return level.getBlockState(below).isFaceSturdy(level, below, net.minecraft.core.Direction.UP)
            }

            override fun updateShape(
                state: BlockState,
                direction: net.minecraft.core.Direction,
                neighborState: BlockState,
                level: net.minecraft.world.level.LevelAccessor,
                pos: BlockPos,
                neighborPos: BlockPos
            ): BlockState {
                if (direction == net.minecraft.core.Direction.DOWN && !state.canSurvive(level, pos)) {
                    return Blocks.AIR.defaultBlockState()
                }
                return super.updateShape(state, direction, neighborState, level, pos, neighborPos)
            }
        }
    }

    // 17. Мембрана портала Бездны (активируется Сердцем Бездны при установленной Голове Дракона на пьедестале)
    val ABYSS_PORTAL: Supplier<Block> = REGISTRY.register("abyss_portal", Supplier {
        ru.rlyqaz.divinesouls.block.custom.AbyssPortalBlock(
            BlockBehaviour.Properties.of()
                .mapColor(net.minecraft.world.level.material.MapColor.COLOR_CYAN)
                .noCollission()
                .strength(-1.0f, 3600000.0f)
                .sound(SoundType.GLASS)
                .lightLevel { 12 }
                .noLootTable()
        )
    })

    // 18. Пьедестал Бездны (генерируется перед порталом Древнего города под Голову Эндер-дракона)
    val ABYSS_PEDESTAL: Supplier<Block> = registerBlock("abyss_pedestal") {
        Block(
            BlockBehaviour.Properties.of()
                .mapColor(net.minecraft.world.level.material.MapColor.DEEPSLATE)
                .strength(15.0f, 1200.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.DEEPSLATE_BRICKS)
                .lightLevel { 5 }
        )
    }

    // ====================================================================
    // БЛОКИ ИЗМЕРЕНИЯ THE ABYSS (БЕЗДНА)
    // ====================================================================

    // 19. Камень Бездны (основа потолка, парящих террас и сталактитов; дропает Булыжник Бездны без Шёлкового касания)
    val ABYSSAL_STONE: Supplier<Block> = registerBlock("abyssal_stone") {
        Block(
            BlockBehaviour.Properties.of()
                .mapColor(net.minecraft.world.level.material.MapColor.COLOR_BLUE)
                .strength(3.5f, 9.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.DEEPSLATE)
        )
    }

    // 20. Булыжник Бездны (дроп с Камня Бездны)
    val ABYSSAL_COBBLESTONE: Supplier<Block> = registerBlock("abyssal_cobblestone") {
        Block(
            BlockBehaviour.Properties.of()
                .mapColor(net.minecraft.world.level.material.MapColor.COLOR_BLUE)
                .strength(4.0f, 9.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.DEEPSLATE)
        )
    }

    // Породное вкрапление Бездны (аналог туфа/андезита внутри массива Камня Бездны)
    val ABYSSAL_TUFF: Supplier<Block> = registerBlock("abyssal_tuff") {
        Block(
            BlockBehaviour.Properties.of()
                .mapColor(net.minecraft.world.level.material.MapColor.COLOR_BLACK)
                .strength(3.0f, 8.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.TUFF)
        )
    }

    // 21. Мицелий Бездны (фиолетово-индиговый светящийся дёрн на редких грибных островках Бездны)
    val ABYSSAL_MYCELIUM: Supplier<Block> = registerBlock("abyssal_mycelium") {
        object : Block(
            BlockBehaviour.Properties.of()
                .mapColor(net.minecraft.world.level.material.MapColor.COLOR_PURPLE)
                .strength(2.5f, 6.0f)
                .requiresCorrectToolForDrops()
                .randomTicks()
                .lightLevel { 5 }
                .sound(SoundType.NYLIUM)
        ) {
            override fun randomTick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
                val abovePos = pos.above()
                if (level.getBlockState(abovePos).isSolidRender(level, abovePos)) {
                    level.setBlockAndUpdate(pos, ABYSSAL_STONE.get().defaultBlockState())
                }
            }
        }
    }

    // 22. Светящаяся лоза Бездны (свисает с потолка, сталактитов и нижних краёв островов; можно лазать!)
    val ABYSSAL_VINE: Supplier<Block> = registerBlock("abyssal_vine") {
        object : Block(
            BlockBehaviour.Properties.of()
                .mapColor(net.minecraft.world.level.material.MapColor.COLOR_PURPLE)
                .instabreak()
                .noCollission()
                .lightLevel { 11 }
                .sound(SoundType.WEEPING_VINES)
                .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
        ) {
            private val SHAPE: VoxelShape = box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0)

            override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = SHAPE

            override fun canSurvive(state: BlockState, level: net.minecraft.world.level.LevelReader, pos: BlockPos): Boolean {
                val above = pos.above()
                val aboveState = level.getBlockState(above)
                return aboveState.`is`(this) || aboveState.isFaceSturdy(level, above, net.minecraft.core.Direction.DOWN)
            }

            override fun updateShape(
                state: BlockState,
                direction: net.minecraft.core.Direction,
                neighborState: BlockState,
                level: net.minecraft.world.level.LevelAccessor,
                pos: BlockPos,
                neighborPos: BlockPos
            ): BlockState {
                if (direction == net.minecraft.core.Direction.UP && !state.canSurvive(level, pos)) {
                    return Blocks.AIR.defaultBlockState()
                }
                return super.updateShape(state, direction, neighborState, level, pos, neighborPos)
            }
        }
    }

    // 23. Ножка гигантского гриба Бездны
    val ABYSSAL_MUSHROOM_STEM: Supplier<Block> = registerBlock("abyssal_mushroom_stem") {
        RotatedPillarBlock(
            BlockBehaviour.Properties.of()
                .mapColor(net.minecraft.world.level.material.MapColor.TERRACOTTA_PURPLE)
                .strength(1.5f)
                .sound(SoundType.STEM)
        )
    }

    // 24. Светящаяся шляпка гигантского гриба Бездны
    val ABYSSAL_MUSHROOM_CAP: Supplier<Block> = registerBlock("abyssal_mushroom_cap") {
        Block(
            BlockBehaviour.Properties.of()
                .mapColor(net.minecraft.world.level.material.MapColor.COLOR_MAGENTA)
                .strength(1.2f)
                .lightLevel { 12 }
                .sound(SoundType.FUNGUS)
        )
    }

    // 25. Малый гриб Бездны (растёт на Мицелии Бездны; удобрение костной мукой вырастит гигантский гриб!)
    val ABYSSAL_MUSHROOM: Supplier<Block> = registerBlock("abyssal_mushroom") {
        object : Block(
            BlockBehaviour.Properties.of()
                .mapColor(net.minecraft.world.level.material.MapColor.COLOR_MAGENTA)
                .instabreak()
                .noCollission()
                .lightLevel { 8 }
                .sound(SoundType.FUNGUS)
                .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
        ), BonemealableBlock {
            private val SHAPE: VoxelShape = box(4.0, 0.0, 4.0, 12.0, 10.0, 12.0)

            override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = SHAPE

            override fun canSurvive(state: BlockState, level: net.minecraft.world.level.LevelReader, pos: BlockPos): Boolean {
                val below = level.getBlockState(pos.below())
                return below.`is`(ABYSSAL_MYCELIUM.get()) || below.`is`(Blocks.MYCELIUM)
            }

            override fun updateShape(
                state: BlockState,
                direction: net.minecraft.core.Direction,
                neighborState: BlockState,
                level: net.minecraft.world.level.LevelAccessor,
                pos: BlockPos,
                neighborPos: BlockPos
            ): BlockState {
                if (direction == net.minecraft.core.Direction.DOWN && !state.canSurvive(level, pos)) {
                    return Blocks.AIR.defaultBlockState()
                }
                return super.updateShape(state, direction, neighborState, level, pos, neighborPos)
            }

            override fun isValidBonemealTarget(level: net.minecraft.world.level.LevelReader, pos: BlockPos, state: BlockState): Boolean {
                return level.getBlockState(pos.below()).`is`(ABYSSAL_MYCELIUM.get())
            }

            override fun isBonemealSuccess(level: net.minecraft.world.level.Level, random: RandomSource, pos: BlockPos, state: BlockState): Boolean {
                return random.nextFloat() < 0.45f
            }

            override fun performBonemeal(level: ServerLevel, random: RandomSource, pos: BlockPos, state: BlockState) {
                ru.rlyqaz.divinesouls.worldgen.AbyssalMushroomFeature.growGiantMushroom(level, pos, random)
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
