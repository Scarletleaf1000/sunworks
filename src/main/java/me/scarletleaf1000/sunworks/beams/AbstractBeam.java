package me.scarletleaf1000.sunworks.beams;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public abstract class AbstractBeam {

    public static final StreamCodec<ByteBuf, Vec3> VEC3_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, vec -> vec.x,
            ByteBufCodecs.DOUBLE, vec -> vec.y,
            ByteBufCodecs.DOUBLE, vec -> vec.z,
            Vec3::new
    );

    public static void encodeBase(RegistryFriendlyByteBuf buf, AbstractBeam beam) {
        UUIDUtil.STREAM_CODEC.encode(buf, beam.getId());
        ResourceKey.streamCodec(Registries.DIMENSION).encode(buf, beam.getDimension());
        VEC3_STREAM_CODEC.encode(buf, beam.start());
        VEC3_STREAM_CODEC.encode(buf, beam.end());
        ByteBufCodecs.VAR_INT.encode(buf, beam.red());
        ByteBufCodecs.VAR_INT.encode(buf, beam.green());
        ByteBufCodecs.VAR_INT.encode(buf, beam.blue());
        ByteBufCodecs.FLOAT.encode(buf, beam.baseAlpha);
        ByteBufCodecs.FLOAT.encode(buf, beam.radius());
    }

    @FunctionalInterface
    public interface BaseFactory<T extends AbstractBeam> {
        T create(UUID id, ResourceKey<Level> dimension, Vec3 start, Vec3 end, int red, int green, int blue, float alpha, float radius);
    }

    public static <T extends AbstractBeam> T decodeBase(RegistryFriendlyByteBuf buf, BaseFactory<T> factory) {
        return factory.create(
                UUIDUtil.STREAM_CODEC.decode(buf),
                ResourceKey.streamCodec(Registries.DIMENSION).decode(buf),
                VEC3_STREAM_CODEC.decode(buf),
                VEC3_STREAM_CODEC.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.FLOAT.decode(buf),
                ByteBufCodecs.FLOAT.decode(buf)
        );
    }

    private final UUID id;
    private final ResourceKey<Level> dimension;
    private final Vec3 start;
    private final Vec3 end;
    private final int red, green, blue;
    protected final float baseAlpha;
    private final float radius;

    public AbstractBeam(UUID id, ResourceKey<Level> dimension, Vec3 start, Vec3 end, int red, int green, int blue, float alpha, float radius) {
        this.id = id;
        this.dimension = dimension;
        this.start = start;
        this.end = end;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.baseAlpha = alpha;
        this.radius = radius;
    }

    public AbstractBeam(ResourceKey<Level> dimension, Vec3 start, Vec3 end, int red, int green, int blue, float alpha, float radius) {
        this(UUID.randomUUID(), dimension, start, end, red, green, blue, alpha, radius);
    }

    public AbstractBeam(Level level, Vec3 start, Vec3 end, int red, int green, int blue, float alpha, float radius) {
        this(UUID.randomUUID(), level.dimension(), start, end, red, green, blue, alpha, radius);
    }

//    public void render(PoseStack poseStack, VertexConsumer consumer, float partialTick) {
//        PoseStack.Pose pose = poseStack.last();
//
//        Vec3 delta = end.subtract(start);
//        double length = delta.length();
//        if (length < 1e-5) return; // Zero-length guard
//
//        Vec3 dir = delta.scale(1.0 / length);
//
//        // Orthonormal basis for beam thickness
//        Vec3 arbitrary = Math.abs(dir.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
//        Vec3 right = dir.cross(arbitrary).normalize().scale(radius);
//        Vec3 up = right.cross(dir).normalize().scale(radius);
//
//        Vec3[] corners = new Vec3[] {
//                right.add(up),                 // Top-Right
//                right.reverse().add(up),       // Top-Left
//                right.reverse().subtract(up),  // Bottom-Left
//                right.subtract(up)             // Bottom-Right
//        };
//
//        // --- 1. Draw 4 Outer Side Quads ---
//        for (int i = 0; i < 4; i++) {
//            int next = (i + 1) % 4;
//
//            Vec3 s1 = start.add(corners[i]);
//            Vec3 s2 = start.add(corners[next]);
//            Vec3 e2 = end.add(corners[next]);
//            Vec3 e1 = end.add(corners[i]);
//
//            addVertex(consumer, pose, s1, partialTick);
//            addVertex(consumer, pose, s2, partialTick);
//            addVertex(consumer, pose, e2, partialTick);
//            addVertex(consumer, pose, e1, partialTick);
//        }
//
//        // --- 2. Draw Start Cap ---
//        addVertex(consumer, pose, start.add(corners[0]), partialTick);
//        addVertex(consumer, pose, start.add(corners[3]), partialTick);
//        addVertex(consumer, pose, start.add(corners[2]), partialTick);
//        addVertex(consumer, pose, start.add(corners[1]), partialTick);
//
//        // --- 3. Draw End Cap ---
//        addVertex(consumer, pose, end.add(corners[0]), partialTick);
//        addVertex(consumer, pose, end.add(corners[1]), partialTick);
//        addVertex(consumer, pose, end.add(corners[2]), partialTick);
//        addVertex(consumer, pose, end.add(corners[3]), partialTick);
//    }
//
//    private void addVertex(VertexConsumer consumer, PoseStack.Pose pose, Vec3 pos, float partialTick) {
//        consumer.addVertex(pose, (float) pos.x, (float) pos.y, (float) pos.z)
//                .setColor(red, green, blue, (int) (255 * getCurrentAlpha(partialTick)))
//                .setUv2(LightTexture.FULL_BRIGHT & 0xFFFF, (LightTexture.FULL_BRIGHT >> 16) & 0xFFFF)
//                .setNormal(pose, 0.0f, 1.0f, 0.0f);
//    }

    public void render(PoseStack poseStack, VertexConsumer consumer, float partialTick) {
        PoseStack.Pose pose = poseStack.last();

        Vec3 delta = end.subtract(start);
        double length = delta.length();
        if (length < 1e-5) return; // Prevent NaN on zero-length beams

        Vec3 dir = delta.scale(1.0 / length);

        // Orthonormal basis perpendicular to beam direction
        Vec3 arbitrary = Math.abs(dir.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 right = dir.cross(arbitrary).normalize().scale(radius);
        Vec3 up = right.cross(dir).normalize().scale(radius);

        // Cardinal face offsets keep beam width equal to radius on all sides
        Vec3[] faceOffsets = new Vec3[] {
                right,           // 0: +Right
                up,              // 1: +Up
                right.reverse(), // 2: -Right
                up.reverse()     // 3: -Up
        };

        int alphaInt = (int) (255 * getCurrentAlpha(partialTick));
        int blockLight = LightTexture.FULL_BRIGHT & 0xFFFF;
        int skyLight = (LightTexture.FULL_BRIGHT >> 16) & 0xFFFF;

        // --- 1. Draw 4 Outer Side Quads (Single pass to preserve correct alpha blending) ---
        for (int i = 0; i < 4; i++) {
            int next = (i + 1) % 4;

            Vec3 o1 = faceOffsets[i];
            Vec3 o2 = faceOffsets[next];

            Vec3 s1 = start.add(o1);
            Vec3 s2 = start.add(o2);
            Vec3 e2 = end.add(o2);
            Vec3 e1 = end.add(o1);

            // Outward-facing normal for proper shader lighting
            Vec3 faceNormal = o1.add(o2).normalize();

            addVertex(consumer, pose, s1, faceNormal, alphaInt, blockLight, skyLight);
            addVertex(consumer, pose, s2, faceNormal, alphaInt, blockLight, skyLight);
            addVertex(consumer, pose, e2, faceNormal, alphaInt, blockLight, skyLight);
            addVertex(consumer, pose, e1, faceNormal, alphaInt, blockLight, skyLight);
        }

        // --- 2. Draw Start Cap (Normal points backwards along -dir) ---
        Vec3 startNormal = dir.reverse();
        addVertex(consumer, pose, start.add(faceOffsets[0]), startNormal, alphaInt, blockLight, skyLight);
        addVertex(consumer, pose, start.add(faceOffsets[3]), startNormal, alphaInt, blockLight, skyLight);
        addVertex(consumer, pose, start.add(faceOffsets[2]), startNormal, alphaInt, blockLight, skyLight);
        addVertex(consumer, pose, start.add(faceOffsets[1]), startNormal, alphaInt, blockLight, skyLight);

        // --- 3. Draw End Cap (Normal points forwards along +dir) ---
        Vec3 endNormal = dir;
        addVertex(consumer, pose, end.add(faceOffsets[0]), endNormal, alphaInt, blockLight, skyLight);
        addVertex(consumer, pose, end.add(faceOffsets[1]), endNormal, alphaInt, blockLight, skyLight);
        addVertex(consumer, pose, end.add(faceOffsets[2]), endNormal, alphaInt, blockLight, skyLight);
        addVertex(consumer, pose, end.add(faceOffsets[3]), endNormal, alphaInt, blockLight, skyLight);
    }

    private void addVertex(VertexConsumer consumer, PoseStack.Pose pose, Vec3 pos, Vec3 normal, int alpha, int blockLight, int skyLight) {
        consumer.addVertex(pose, (float) pos.x, (float) pos.y, (float) pos.z)
                .setColor(red, green, blue, alpha)
                .setUv2(blockLight, skyLight)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
    }

    /// @return true if the beam should be removed.
    public boolean tick() {
        return false;
    }

    public float getCurrentAlpha(float partialTick) {
        return baseAlpha;
    }

    public abstract BeamType<?> getType();

    // Getters
    public UUID getId() { return id; }
    public ResourceKey<Level> getDimension() { return dimension; }
    public Vec3 start() { return start; }
    public Vec3 end() { return end; }
    public int red() { return red; }
    public int green() { return green; }
    public int blue() { return blue; }
    public float radius() { return radius; }
}
