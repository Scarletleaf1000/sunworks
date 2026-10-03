package me.scarletleaf1000.sunworks.beams;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class TemporaryBeam extends AbstractBeam {

    private final float targetAlpha;
    private final int maxAgeTicks;
    private int ageTicks = 0;

    public TemporaryBeam(UUID id, ResourceKey<Level> dimension, Vec3 start, Vec3 end, int red, int green, int blue, float alpha, float radius, int maxAgeTicks, float targetAlpha) {
        super(id, dimension, start, end, red, green, blue, alpha, radius);
        this.maxAgeTicks = maxAgeTicks;
        this.targetAlpha = targetAlpha;
    }

    public TemporaryBeam(Level level, Vec3 start, Vec3 end, int red, int green, int blue, float alpha, float radius, int maxAgeTicks, float targetAlpha) {
        this(UUID.randomUUID(), level.dimension(), start, end, red, green, blue, alpha, radius, maxAgeTicks, targetAlpha);
    }

    @Override
    public boolean tick() {
        this.ageTicks++;
        return this.ageTicks > this.maxAgeTicks;
    }

    @Override
    public float getCurrentAlpha(float partialTick) {
        float continuousAge = this.ageTicks + partialTick;
        float progress = Math.clamp(continuousAge / (float) (this.maxAgeTicks - 1), 0.0f, 1.0f);

        return Mth.lerp(progress, baseAlpha, targetAlpha);
    }

    @Override
    public BeamType<TemporaryBeam> getType() {
        return ModBeamTypes.TEMPORARY.get();
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, TemporaryBeam> STREAM_CODEC = StreamCodec.of(
            TemporaryBeam::write, TemporaryBeam::decode
    );

    private static void write(RegistryFriendlyByteBuf buf, TemporaryBeam beam) {
        AbstractBeam.encodeBase(buf, beam);
        ByteBufCodecs.VAR_INT.encode(buf, beam.maxAgeTicks);
        ByteBufCodecs.FLOAT.encode(buf, beam.targetAlpha);
    }

    private static TemporaryBeam decode(RegistryFriendlyByteBuf buf) {
        return AbstractBeam.decodeBase(buf, (id, dim, start, end, r, g, b, alpha, radius) ->
                new TemporaryBeam(id, dim, start, end, r, g, b, alpha, radius,
                        ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.FLOAT.decode(buf))
        );
    }

}
