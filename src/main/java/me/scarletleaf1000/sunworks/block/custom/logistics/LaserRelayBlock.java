package me.scarletleaf1000.sunworks.block.custom.logistics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.scarletleaf1000.sunworks.block.entity.ModBlockEntities;
import me.scarletleaf1000.sunworks.block.entity.custom.logistics.LaserRelayBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class LaserRelayBlock extends BaseEntityBlock {
    public static final MapCodec<LaserRelayBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            propertiesCodec(),
            Codec.INT.fieldOf("transferAmount").forGetter(block -> block.transferAmount),
            Codec.INT.fieldOf("maxDistance").forGetter(block -> block.maxDistance)
    ).apply(instance, LaserRelayBlock::new));
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Map.of(
            Direction.UP,    Block.box(0, 0, 0, 16, 3, 16),     // plate on floor
            Direction.DOWN,  Block.box(0, 13, 0, 16, 16, 16),   // plate on ceiling
            Direction.NORTH, Block.box(0, 0, 13, 16, 16, 16),   // plate on south wall
            Direction.SOUTH, Block.box(0, 0, 0, 16, 16, 3),
            Direction.WEST,  Block.box(13, 0, 0, 16, 16, 16),
            Direction.EAST,  Block.box(0, 0, 0, 3, 16, 16)
    ));

    private final int transferAmount;
    private final int maxDistance;

    public LaserRelayBlock(Properties properties, int transferAmount, int maxDistance) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN));
        this.transferAmount = transferAmount;
        this.maxDistance = maxDistance;
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction dir = ctx.getClickedFace();
        return defaultBlockState().setValue(FACING, dir);
    }

    @Override
    protected RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new LaserRelayBlockEntity(blockPos, blockState);
    }

    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) return null;

        return createTickerHelper(blockEntityType, ModBlockEntities.LASER_RELAY_BE.get(),
                (level1, pos, state1, blockEntity) -> blockEntity.tick(level1, pos, state1));
    }

    public int getTransferAmount() {
        return transferAmount;
    }

    public int getMaxDistance() {
        return maxDistance;
    }
}
