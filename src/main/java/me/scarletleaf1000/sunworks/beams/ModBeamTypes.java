package me.scarletleaf1000.sunworks.beams;

import me.scarletleaf1000.sunworks.Sunworks;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBeamTypes {
    public static final ResourceKey<Registry<BeamType<?>>> BEAM_TYPE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Sunworks.MOD_ID, "beam_type"));

    public static final DeferredRegister<BeamType<?>> BEAM_TYPES =
            DeferredRegister.create(BEAM_TYPE_REGISTRY_KEY, Sunworks.MOD_ID);

    public static final Registry<BeamType<?>> REGISTRY =
            BEAM_TYPES.makeRegistry(builder -> builder.sync(true));

    public static final DeferredHolder<BeamType<?>, BeamType<PersistentBeam>> PERSISTENT =
            BEAM_TYPES.register("persistent", () -> new BeamType<>(PersistentBeam.STREAM_CODEC));
    public static final DeferredHolder<BeamType<?>, BeamType<TemporaryBeam>> TEMPORARY =
            BEAM_TYPES.register("temporary", () -> new BeamType<>(TemporaryBeam.STREAM_CODEC));

    public static void register(IEventBus modEventBus) {
        BEAM_TYPES.register(modEventBus);
    }
}
