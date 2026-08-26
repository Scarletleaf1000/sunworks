package me.scarletleaf1000.sunworks.worldgen;

import me.scarletleaf1000.sunworks.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class DeadChorusPlantGrowth {

    public static void generateDeadPlant(LevelAccessor level, BlockPos origin, RandomSource random, int maxHorizontalDistance) {
        if (!level.isEmptyBlock(origin) && !level.getBlockState(origin).is(Blocks.CHORUS_FLOWER)) {
            return;
        }

        BoundingBox box = new BoundingBox(
                origin.getX() - maxHorizontalDistance - 2,
                origin.getY() - 2,
                origin.getZ() - maxHorizontalDistance - 2,
                origin.getX() + maxHorizontalDistance + 2,
                origin.getY() + 24,
                origin.getZ() + maxHorizontalDistance + 2
        );

        Set<BlockPos> existingChorus = collectBlocks(level, box, state -> state.is(Blocks.CHORUS_PLANT) || state.is(Blocks.CHORUS_FLOWER));

        ChorusFlowerBlock.generatePlant(level, origin, random, maxHorizontalDistance);

        Set<BlockPos> newChorus = collectBlocks(level, box, state -> state.is(Blocks.CHORUS_PLANT) || state.is(Blocks.CHORUS_FLOWER));
        newChorus.removeAll(existingChorus);
        newChorus.add(origin.immutable());

        BlockState deadPlant = ModBlocks.DEAD_CHORUS_PLANT.get().defaultBlockState();

        for (BlockPos pos : newChorus) {
            level.setBlock(pos, deadPlant, 2);
        }

        for (BlockPos pos : newChorus) {
            BlockState state = computeConnectionState(level, pos, newChorus);
            level.setBlock(pos, state, Block.UPDATE_ALL);
        }
    }

    private static Set<BlockPos> collectBlocks(LevelAccessor level, BoundingBox box, Predicate<BlockState> predicate) {
        Set<BlockPos> result = new HashSet<>();
        BlockPos.betweenClosedStream(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())
                .forEach(pos -> {
                    if (predicate.test(level.getBlockState(pos))) {
                        result.add(pos.immutable());
                    }
                });
        return result;
    }

    private static BlockState computeConnectionState(LevelAccessor level, BlockPos pos, Set<BlockPos> plantPositions) {
        BlockState state = ModBlocks.DEAD_CHORUS_PLANT.get().defaultBlockState();

        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            boolean connected = isConnectedToChorusSupport(level, neighbor, plantPositions);

            Property<Boolean> property = PipeBlock.PROPERTY_BY_DIRECTION.get(dir);
            if (state.hasProperty(property)) {
                state = state.setValue(property, connected);
            }
        }

        return state;
    }

    private static boolean isConnectedToChorusSupport(LevelAccessor level, BlockPos pos, Set<BlockPos> plantPositions) {
        if (plantPositions.contains(pos)) {
            return true;
        }
        BlockState state = level.getBlockState(pos);
        return state.is(Blocks.END_STONE) || state.is(Blocks.CHORUS_PLANT) || state.is(Blocks.CHORUS_FLOWER);
    }
}
