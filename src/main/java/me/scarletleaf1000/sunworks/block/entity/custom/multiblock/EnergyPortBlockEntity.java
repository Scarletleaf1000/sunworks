package me.scarletleaf1000.sunworks.block.entity.custom.multiblock;

import me.scarletleaf1000.sunworks.block.custom.multiblock.EnergyPortBlock;
import me.scarletleaf1000.sunworks.block.entity.ModBlockEntities;
import me.scarletleaf1000.sunworks.block.entity.energy.ModEnergyUtil;
import me.scarletleaf1000.sunworks.multiblocks.ports.CapabilityPortBlock;
import me.scarletleaf1000.sunworks.multiblocks.ports.CapabilityPortBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public class EnergyPortBlockEntity extends CapabilityPortBlockEntity<IEnergyStorage> {

    // Cached wrapper instance created once per BlockEntity life cycle
    private final IEnergyStorage portEnergyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return (mbCapability != null && canReceive()) ? mbCapability.receiveEnergy(maxReceive, simulate) : 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return (mbCapability != null && canExtract()) ? mbCapability.extractEnergy(maxExtract, simulate) : 0;
        }

        @Override
        public int getEnergyStored() {
            return mbCapability != null ? mbCapability.getEnergyStored() : 0;
        }

        @Override
        public int getMaxEnergyStored() {
            return mbCapability != null ? mbCapability.getMaxEnergyStored() : 0;
        }

        @Override
        public boolean canExtract() {
            return mbCapability != null && getIOState() == CapabilityPortBlock.IOState.PUSH;
        }

        @Override
        public boolean canReceive() {
            return mbCapability != null && getIOState() == CapabilityPortBlock.IOState.PULL;
        }
    };

    public EnergyPortBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ENERGY_PORT_BE.get(), pos, blockState);
    }

    public void addOwnersCapability(IEnergyStorage ownerCapability) {
        this.mbCapability = ownerCapability;
        setChanged();

        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
            this.level.invalidateCapabilities(this.worldPosition);
            this.level.updateNeighborsAt(this.worldPosition, getBlockState().getBlock());
        }
    }

    public IEnergyStorage getEnergyStorage(@Nullable Direction direction) {
        // Direct direction check & capability availability test with no allocations
        if ((direction == null || direction == getFacing())) {
            return portEnergyStorage;
        }
        return null;
    }

    @Override
    public void tick() {
        if (level == null || level.isClientSide() || mbCapability == null) return;

        if (getIOState() == CapabilityPortBlock.IOState.PUSH) {
            ModEnergyUtil.move(worldPosition, worldPosition.relative(getFacing()), getIORate(), level);
        } else {
            ModEnergyUtil.move(worldPosition.relative(getFacing()), worldPosition, getIORate(), level);
        }
    }

    protected int getIORate() {
        return ((EnergyPortBlock) getBlockState().getBlock()).ioRate;
    }
}
