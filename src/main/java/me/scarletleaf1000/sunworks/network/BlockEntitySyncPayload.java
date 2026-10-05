package me.scarletleaf1000.sunworks.network;

import me.scarletleaf1000.sunworks.Sunworks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BlockEntitySyncPayload(BlockPos pos, CompoundTag tag) implements CustomPacketPayload {

    public static final Type<BlockEntitySyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Sunworks.MOD_ID, "be_sync"));

    public static final StreamCodec<FriendlyByteBuf, BlockEntitySyncPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, BlockEntitySyncPayload::pos,
            ByteBufCodecs.COMPOUND_TAG, BlockEntitySyncPayload::tag,
            BlockEntitySyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
