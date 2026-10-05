package me.scarletleaf1000.sunworks.beams;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class PersistentBeam extends AbstractBeam {

    private int ticksExisted = 0;

    // e.g., 40 ticks = 2 seconds
    private static final int periodInTicks = 40;
    // Amplitude of 0.05f oscillates between 0.95f and 1.05f
    private static final float PULSE_AMPLITUDE = 0.125f;

    public PersistentBeam(UUID id, ResourceKey<Level> dimension, Vec3 start, Vec3 end, int red, int green, int blue, float alpha, float radius) {
        super(id, dimension, start, end, red, green, blue, alpha, radius);
    }

    public PersistentBeam(Level level, Vec3 start, Vec3 end, int red, int green, int blue, float alpha, float radius) {
        super(level, start, end, red, green, blue, alpha, radius);
    }

    @Override
    public boolean tick() {
        this.ticksExisted++;
        return false;
    }

    @Override
    public float getCurrentAlpha(float partialTick) {
        // Combine discrete ticks with partialTick for exact frame-level time (e.g., 42.65 ticks)
        float continuousTime = this.ticksExisted + partialTick;

        float speed = (float) ((Math.PI * 2.0) / periodInTicks);
        // Calculate smooth pulse: 1.0 + 0.05 * sin(time)
        float pulse = 1.0f + PULSE_AMPLITUDE * (float) Math.sin(continuousTime * speed);

        return Math.clamp(baseAlpha * pulse, 0.0f, 1.0f);
    }

    @Override
    public BeamType<?> getType() {
        return ModBeamTypes.PERSISTENT.get();
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, PersistentBeam> STREAM_CODEC = StreamCodec.of(
            AbstractBeam::encodeBase,
            buf -> AbstractBeam.decodeBase(buf, PersistentBeam::new)
    );
}
