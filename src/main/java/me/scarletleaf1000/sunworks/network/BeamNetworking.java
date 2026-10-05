package me.scarletleaf1000.sunworks.network;

import me.scarletleaf1000.sunworks.client.renderer.beams.ClientBeamManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class BeamNetworking {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(ClientboundAddBeamPayload.TYPE, ClientboundAddBeamPayload.STREAM_CODEC, BeamNetworking::handleAddBeam);
        registrar.playToClient(ClientboundRemoveBeamPayload.TYPE, ClientboundRemoveBeamPayload.STREAM_CODEC, BeamNetworking::handleRemoveBeam);
        registrar.playToClient(ClientboundRemoveLevelBeamsPayload.TYPE, ClientboundRemoveLevelBeamsPayload.STREAM_CODEC, BeamNetworking::handleRemoveLevelBeams);
    }

    public static void handleAddBeam(ClientboundAddBeamPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientBeamManager.addBeam(payload.beam()));
    }
    public static void handleRemoveBeam(ClientboundRemoveBeamPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientBeamManager.removeBeam(payload.id()));
    }
    public static void handleRemoveLevelBeams(ClientboundRemoveLevelBeamsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientBeamManager.clearBeams(payload.dimension()));
    }

}
