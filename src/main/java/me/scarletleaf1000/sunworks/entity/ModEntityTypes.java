package me.scarletleaf1000.sunworks.entity;

import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.entity.projectile.ThrownCorruptedPearl;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Sunworks.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<ThrownCorruptedPearl>> CORRUPTED_PEARL = ENTITY_TYPES.register(
            "corrupted_pearl",
            () -> EntityType.Builder.<ThrownCorruptedPearl>of(ThrownCorruptedPearl::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("corrupted_pearl"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
