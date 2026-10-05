package me.scarletleaf1000.sunworks.network;

import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.beams.AbstractBeam;
import me.scarletleaf1000.sunworks.beams.BeamType;
import me.scarletleaf1000.sunworks.beams.ModBeamTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundAddBeamPayload(AbstractBeam beam) implements CustomPacketPayload {

    public static final Type<ClientboundAddBeamPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Sunworks.MOD_ID, "add_beam"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundAddBeamPayload> STREAM_CODEC = StreamCodec.of(
            ClientboundAddBeamPayload::write,
            ClientboundAddBeamPayload::decode
    );

    @SuppressWarnings("unchecked")
    private static void write(RegistryFriendlyByteBuf buf, ClientboundAddBeamPayload payload) {
        // Encode BeamType using NeoForge's built-in registry stream codec
        ByteBufCodecs.registry(ModBeamTypes.BEAM_TYPE_REGISTRY_KEY).encode(buf, payload.beam().getType());

        // Encode the actual beam payload data
        BeamType<AbstractBeam> type = (BeamType<AbstractBeam>) payload.beam().getType();
        type.codec().encode(buf, payload.beam());
    }

    private static ClientboundAddBeamPayload decode(RegistryFriendlyByteBuf buf) {
        // Look up BeamType automatically from the client-side synced registry
        BeamType<?> type = ByteBufCodecs.registry(ModBeamTypes.BEAM_TYPE_REGISTRY_KEY).decode(buf);
        AbstractBeam beam = type.codec().decode(buf);
        return new ClientboundAddBeamPayload(beam);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}