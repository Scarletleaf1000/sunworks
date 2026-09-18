package me.scarletleaf1000.sunworks.block.entity.custom.multiblock;

import me.scarletleaf1000.sunworks.block.custom.ModifierBlock;
import me.scarletleaf1000.sunworks.block.entity.ModBlockEntities;
import me.scarletleaf1000.sunworks.block.entity.custom.ISyncedBlockEntity;
import me.scarletleaf1000.sunworks.block.entity.energy.ModEnergyStorage;
import me.scarletleaf1000.sunworks.multiblocks.MBBuildingBlock;
import me.scarletleaf1000.sunworks.multiblocks.MultiblockTileController;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CoronaTapBlockEntity extends MultiblockTileController implements ISyncedBlockEntity {

    public static final int DEFAULT_BURST_DELAY_TICKS = 2400; // 2 mins TODO add to config
    public static final int DEFAULT_GENERATION_FE = 50_000_000; // TODO add to config
    public int burstDelayTicks = DEFAULT_BURST_DELAY_TICKS;
    public int generationFE = DEFAULT_GENERATION_FE;

    protected int delayTick = burstDelayTicks;

    private static Map<BlockPos, MBBuildingBlock> structure = null;

    ModEnergyStorage energy = new ModEnergyStorage(300_000_000, Integer.MAX_VALUE) { // The ports define the max in/out
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
        if (delayTick!=0) {
            delayTick--;
            return;
        }
        delayTick = burstDelayTicks;

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

        CoronaTapBlockEntity.structure = structure;
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

        delayTick = burstDelayTicks;
    }

    @Override
    protected void handleBuiltStateChange(boolean built) {
        assert level != null;

        if (built) {
            for (EnergyPortBlockEntity port : getBlockEntities(MBBuildingBlock.ENERGY_PORT, EnergyPortBlockEntity.class))
                port.addOwnersCapability(this.energy);

            recalcModifiers();
        }
        else {
            for (EnergyPortBlockEntity port : getBlockEntities(MBBuildingBlock.ENERGY_PORT, EnergyPortBlockEntity.class))
                port.invalidate();

            burstDelayTicks = DEFAULT_BURST_DELAY_TICKS;
            generationFE = DEFAULT_GENERATION_FE;
        }

        setChanged();
    }

    @Override
    protected void onStructureBlocksUpdated() {
        super.onStructureBlocksUpdated();
        assert level != null;

        for (EnergyPortBlockEntity port : getBlockEntities(MBBuildingBlock.ENERGY_PORT, EnergyPortBlockEntity.class))
            port.addOwnersCapability(this.energy);

        recalcModifiers();
    }

    // Registered only as null to allow for Waila-like mods to work
    public IEnergyStorage getEnergyStorage(@Nullable Direction direction) {
        if (direction != null) return null;
        return energy;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("energy", energy.getEnergyStored());

        tag.putInt("burstDelayTicks", burstDelayTicks);
        tag.putInt("generationFE", generationFE);
        tag.putInt("delayTick", delayTick);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        this.energy.setEnergy(tag.getInt("energy"));

        burstDelayTicks = tag.getInt("burstDelayTicks");
        generationFE = tag.getInt("generationFE");
        delayTick = tag.getInt("delayTick");

        if (this.level != null && this.level.isClientSide() && isBuilt())
            for (EnergyPortBlockEntity port : getBlockEntities(MBBuildingBlock.ENERGY_PORT, EnergyPortBlockEntity.class))
                port.addOwnersCapability(this.energy);
    }

    @Override
    public void writeSyncData(CompoundTag tag, HolderLookup.Provider registries) {
        saveAdditional(tag, registries);
    }

    @Override
    public void readSyncData(CompoundTag tag, HolderLookup.Provider registries) {
        loadAdditional(tag, registries);
    }
}
