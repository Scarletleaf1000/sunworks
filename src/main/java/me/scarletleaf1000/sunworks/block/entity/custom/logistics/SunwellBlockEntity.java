package me.scarletleaf1000.sunworks.block.entity.custom.logistics;

import me.scarletleaf1000.sunworks.block.custom.logistics.SunwellBlock;
import me.scarletleaf1000.sunworks.block.entity.ModBlockEntities;
import me.scarletleaf1000.sunworks.block.entity.api.IOverlayInfoProvider;
import me.scarletleaf1000.sunworks.block.entity.energy.ModEnergyStorage;
import me.scarletleaf1000.sunworks.block.entity.energy.ModEnergyUtil;
import me.scarletleaf1000.sunworks.client.overlay.OverlayBar;
import me.scarletleaf1000.sunworks.client.overlay.OverlayContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SunwellBlockEntity extends BlockEntity implements IOverlayInfoProvider {
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
                if (getLevel() != null) {
                    getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                }
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

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        super.onDataPacket(net, pkt, registries);
    }

    @Override
    public boolean addOverlayInfo(OverlayContext ctx) {
        int stored = ENERGY_STORAGE.getEnergyStored();
        int max = ENERGY_STORAGE.getMaxEnergyStored();

        ctx.addDescription(Component.translatable("overlay.sunworks.redstone_powered", level.hasNeighborSignal(getBlockPos())).withStyle(ChatFormatting.GRAY));
        ctx.addBar(new OverlayBar(Component.literal("FE:"), stored, max, false));

        return true;
    }
}
