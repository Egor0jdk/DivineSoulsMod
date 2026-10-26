package ru.rlyqaz.divinesouls;

import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.placement.TopSolidRangeConfig;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;
import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = DivineSoulsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModWorldGen {

    private static ConfiguredFeature<?, ?> endCrystalOreFeature;
    private static ConfiguredFeature<?, ?> witheredBushFeature;
    private static ConfiguredFeature<?, ?> endGrassOasisFeature;

    @SubscribeEvent
    public static void onBiomeLoading(final BiomeLoadingEvent event) {
        ResourceLocation biomeName = event.getName();

        if (biomeName != null) {
            String biomeStr = biomeName.toString();

            if (biomeStr.equals("minecraft:the_end")) {
                if (witheredBushFeature == null) {
                    witheredBushFeature = Feature.RANDOM_PATCH
                            .configured(new BlockClusterFeatureConfig.Builder(
                                    new net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider(ModBlocks.WITHERED_END_BUSH.get().defaultBlockState()),
                                    new net.minecraft.world.gen.blockplacer.SimpleBlockPlacer()
                            ).tries(16).build())
                            .decorated(Features.Placements.HEIGHTMAP_SQUARE)
                            .count(3);
                }
                event.getGeneration().addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, witheredBushFeature);
                return;
            }

            if (biomeName.getNamespace().equals("minecraft") && biomeName.getPath().contains("end")) {

                if (endCrystalOreFeature == null) {
                    endCrystalOreFeature = Feature.ORE
                            .configured(new OreFeatureConfig(
                                    new net.minecraft.world.gen.feature.template.BlockMatchRuleTest(Blocks.END_STONE),
                                    ModBlocks.END_CRYSTAL_ORE.get().defaultBlockState(),
                                    2))
                            .decorated(Placement.RANGE.configured(new TopSolidRangeConfig(10, 10, 85)))
                            .count(16);
                }
                event.getGeneration().addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, endCrystalOreFeature);

                if (endGrassOasisFeature == null) {
                    Feature<NoFeatureConfig> customOasisFeature = new Feature<NoFeatureConfig>(NoFeatureConfig.CODEC) {
                        @Override
                        public boolean place(@Nonnull ISeedReader world, @Nonnull ChunkGenerator generator, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull NoFeatureConfig config) {
                            int x = pos.getX() + rand.nextInt(16);
                            int z = pos.getZ() + rand.nextInt(16);
                            BlockPos surfacePos = world.getHeightmapPos(Heightmap.Type.WORLD_SURFACE_WG, new BlockPos(x, 0, z));

                            BlockPos groundPos = surfacePos.below();
                            if (world.getBlockState(groundPos).getBlock() != Blocks.END_STONE) {
                                return false;
                            }

                            int radius = 12 + rand.nextInt(5);

                            for (int dx = -radius; dx <= radius; dx++) {
                                for (int dz = -radius; dz <= radius; dz++) {
                                    if (dx * dx + dz * dz <= radius * radius) {
                                        BlockPos currentGround = groundPos.offset(dx, 0, dz);
                                        if (world.getBlockState(currentGround).getBlock() == Blocks.END_STONE) {
                                            world.setBlock(currentGround, ModBlocks.XEN_GRASS_BLOCK.get().defaultBlockState(), 2);

                                            BlockPos airPos = currentGround.above();
                                            if (world.isEmptyBlock(airPos) && rand.nextFloat() < 0.35F) {
                                                world.setBlock(airPos, ModBlocks.XEN_BUSH_GRASS.get().defaultBlockState(), 2);
                                            }
                                        }
                                    }
                                }
                            }

                            ru.rlyqaz.divinesouls.EndTree.generateTreeSafely(world, surfacePos, rand);

                            return true;
                        }
                    };

                    endGrassOasisFeature = customOasisFeature
                            .configured(NoFeatureConfig.NONE)
                            .decorated(Features.Placements.HEIGHTMAP_SQUARE)
                            .count(1);
                }

                event.getGeneration().addFeature(
                        GenerationStage.Decoration.VEGETAL_DECORATION,
                        endGrassOasisFeature
                );
            }
        }
    }
}
