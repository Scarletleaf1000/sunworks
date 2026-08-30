package me.scarletleaf1000.sunworks.item;

import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.item.custom.CorruptedPearlItem;
import me.scarletleaf1000.sunworks.item.custom.SolarWrenchItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Sunworks.MOD_ID);

    public static final DeferredItem<Item> HELIOLITE_SHARD = ITEMS.registerItem("heliolite_shard",
            Item::new, new Item.Properties());

    public static final DeferredItem<Item> CINDERITE_INGOT = ITEMS.registerItem("cinderite_ingot",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> RAW_CINDERITE = ITEMS.registerItem("raw_cinderite",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> SILVER_INGOT = ITEMS.registerItem("silver_ingot",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> RAW_SILVER = ITEMS.registerItem("raw_silver",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> CINDERITE_NUGGET = ITEMS.registerItem("cinderite_nugget",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> SILVER_NUGGET = ITEMS.registerItem("silver_nugget",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> ELECTRUM_INGOT = ITEMS.registerItem("electrum_ingot",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> ELECTRUM_NUGGET = ITEMS.registerItem("electrum_nugget",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> CINDERSTEEL_INGOT = ITEMS.registerItem("cindersteel_ingot",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> CINDERSTEEL_NUGGET = ITEMS.registerItem("cindersteel_nugget",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> VOIDSTEEL_INGOT = ITEMS.registerItem("voidsteel_ingot",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> VOIDSTEEL_NUGGET = ITEMS.registerItem("voidsteel_nugget",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> SILICON = ITEMS.registerItem("silicon",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> HEAT_CORE = ITEMS.registerItem("heat_core",
            Item::new, new Item.Properties());
    public static final DeferredItem<Item> SOLAR_PANEL_COMPONENT = ITEMS.registerItem("solar_panel_component",
            Item::new, new Item.Properties());

    public static final DeferredItem<Item> CHORUS_HUSK = ITEMS.registerItem("chorus_husk",
            Item::new, new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(1)
                    .saturationModifier(0.25f)
                    .alwaysEdible()
                    .effect(new MobEffectInstance(MobEffects.WITHER, 60, 0), 1.0f)
                    .build()));
    public static final DeferredItem<Item> CORRUPTED_PEARL = ITEMS.registerItem("corrupted_pearl",
            CorruptedPearlItem::new, new Item.Properties().stacksTo(16));

    public static final DeferredItem<Item> SOLAR_WRENCH = ITEMS.registerItem("solar_wrench",
            SolarWrenchItem::new, new Item.Properties().stacksTo(1));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
