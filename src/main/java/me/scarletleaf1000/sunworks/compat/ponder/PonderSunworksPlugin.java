package me.scarletleaf1000.sunworks.compat.ponder;

import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.block.ModBlocks;
import me.scarletleaf1000.sunworks.compat.ponder.scenes.HelioreceiverScenes;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class PonderSunworksPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return Sunworks.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.forComponents(ModBlocks.HELIORECEIVER.getId(), ModBlocks.REFLECTION_PANEL.getId())
                .addStoryBoard("helioreceiver_ponder", HelioreceiverScenes::linking);
    }
}
