package me.scarletleaf1000.sunworks.beams;

import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.network.ClientboundAddBeamPayload;
import me.scarletleaf1000.sunworks.network.ClientboundRemoveBeamPayload;
import me.scarletleaf1000.sunworks.network.ClientboundRemoveLevelBeamsPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

@EventBusSubscriber(modid = Sunworks.MOD_ID)
public class ServerBeamManager {

    private static final Map<ResourceKey<Level>, List<AbstractBeam>> BEAMS = new HashMap<>();

    public static void addBeam(ServerLevel level, AbstractBeam beam) {
        BEAMS.computeIfAbsent(level.dimension(), dim -> new ArrayList<>()).add(beam);

        // Broadcast new beam to all players in this dimension
        PacketDistributor.sendToPlayersInDimension(level, new ClientboundAddBeamPayload(beam));
    }

    public static void removeBeam(ServerLevel level, UUID id) {
        if (id == null) return;

        List<AbstractBeam> active = BEAMS.get(level.dimension());
        if (active != null) {
            boolean removed = active.removeIf(beam -> beam.getId().equals(id));
            if (removed)
                // Send specific single-beam removal packet instead of wiping the entire level
                PacketDistributor.sendToPlayersInDimension(level, new ClientboundRemoveBeamPayload(id));

            if (active.isEmpty())
                BEAMS.remove(level.dimension());

        }
    }

    public static void clearAllBeams(ServerLevel level) {
        BEAMS.remove(level.dimension());
        PacketDistributor.sendToPlayersInDimension(level, new ClientboundRemoveLevelBeamsPayload(level.dimension()));
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        List<AbstractBeam> active = BEAMS.get(level.dimension());
        if (active == null || active.isEmpty()) return;

        // Iterate over a snapshot list to prevent ConcurrentModificationException when removeBeam is called
        for (AbstractBeam beam : new ArrayList<>(active)) {
            if (beam != null && beam.tick()) {
                removeBeam(level, beam.getId());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncBeamsToPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncBeamsToPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            BEAMS.remove(level.dimension());
        }
    }

    private static void syncBeamsToPlayer(ServerPlayer player) {
        ServerLevel level = player.serverLevel();

        // 1. Tell client to clear old cached beams for this dimension first to avoid ghosting/duplicates
        PacketDistributor.sendToPlayer(player, new ClientboundRemoveLevelBeamsPayload(level.dimension()));

        // 2. Send all currently active beams for this dimension
        List<AbstractBeam> active = BEAMS.get(level.dimension());
        if (active == null) return;

        for (AbstractBeam beam : active) {
            PacketDistributor.sendToPlayer(player, new ClientboundAddBeamPayload(beam));
        }
    }
}
