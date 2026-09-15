package me.scarletleaf1000.sunworks.multiblocks;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/// A unique set of blocks. Should only create one instance per set of blocks
public class MBBuildingBlock implements Predicate<BlockState> {

    protected final Predicate<BlockState> predicate;

    @SafeVarargs
    public MBBuildingBlock(Supplier<? extends Block>... blockSuppliers) {
        this.predicate = state -> {
            Set<Block> resolved = Arrays.stream(blockSuppliers)
                    .map(Supplier::get)
                    .collect(Collectors.toSet());
            return resolved.contains(state.getBlock());
        };
    }

    public MBBuildingBlock(Predicate<BlockState> predicate) {
        this.predicate = predicate;
    }

    public MBBuildingBlock(TagKey<Block> tag) {
        this(state -> state.is(tag));
    }

    @Override
    public boolean test(BlockState state) {
        return predicate.test(state);
    }

    public static final MBBuildingBlock ANY = new MBBuildingBlock(state -> true); // dont think this should be used
    public static final MBBuildingBlock AIR = new MBBuildingBlock(BlockBehaviour.BlockStateBase::isAir);

//    public static final MBBuildingBlock DIRTS = new MBBuildingBlock(
//            () -> Blocks.DIRT,
//            () -> Blocks.DIRT_PATH,
//            () -> Blocks.COARSE_DIRT,
//            () -> Blocks.ROOTED_DIRT
//    ); EXAMPLE
}
