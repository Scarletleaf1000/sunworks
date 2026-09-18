package me.scarletleaf1000.sunworks.multiblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class MultiblockTileController extends BlockEntity {
    private int countdown = 0;
    private boolean built = false;
    // Pre-computed lookup map: MBBuildingBlock -> Set<BlockPos>
    private Map<MBBuildingBlock, Set<BlockPos>> blockToPositionsCache;
    // Stores the actual relative BlockStates detected during the last validation check
    private Map<BlockPos, BlockState> lastStructureSnapshot = new HashMap<>();

    public MultiblockTileController(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    /**
     * Defines the block layout of the multiblock structure.
     *
     * The controller block serves as the origin point at relative position (0,0,0).
     * Any spatial transformations—such as rotation or mirroring—must be applied to
     * the returned map coordinates.
     *
     * @return A mapping of relative {@link BlockPos} offsets from the controller
     *         to their corresponding {@link MBBuildingBlock} validation rules.
     */
    protected abstract Map<BlockPos, @NotNull MBBuildingBlock> getRelativeStructure();

    /// Used to invalidate stuff like capability blocks
    protected abstract void handleBuiltStateChange(boolean built);

    /// Used to change multiblock values, e.g. generation amount and capability ports
    protected void onStructureBlocksUpdated() {
        // Called when a block inside a formed structure changes to another valid block
        setChanged();
    }

    protected void cacheStructurePositions() {
        this.blockToPositionsCache = new HashMap<>();
        getRelativeStructure().forEach((pos, block) ->
                blockToPositionsCache.computeIfAbsent(block, k -> new HashSet<>()).add(pos));
    }

    public Set<BlockPos> getRelativePositionsForBlock(MBBuildingBlock targetBlock) {
        if (blockToPositionsCache == null) {
            cacheStructurePositions();
        }
        return blockToPositionsCache.getOrDefault(targetBlock, Collections.emptySet());
    }

    public <BE extends BlockEntity> Set<BE> getBlockEntities(MBBuildingBlock targetBlock, Class<BE> clazz) {
        if (level == null) return Collections.emptySet();

        Set<BE> found = new HashSet<>();
        for (BlockPos pos : getRelativePositionsForBlock(targetBlock)) {
            BlockEntity be = level.getBlockEntity(worldPosition.offset(pos));
            if (clazz.isInstance(be))
                found.add(clazz.cast(be));

        }
        return found;
    }

    public <B extends Block> List<BlockState> getBlockStates(MBBuildingBlock targetBlock, Class<B> clazz) {
        if (level == null) return Collections.emptyList();

        List<BlockState> found = new ArrayList<>();
        for (BlockPos pos : getRelativePositionsForBlock(targetBlock)) {
            BlockState state = level.getBlockState(worldPosition.offset(pos));
            if (clazz.isInstance(state.getBlock()))
                found.add(state);
        }
        return found;
    }

    public List<BlockState> getBlockStates(MBBuildingBlock targetBlock) {
        return getBlockStates(targetBlock, Block.class);
    }

    public boolean isStructureValid() {
        if (level == null) return false;

        Map<BlockPos, BlockState> newSnapshot = new HashMap<>();

        for (Map.Entry<BlockPos, MBBuildingBlock> entry : getRelativeStructure().entrySet()) {
            BlockPos relativePos = entry.getKey();
            BlockPos absolutePos = worldPosition.offset(relativePos);

            if (!level.isLoaded(absolutePos)) {
                return false;
            }

            BlockState currentState = level.getBlockState(absolutePos);

            // Check if the block is valid for this relative position
            if (!entry.getValue().test(currentState)) {
                // Clear snapshot so stale data isn't preserved while broken
                this.lastStructureSnapshot.clear();
                return false;
            }

            // Record the actual BlockState found in the world
            newSnapshot.put(relativePos, currentState);
        }

        // Check if any block changed from the last snapshot while already built
        if (this.built && hasStructureChanged(newSnapshot)) {
            onStructureBlocksUpdated();
        }

        // Update the saved snapshot
        this.lastStructureSnapshot = newSnapshot;
        return true;
    }

    private boolean hasStructureChanged(Map<BlockPos, BlockState> newSnapshot) {
        // If we have no previous snapshot (e.g., right after world load), don't trigger updates
        if (lastStructureSnapshot.isEmpty()) {
            return false;
        }

        if (lastStructureSnapshot.size() != newSnapshot.size()) {
            return true;
        }

        for (Map.Entry<BlockPos, BlockState> entry : newSnapshot.entrySet()) {
            BlockState oldState = lastStructureSnapshot.get(entry.getKey());
            BlockState newState = entry.getValue();

            if (oldState == null || !oldState.equals(newState)) {
                return true;
            }
        }

        return false;
    }

    public void tick() {
        if (level == null) return;

        if (countdown > 0) {
            countdown--;
            return;
        }
        countdown = 10; // half a second

        boolean currentlyValid = isStructureValid();
        if (this.built != currentlyValid) {
            this.built = currentlyValid;
            handleBuiltStateChange(built);
            setChanged(); // Mark chunk dirty when structure state changes
        }
    }

    public boolean isBuilt() {
        return built;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("built", built);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        built = tag.getBoolean("built");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putBoolean("built", this.built);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
