package me.scarletleaf1000.sunworks.block.entity.custom;

import me.scarletleaf1000.sunworks.network.BlockEntitySyncPayload;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;

/// Syncs block entity data to client
public interface ISyncedBlockEntity {

    default BlockEntity getSelf() {
        return (BlockEntity) this;
    }

    void writeSyncData(CompoundTag tag, HolderLookup.Provider registries);

    void readSyncData(CompoundTag tag, HolderLookup.Provider registries);

    /**
     * Sends a direct CustomPacketPayload to all clients tracking this block entity.
     * Bypasses block re-renders and full NBT block updates.
     */
    default void syncToTrackingClients() {
        BlockEntity be = getSelf();
        if (be.getLevel() instanceof ServerLevel serverLevel) {
            CompoundTag tag = new CompoundTag();
            writeSyncData(tag, serverLevel.registryAccess());

            BlockEntitySyncPayload payload = new BlockEntitySyncPayload(be.getBlockPos(), tag);
            PacketDistributor.sendToPlayersInDimension(serverLevel, payload);
        }
    }
}
