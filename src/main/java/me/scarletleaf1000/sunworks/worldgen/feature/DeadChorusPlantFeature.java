package me.scarletleaf1000.sunworks.worldgen.feature;

import com.mojang.serialization.Codec;
import me.scarletleaf1000.sunworks.worldgen.DeadChorusPlantGrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class DeadChorusPlantFeature extends Feature<NoneFeatureConfiguration> {
    public DeadChorusPlantFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        BlockPos basePos = findBasePos(level, origin);
        if (basePos == null) {
            return false;
        }

        DeadChorusPlantGrowth.generateDeadPlant(level, basePos, random, 3);
        return true;
    }

    private static BlockPos findBasePos(WorldGenLevel level, BlockPos origin) {
        BlockPos.MutableBlockPos mutable = origin.mutable();

        while (mutable.getY() > level.getMinBuildHeight()) {
            if (level.getBlockState(mutable.below()).is(Blocks.END_STONE)) {
                if (level.isEmptyBlock(mutable)) {
                    return mutable.immutable();
                }
                return null;
            }
            if (!level.isEmptyBlock(mutable)) {
                return null;
            }
            mutable.move(0, -1, 0);
        }

        return null;
    }
}
