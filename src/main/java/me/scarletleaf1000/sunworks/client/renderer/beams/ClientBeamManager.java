package me.scarletleaf1000.sunworks.client.renderer.beams;

import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.beams.AbstractBeam;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.*;

@EventBusSubscriber(modid = Sunworks.MOD_ID, value = Dist.CLIENT)
public class ClientBeamManager {

    // Map beams directly to their dimension key
    private static final Map<ResourceKey<Level>, List<AbstractBeam>> BEAMS_BY_DIMENSION = new HashMap<>();

    public static void addBeam(AbstractBeam beam) {
        BEAMS_BY_DIMENSION
                .computeIfAbsent(beam.getDimension(), dim -> new ArrayList<>())
                .add(beam);
    }

    public static void removeBeam(UUID id) {
        if (id == null) return;

        BEAMS_BY_DIMENSION.values().forEach(beams -> beams.removeIf(beam -> beam.getId().equals(id)));

        BEAMS_BY_DIMENSION.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public static void clearBeams(ResourceKey<Level> dimension) {
        if (dimension != null) {
            BEAMS_BY_DIMENSION.remove(dimension);
        }
    }

    public static void clearBeams() {
        BEAMS_BY_DIMENSION.clear();
    }

    /**
     * Retrieves ONLY the beams in the player's current dimension without stream filtering.
     */
    public static List<AbstractBeam> getBeamsForCurrentLevel() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return Collections.emptyList();

        return BEAMS_BY_DIMENSION.getOrDefault(mc.level.dimension(), Collections.emptyList());
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        List<AbstractBeam> activeBeams = BEAMS_BY_DIMENSION.get(mc.level.dimension());
        if (activeBeams == null || activeBeams.isEmpty()) return;

        // Only tick beams in the current level
        activeBeams.removeIf(AbstractBeam::tick);
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            clearBeams();
        }
    }
}