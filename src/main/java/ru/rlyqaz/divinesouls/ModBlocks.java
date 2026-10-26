package ru.rlyqaz.divinesouls;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BushBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import javax.annotation.Nonnull;

public class ModBlocks {

    public static final RegistryObject<Block> END_CRYSTAL_ORE = DivineSoulsMod.BLOCKS.register("end_crystal_ore",
            () -> new Block(AbstractBlock.Properties.of(Material.STONE)
                    .strength(5.0F, 6.0F)
                    .sound(SoundType.STONE)
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(4)));

    public static final RegistryObject<Block> XEN_GRASS_BLOCK = DivineSoulsMod.BLOCKS.register("xen_grass_block",
            () -> new Block(AbstractBlock.Properties.of(Material.GRASS)
                    .strength(0.6F)
                    .sound(SoundType.GRASS)));

    public static final RegistryObject<BushBlock> XEN_BUSH_GRASS = DivineSoulsMod.BLOCKS.register("xen_bush_grass",
            () -> new BushBlock(AbstractBlock.Properties.of(Material.PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)) {

                @Override
                protected boolean mayPlaceOn(@Nonnull BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos) {
                    return state.getBlock() == ModBlocks.XEN_GRASS_BLOCK.get();
                }
            });
    // Твой иссушенный куст Края (строка 41 на скриншоте)
    public static final RegistryObject<BushBlock> WITHERED_END_BUSH = DivineSoulsMod.BLOCKS.register("wither_end_bush",
            () -> new BushBlock(AbstractBlock.Properties.of(Material.PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)) {
                @Override
                protected boolean mayPlaceOn(@Nonnull BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos) {
                    return state.getBlock() == net.minecraft.block.Blocks.END_STONE;
                }
            });

    // ================= СЮДА ВСТАВЛЯЕМ НАШЕ ДЕРЕВО КРАЯ =================

    // ИСПРАВЛЕНО: Переключено на RotatedPillarBlock, чтобы блок официально поддерживал оси поворота x, y, z!
    public static final RegistryObject<Block> END_WOOD_LOG = DivineSoulsMod.BLOCKS.register("end_wood_log",
            () -> new net.minecraft.block.RotatedPillarBlock(AbstractBlock.Properties.of(Material.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)));

    // 5. Доски дерева Края
    public static final RegistryObject<Block> END_WOOD_PLANKS = DivineSoulsMod.BLOCKS.register("end_wood_planks",
            () -> new Block(AbstractBlock.Properties.of(Material.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)));

    // ИСПРАВЛЕНО: Переключено на LeavesBlock! Теперь листва официально умеет опадать без брёвен Края.
    public static final RegistryObject<Block> END_WOOD_LEAVES = DivineSoulsMod.BLOCKS.register("end_wood_leaves",
            () -> new net.minecraft.block.LeavesBlock(AbstractBlock.Properties.of(Material.LEAVES)
                    .strength(0.2F)
                    .randomTicks() // ВАЖНО: заставляет игру проверять расстояние до брёвен Края
                    .sound(SoundType.GRASS)
                    .noOcclusion()));

    // 7. Саженец дерева Края (Передаем ему наш кастомный класс выращивания)
    public static final RegistryObject<net.minecraft.block.SaplingBlock> END_SAPLING = DivineSoulsMod.BLOCKS.register("end_sapling",
            () -> new net.minecraft.block.SaplingBlock(new ru.rlyqaz.divinesouls.EndTree(), AbstractBlock.Properties.of(Material.PLANT)
                    .noCollission()
                    .randomTicks() // Чтобы он рос сам со временем
                    .instabreak()
                    .sound(SoundType.GRASS)) {

                @Override
                protected boolean mayPlaceOn(BlockState state, net.minecraft.world.IBlockReader worldIn, BlockPos pos) {
                    // Разрешаем саженцу расти строго на нашем Дёрне Края
                    return state.getBlock() == ModBlocks.XEN_GRASS_BLOCK.get();
                }
            });

    public static void register() {
        // Оставляем пустым
    }
} // Самая последняя закрывающая скобка класса ModBlocks
