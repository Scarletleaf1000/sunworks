package me.scarletleaf1000.sunworks.beams;

import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.network.ClientboundAddBeamPayload;
import me.scarletleaf1000.sunworks.network.ClientboundRemoveLevelBeamsPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = Sunworks.MOD_ID)
public class ServerBeamManager {

    private static final Map<ResourceKey<Level>, List<AbstractBeam>> BEAMS = new HashMap<>();

    public static void addBeam(ServerLevel level, AbstractBeam beam) {
        BEAMS.computeIfAbsent(level.dimension(), dim -> new ArrayList<>()).add(beam);

        // Broadcast new beam to all players in this dimension
        PacketDistributor.sendToPlayersInDimension(level, new ClientboundAddBeamPayload(beam));
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

        active.removeIf(AbstractBeam::tick);
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

    private static void syncBeamsToPlayer(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        List<AbstractBeam> active = BEAMS.get(level.dimension());
        if (active == null) return;

        for (AbstractBeam beam : active)
            PacketDistributor.sendToPlayer(player, new ClientboundAddBeamPayload(beam));

    }
}
