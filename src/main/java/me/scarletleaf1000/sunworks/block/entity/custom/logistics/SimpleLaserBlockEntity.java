package me.scarletleaf1000.sunworks.block.entity.custom.logistics;

import me.scarletleaf1000.sunworks.block.entity.ModBlockEntities;
import me.scarletleaf1000.sunworks.block.entity.energy.ModEnergyUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class SimpleLaserBlockEntity extends BlockEntity {
    private final int TRANSFER_AMOUNT = 4096;
    private final int MAX_DISTANCE = 64;

    public SimpleLaserBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.SIMPLE_LASER_BE.get(), pos, blockState);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        Direction beamDir = state.getValue(BlockStateProperties.FACING);
        BlockPos fromPos = pos.relative(beamDir.getOpposite());   // the mount block
        if (!ModEnergyUtil.doesBlockHaveEnergyStorage(fromPos, beamDir, serverLevel)) return;

        Vec3 startVec = pos.relative(beamDir).getCenter();        // one block in front of the laser
        Vec3 endVec = startVec.add(
                Vec3.atLowerCornerOf(beamDir.getNormal()).scale(MAX_DISTANCE));

        BlockHitResult hit = serverLevel.clip(new ClipContext(
                startVec, endVec,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                CollisionContext.empty()));

        BlockPos toPos = hit.getBlockPos();
        if (!ModEnergyUtil.doesBlockHaveEnergyStorage(toPos, beamDir.getOpposite(), serverLevel)) return;

        ModEnergyUtil.move(fromPos, toPos, TRANSFER_AMOUNT, serverLevel);
    }
}
