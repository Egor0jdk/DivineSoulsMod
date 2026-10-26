package ru.rlyqaz.divinesouls;

import net.minecraft.block.Blocks;
import net.minecraft.block.PortalSize;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.Optional;

public class AncientRuneItem extends Item {

    public AncientRuneItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (!world.isClientSide()) {
            if (world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN) {
                Direction face = context.getClickedFace();
                BlockPos airPos = pos.relative(face);

                if (world.isEmptyBlock(airPos)) {
                    Optional<PortalSize> size = PortalSize.findEmptyPortalShape(world, airPos, Direction.Axis.X);

                    if (!size.isPresent()) {
                        size = PortalSize.findEmptyPortalShape(world, airPos, Direction.Axis.Z);
                    }

                    // НАСТОЯЩЕЕ НАЗВАНИЕ В MOJANG MAPPINGS: createPortalBlocks
                    if (size.isPresent()) {
                        size.get().createPortalBlocks();

                        if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
                            context.getItemInHand().shrink(1);
                        }
                        return ActionResultType.SUCCESS;
                    }
                }
            }
        }
        return ActionResultType.PASS;
    }
}
