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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnergyPortBlockEntity extends CapabilityPortBlockEntity<IEnergyStorage> {

    public EnergyPortBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ENERGY_PORT_BE.get(), pos, blockState);
    }

    public IEnergyStorage getEnergyStorage(@Nullable Direction direction) {
        if (direction==null || getFacing() == direction) return mbCapability;
        return null;
    }

    @Override
    public void tick() {
        if (level == null || level.isClientSide()) return;

        if (getIOState() == CapabilityPortBlock.IOState.PULL)
            ModEnergyUtil.move(worldPosition, worldPosition.relative(getFacing()), getIORate(), level);
        else
            ModEnergyUtil.move(worldPosition.relative(getFacing()), worldPosition, getIORate(), level);
    }

    @Override
    public void addOwnersCapability(IEnergyStorage ownerCapability) {
        this.mbCapability = new PortEnergyStorage(ownerCapability);
        setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    protected int getIORate() {
        return ((EnergyPortBlock) getBlockState().getBlock()).ioRate;
    }

    public class PortEnergyStorage implements IEnergyStorage {
        private final @NotNull IEnergyStorage energyStorage;

        public PortEnergyStorage(@NotNull IEnergyStorage mbEnergyStorage) {
            energyStorage = mbEnergyStorage;
        }

        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            return energyStorage.receiveEnergy(toReceive, simulate);
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            return energyStorage.extractEnergy(toExtract, simulate);
        }

        @Override
        public int getEnergyStored() {
            return energyStorage.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return energyStorage.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            return getIOState() == CapabilityPortBlock.IOState.PUSH;
        }

        @Override
        public boolean canReceive() {
            return getIOState() == CapabilityPortBlock.IOState.PULL;
        }
    }
}
