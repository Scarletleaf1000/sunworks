package me.scarletleaf1000.sunworks.multiblocks.ports;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class CapabilityPortBlockEntity<C> extends BlockEntity {

    protected C mbCapability;

    public CapabilityPortBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public abstract void tick();

    public void invalidate() {
        mbCapability = null;
    }

    public abstract void addOwnersCapability(C ownerCapability);

    public Direction getFacing() {
        return getBlockState().getValue(CapabilityPortBlock.FACING);
    }
    public CapabilityPortBlock.IOState getIOState() {
        return getBlockState().getValue(CapabilityPortBlock.IO_STATE);
    }
}
