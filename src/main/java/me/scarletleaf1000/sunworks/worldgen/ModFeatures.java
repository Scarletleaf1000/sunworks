package me.scarletleaf1000.sunworks.worldgen;

import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.worldgen.feature.DeadChorusPlantFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Sunworks.MOD_ID);

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> DEAD_CHORUS_PLANT = FEATURES.register(
            "dead_chorus_plant",
            () -> new DeadChorusPlantFeature(NoneFeatureConfiguration.CODEC));

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}
