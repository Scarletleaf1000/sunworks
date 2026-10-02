package me.scarletleaf1000.sunworks.block.entity.custom.logistics;

import me.scarletleaf1000.sunworks.block.ModBlocks;
import me.scarletleaf1000.sunworks.block.custom.logistics.SunwellBlock;
import me.scarletleaf1000.sunworks.block.entity.ModBlockEntities;
import me.scarletleaf1000.sunworks.block.entity.energy.ModEnergyStorage;
import me.scarletleaf1000.sunworks.block.entity.energy.ModEnergyUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SunwellBlockEntity extends BlockEntity {
    private final int MAX_TRANSFER = 262144;
    private final int MAX_STORAGE = 1048576;

    public SunwellBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.SUNWELL_BE.get(), pos, blockState);
    }


    private final ModEnergyStorage ENERGY_STORAGE = createEnergyStorage();

    private ModEnergyStorage createEnergyStorage() {
        return new ModEnergyStorage(MAX_STORAGE, MAX_TRANSFER) {
            @Override
            public void onEnergyChanged() {
                setChanged();
                getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }

            @Override
            public boolean canReceive() {
                return getBlockState().getValue(SunwellBlock.POWERED) && super.canReceive();
            }

            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                if (!getBlockState().getValue(SunwellBlock.POWERED)) return 0;
                return super.receiveEnergy(maxReceive, simulate);
            }
        };
    }

    public IEnergyStorage getEnergyStorage(@Nullable Direction direction) {
        return ENERGY_STORAGE;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        List<BlockPos> neighbors = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            if (ModEnergyUtil.doesBlockHaveEnergyStorage(neighborPos, null, serverLevel))
                neighbors.add(neighborPos);
        }
        if (state.getValue(SunwellBlock.POWERED)) {
            for (BlockPos neighbor : neighbors) {
                ModEnergyUtil.move(neighbor, pos, (MAX_TRANSFER / neighbors.size()), serverLevel);
            }
        } else {
            for (BlockPos neighbor : neighbors) {
                ModEnergyUtil.move(pos, neighbor, (MAX_TRANSFER / neighbors.size()), serverLevel);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("helioreceiver.energy", ENERGY_STORAGE.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ENERGY_STORAGE.setEnergy(tag.getInt("helioreceiver.energy"));
    }
}
