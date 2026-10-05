package me.scarletleaf1000.sunworks.network;

import me.scarletleaf1000.sunworks.Sunworks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public record ClientboundRemoveLevelBeamsPayload(ResourceKey<Level> dimension) implements CustomPacketPayload {

    public static final Type<ClientboundRemoveLevelBeamsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Sunworks.MOD_ID, "remove_all_beams"));

    public static final StreamCodec<FriendlyByteBuf, ClientboundRemoveLevelBeamsPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION), ClientboundRemoveLevelBeamsPayload::dimension,
            ClientboundRemoveLevelBeamsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}