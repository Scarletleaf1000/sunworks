package me.scarletleaf1000.sunworks.block.entity.custom.multiblock;

import me.scarletleaf1000.sunworks.beams.BeamUtil;
import me.scarletleaf1000.sunworks.beams.ServerBeamManager;
import me.scarletleaf1000.sunworks.beams.TemporaryBeam;
import me.scarletleaf1000.sunworks.block.custom.ModifierBlock;
import me.scarletleaf1000.sunworks.block.entity.ModBlockEntities;
import me.scarletleaf1000.sunworks.block.entity.custom.ISyncedBlockEntity;
import me.scarletleaf1000.sunworks.block.entity.energy.ModEnergyStorage;
import me.scarletleaf1000.sunworks.multiblocks.MBBuildingBlock;
import me.scarletleaf1000.sunworks.multiblocks.MultiblockTileController;
import me.scarletleaf1000.sunworks.multiblocks.ports.IEnergyPortHost;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CoronaTapBlockEntity extends MultiblockTileController implements ISyncedBlockEntity, IEnergyPortHost {

    public static final int BEAM_LIFESPAN = 60; // 3 seconds
    public static final int DEFAULT_BURST_DELAY_TICKS = 2400; // 2 mins TODO add to config
    public static final int DEFAULT_GENERATION_FE = 50_000_000; // TODO add to config
    public int burstDelayTicks = DEFAULT_BURST_DELAY_TICKS;
    public int generationFE = DEFAULT_GENERATION_FE;

    protected int delayTick = burstDelayTicks;

    protected @Nullable TemporaryBeam beam;

    private static Map<BlockPos, MBBuildingBlock> structure = null;

    ModEnergyStorage energy = new ModEnergyStorage(300_000_000, 0, Integer.MAX_VALUE) { // The ports define the max in/out
        @Override
        public void onEnergyChanged() {
            setChanged();
            syncToTrackingClients();
        }
    };

    public CoronaTapBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.CORONA_TAP_BE.get(), pos, blockState);
    }

    @Override
    public void tick() {
        super.tick();

        if (!isBuilt()) return;
        if (delayTick == BEAM_LIFESPAN)
            beam = createTapBeam();
        else if (delayTick <= 20)
            createPowerParticles();

        if (delayTick != 0) {
            delayTick--;
            return;
        }
        delayTick = burstDelayTicks;
        beam = null;

        int totalEnergy = Math.min(generationFE + energy.getEnergyStored(), energy.getMaxEnergyStored());
        energy.setEnergy(totalEnergy);
    }

    @Override
    protected Map<BlockPos, @NotNull MBBuildingBlock> getRelativeStructure() {
        if (CoronaTapBlockEntity.structure != null) return CoronaTapBlockEntity.structure;

        Map<BlockPos, MBBuildingBlock> structure = new HashMap<>();

        // 1. Top ring at Y = 2 (16 Corona Tap Modifier blocks)
        for (int x = -2; x <= 2; x++)
            for (int z = -2; z <= 2; z++)
                if (Math.abs(x) == 2 || Math.abs(z) == 2)
                    structure.put(new BlockPos(x, 2, z), MBBuildingBlock.CORONA_TAP_MODIFIER_BLOCK);

        // 2. Base layer at Y = -2 (Energy Ports on sides, Floor in corners and center)
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                boolean isPerimeter = Math.abs(x) == 2 || Math.abs(z) == 2;
                boolean isCorner = Math.abs(x) == 2 && Math.abs(z) == 2;

                if (isPerimeter && !isCorner) {
                    // Outer ring sides (12 blocks)
                    structure.put(new BlockPos(x, -2, z), MBBuildingBlock.ENERGY_PORT);
                } else {
                    // Corners and 3x3 interior floor (13 blocks)
                    structure.put(new BlockPos(x, -2, z), MBBuildingBlock.SIMPLE_CASING_BLOCK);
                }
            }
        }

        // 3. Corner pillars connecting Y = 2 down to the floor at Y = -2 (Y = 1, 0, -1)
        int[] cornerCoords = {-2, 2};
        for (int x : cornerCoords) {
            for (int z : cornerCoords) {
                for (int y = -1; y <= 1; y++) {
                    structure.put(new BlockPos(x, y, z), MBBuildingBlock.SIMPLE_CASING_BLOCK);
                }
            }
        }

        // 4. Air in a 3x3x3 around the tap
        for (int x = -1; x <= 1; x++)
            for (int y = -1; y <= 1; y++)
                for (int z = -1; z <= 1; z++)
                    if (!(x == 0 && y == 0 && z == 0))
                        structure.put(new BlockPos(x, y, z), MBBuildingBlock.AIR);

        CoronaTapBlockEntity.structure = Collections.unmodifiableMap(structure);
        return structure;
    }

    protected void recalcModifiers() {
        int totalGenerationSum = 0;
        int totalDelaySum = 0;
        float totalGenerationMult = 1;
        float totalDelayMult = 1;
        for (BlockState modifierState : getBlockStates(MBBuildingBlock.CORONA_TAP_MODIFIER_BLOCK, ModifierBlock.class)) {
            ModifierBlock modifier = (ModifierBlock) modifierState.getBlock();
            totalGenerationSum += modifier.generationAddition;
            totalDelaySum += modifier.delayAddition;
            totalGenerationMult *= modifier.generationMult;
            totalDelayMult *= modifier.delayMult;
        }
        generationFE = (int) ((DEFAULT_GENERATION_FE + totalGenerationSum) * totalGenerationMult);
        burstDelayTicks = (int) ((DEFAULT_BURST_DELAY_TICKS + totalDelaySum) * totalDelayMult);

        this.delayTick = Math.min(this.delayTick, this.burstDelayTicks);
    }

    protected TemporaryBeam createTapBeam() {
        if (level == null || level.isClientSide() || !(level instanceof ServerLevel serverLevel))
            return null;

        return BeamUtil.createTemporaryOrangeBeam(
                serverLevel, worldPosition.getCenter(),
                worldPosition.above(100).getCenter(),
                0.2f, 0.35f, BEAM_LIFESPAN, 1f
        );
    }

    protected void createPowerParticles() {
        if (this.level == null || !(this.level instanceof ServerLevel serverLevel)) return;

        BlockPos pos = this.getBlockPos();
        Vec3 center = pos.getCenter(); // Center at (X + 0.5, Y + 0.5, Z + 0.5)

        // Glowing beam core rising directly up the center
        for (int i = 0; i < 5; i++) {
            double coreOffset = (serverLevel.random.nextDouble() - 0.5) * 0.25;

            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    center.x + coreOffset,
                    pos.getY() + 1.0,
                    center.z + coreOffset,
                    1,            // Spawn 1 particle
                    0.0, 0.5, 0.0, // Upward velocity
                    0.03          // Speed jitter
            );
        }

        if (delayTick == 1) {
            serverLevel.sendParticles(
                    ParticleTypes.FLASH,
                    center.x, pos.getY() + 1.5, center.z,
                    1, 0, 0, 0, 0
            );
        }
    }

    @Override
    protected void onStructureFormed() {
        recalcModifiers();
    }

    @Override
    protected void onStructureUnformed() {
        this.burstDelayTicks = DEFAULT_BURST_DELAY_TICKS;
        this.generationFE = DEFAULT_GENERATION_FE;

        if (beam != null && (level instanceof ServerLevel serverLevel))
            ServerBeamManager.removeBeam(serverLevel ,beam.getId());
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (beam != null && (level instanceof ServerLevel serverLevel))
            ServerBeamManager.removeBeam(serverLevel ,beam.getId());
    }

    @Override
    protected void onStructureUpdated() {
        recalcModifiers();
    }

    // Registered only as null to allow for Waila-like mods to work
    public IEnergyStorage getEnergyStorage(@Nullable Direction direction) {
        if (direction != null) return null;
        return energy;
    }

    @Override
    public @NotNull IEnergyStorage getMultiblockEnergy() {
        return energy;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy.getEnergyStored());
        tag.putInt("burstDelayTicks", burstDelayTicks);
        tag.putInt("generationFE", generationFE);
        tag.putInt("delayTick", delayTick);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.energy.setEnergy(tag.getInt("energy"));

        burstDelayTicks = tag.getInt("burstDelayTicks");
        generationFE = tag.getInt("generationFE");
        delayTick = tag.getInt("delayTick");
    }

    @Override
    public void writeSyncData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("energy", energy.getEnergyStored());
    }

    @Override
    public void readSyncData(CompoundTag tag, HolderLookup.Provider registries) {
        this.energy.setEnergy(tag.getInt("energy"));
    }
}
