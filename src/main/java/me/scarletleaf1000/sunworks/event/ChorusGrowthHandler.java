package me.scarletleaf1000.sunworks.event;

import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.worldgen.DeadChorusPlantGrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;

@EventBusSubscriber(modid = Sunworks.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ChorusGrowthHandler {

    @SubscribeEvent
    public static void onChorusFlowerGrow(CropGrowEvent.Pre event) {
        LevelAccessor levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof Level level)) {
            return;
        }
        if (level.isClientSide()) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockState state = event.getState();

        if (!state.is(Blocks.CHORUS_FLOWER)) {
            return;
        }

        if (!DeadChorusPlantGrowth.hasAdjacentWitherRose(level, pos)) {
            return;
        }

        event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
        DeadChorusPlantGrowth.generateDeadPlant(level, pos, level.getRandom(), 2);
    }
}
