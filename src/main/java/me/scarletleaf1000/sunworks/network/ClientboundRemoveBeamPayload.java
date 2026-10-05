package me.scarletleaf1000.sunworks.network;

import me.scarletleaf1000.sunworks.Sunworks;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record ClientboundRemoveBeamPayload(UUID id) implements CustomPacketPayload {

    public static final Type<ClientboundRemoveBeamPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Sunworks.MOD_ID, "remove_beam"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRemoveBeamPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ClientboundRemoveBeamPayload::id,
            ClientboundRemoveBeamPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
