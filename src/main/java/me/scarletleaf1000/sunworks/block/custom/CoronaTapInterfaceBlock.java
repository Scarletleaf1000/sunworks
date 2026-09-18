package me.scarletleaf1000.sunworks.block.custom;

import com.mojang.serialization.MapCodec;
import me.scarletleaf1000.sunworks.block.entity.custom.CoronaTapInterfaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@Deprecated(forRemoval = true)
public class CoronaTapInterfaceBlock extends BaseEntityBlock {
    public static final MapCodec<CoronaTapInterfaceBlock> CODEC = simpleCodec(CoronaTapInterfaceBlock::new);

    public CoronaTapInterfaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CoronaTapInterfaceBlockEntity(null, pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
