package me.scarletleaf1000.sunworks.multiblocks.ports;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CapabilityPortBlock extends BaseEntityBlock {

    public static final EnumProperty<IOState> IO_STATE = EnumProperty.create("io_state", IOState.class);
    public static final DirectionProperty     FACING = BlockStateProperties.HORIZONTAL_FACING;

    public CapabilityPortBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition
                        .any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(IO_STATE, IOState.PULL)
        );
    }

    public abstract BlockEntityType<?> getBlockEntityType();

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, getBlockEntityType(), (level1, blockPos, blockState, blockEntity) ->
                ((CapabilityPortBlockEntity<?>) blockEntity).tick());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return getBlockEntityType().create(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        IOState state = IOState.PULL;
        if (context.getPlayer() != null)
            state = context.getPlayer().isShiftKeyDown() ? IOState.PUSH : IOState.PULL;

        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection())
                .setValue(IO_STATE, state);
    }

    @Override
    protected @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    public enum IOState implements StringRepresentable {
        PULL,
        PUSH;

        @Override
        public @NotNull String getSerializedName() {
            return toString().toLowerCase();
        }
    }
}
