package me.scarletleaf1000.sunworks.block.entity.custom.logistics;

import me.scarletleaf1000.sunworks.beams.BeamUtil;
import me.scarletleaf1000.sunworks.beams.ServerBeamManager;
import me.scarletleaf1000.sunworks.block.ModBlocks;
import me.scarletleaf1000.sunworks.block.entity.ModBlockEntities;
import me.scarletleaf1000.sunworks.block.entity.energy.ModEnergyUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.UUID;

public class LaserRelayBlockEntity extends BlockEntity {
    private final int TRANSFER_AMOUNT;
    private final int MAX_DISTANCE;

    private UUID beamId;
    private Vec3 lastHitLoc;

    public LaserRelayBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.LASER_RELAY_BE.get(), pos, blockState);


        if (blockState.is(ModBlocks.SINGLE_LASER_RELAY.get())) {
            TRANSFER_AMOUNT = 8192;
            MAX_DISTANCE = 32;
        } else if (blockState.is(ModBlocks.DOUBLE_LASER_RELAY.get())) {
            TRANSFER_AMOUNT = 32768;
            MAX_DISTANCE = 64;
        } else {
            TRANSFER_AMOUNT = 0;
            MAX_DISTANCE = 0;
        }
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        Direction beamDir = state.getValue(BlockStateProperties.FACING);
        BlockPos fromPos = pos.relative(beamDir.getOpposite());   // the mount block
        if (!ModEnergyUtil.doesBlockHaveEnergyStorage(fromPos, beamDir, serverLevel)) return;

        Vec3 startVec = Vec3.atCenterOf(pos).relative(beamDir, 0.5);       // half block in front of the laser
        Vec3 endVec = startVec.add(
                Vec3.atLowerCornerOf(beamDir.getNormal()).scale(MAX_DISTANCE));

        BlockHitResult hit = serverLevel.clip(new ClipContext(
                startVec, endVec,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                CollisionContext.empty()));

        BlockPos toPos = hit.getBlockPos();

        Vec3 hitLoc = hit.getLocation();
        if (hit.getType() == HitResult.Type.MISS) hitLoc = endVec;

        IEnergyStorage source = serverLevel.getCapability(
                Capabilities.EnergyStorage.BLOCK, fromPos, beamDir);
        boolean shouldShow = source != null && source.getEnergyStored() > 0;


        Vec3 beamStartVec = Vec3.atCenterOf(pos).relative(beamDir.getOpposite(), 0.25);

        if (shouldShow && beamId == null) {
            spawnBeam(serverLevel, beamStartVec, hitLoc);        // store new id
        } else if (!shouldShow && beamId != null) {
            ServerBeamManager.removeBeam(serverLevel, beamId);
            beamId = null;
        } else if (shouldShow && !hitLoc.equals(lastHitLoc)) {
            ServerBeamManager.removeBeam(serverLevel, beamId);
            spawnBeam(serverLevel, beamStartVec, hitLoc);
        }
        lastHitLoc = hitLoc;
        if (shouldShow) {
            if (level.getGameTime() % 40 == 0)
                level.playSeededSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BEACON_AMBIENT,
                    SoundSource.BLOCKS, 0.15f, 1.25f, level.random.nextLong()
            );
        }

        if (!ModEnergyUtil.doesBlockHaveEnergyStorage(toPos, beamDir.getOpposite(), serverLevel)) return;

        ModEnergyUtil.move(fromPos, toPos, TRANSFER_AMOUNT, serverLevel);
    }

    private void spawnBeam(ServerLevel serverLevel, Vec3 startVec, Vec3 hitLoc) {
        var beamAlpha = this.getBlockState().is(ModBlocks.DOUBLE_LASER_RELAY.get()) ? 0.7f : 0.9f;
        var beam = BeamUtil.createPersistentOrangeBeam(serverLevel, startVec, hitLoc, beamAlpha, 0.1f);
        beamId = beam.getId();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (this.getLevel() instanceof ServerLevel level) {
            ServerBeamManager.removeBeam(level, beamId);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (this.getLevel() instanceof ServerLevel level) {
            ServerBeamManager.removeBeam(level, beamId);
        }
    }
}
