package me.scarletleaf1000.sunworks.network;

import me.scarletleaf1000.sunworks.block.entity.custom.ISyncedBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber()
public class CapabilityNetworking {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                BlockEntitySyncPayload.TYPE,
                BlockEntitySyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    // Executed on the client thread
                    var level = context.player().level();
                    if (level.isLoaded(payload.pos()) && level.getBlockEntity(payload.pos()) instanceof ISyncedBlockEntity synced) {
                        synced.readSyncData(payload.tag(), level.registryAccess());
                    }
                })
        );
    }
}
