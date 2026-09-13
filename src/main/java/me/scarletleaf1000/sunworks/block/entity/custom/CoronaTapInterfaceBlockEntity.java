package me.scarletleaf1000.sunworks.block.entity.custom;

import me.scarletleaf1000.sunworks.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CoronaTapInterfaceBlockEntity extends BlockEntity {
    public CoronaTapInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CORONA_TAP_INTERFACE_BE.get(), pos, state);
    }
}
