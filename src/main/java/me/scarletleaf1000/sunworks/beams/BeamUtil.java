package me.scarletleaf1000.sunworks.beams;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class BeamUtil {

    public static PersistentBeam createPersistentOrangeBeam(ServerLevel level, Vec3 start, Vec3 end) {
        return createPersistentOrangeBeam(level, start, end, 0.8f, 0.08f);
    }

    public static PersistentBeam createPersistentOrangeBeam(ServerLevel level, Vec3 start, Vec3 end, float alpha, float radius) {
        return createPersistentBeam(level, start, end, 255, 116, 33, alpha, radius);
    }

    public static PersistentBeam createPersistentBeam(ServerLevel level, Vec3 start, Vec3 end, int red, int green, int blue, float alpha, float radius) {
        PersistentBeam beam = new PersistentBeam(level, start, end, red, green, blue, alpha, radius);
        initiateBeam(level, beam);
        return beam;
    }

    public static void initiateBeam(ServerLevel level, AbstractBeam beam) {
        ServerBeamManager.addBeam(level, beam);
    }
}
