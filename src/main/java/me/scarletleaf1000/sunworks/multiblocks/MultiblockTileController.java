package me.scarletleaf1000.sunworks.multiblocks;

import me.scarletleaf1000.sunworks.multiblocks.ports.CapabilityPortBlockEntity;
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

    private Map<MBBuildingBlock, Set<BlockPos>> blockToPositionsCache;
    private Map<BlockPos, BlockState> lastStructureSnapshot = new HashMap<>();

    public MultiblockTileController(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    // ==========================================
    // ABSTRACT & OPTIONAL HOOKS
    // ==========================================

    protected abstract Map<BlockPos, @NotNull MBBuildingBlock> getRelativeStructure();

    /** Called when the structure forms for the first time. */
    protected void onStructureFormed() {}

    /** Called when the structure breaks. */
    protected void onStructureUnformed() {}

    /** Called when a valid block inside a formed multiblock changes state. */
    protected void onStructureUpdated() {}

    // ==========================================
    // GENERIC PORT & STRUCTURE LIFECYCLE
    // ==========================================

    protected void handleBuiltStateChange(boolean built) {
        if (built) {
            bindAllPorts();
            onStructureFormed();
        } else {
            unbindAllPorts();
            onStructureUnformed();
        }
    }

    protected void onStructureBlocksUpdated() {
        bindAllPorts();
        onStructureUpdated();
        setChanged();
    }

    public void bindAllPorts() {
        if (level == null) return;
        for (Map.Entry<BlockPos, MBBuildingBlock> entry : getRelativeStructure().entrySet()) {
            BlockEntity be = level.getBlockEntity(worldPosition.offset(entry.getKey()));
            if (be instanceof CapabilityPortBlockEntity<?> port) {
                port.bindToController(this); // Double dispatch: port handles its own interface binding
            }
        }
    }

    public void unbindAllPorts() {
        if (level == null) return;
        for (Map.Entry<BlockPos, MBBuildingBlock> entry : getRelativeStructure().entrySet()) {
            BlockEntity be = level.getBlockEntity(worldPosition.offset(entry.getKey()));
            if (be instanceof CapabilityPortBlockEntity<?> port) {
                port.invalidatePorts();
            }
        }
    }

    // ==========================================
    // CACHING & LOOKUPS
    // ==========================================

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
            if (clazz.isInstance(be)) {
                found.add(clazz.cast(be));
            }
        }
        return found;
    }

    public <B extends Block> List<BlockState> getBlockStates(MBBuildingBlock targetBlock, Class<B> clazz) {
        if (level == null) return Collections.emptyList();

        List<BlockState> found = new ArrayList<>();
        for (BlockPos pos : getRelativePositionsForBlock(targetBlock)) {
            BlockState state = level.getBlockState(worldPosition.offset(pos));
            if (clazz.isInstance(state.getBlock())) {
                found.add(state);
            }
        }
        return found;
    }

    public List<BlockState> getBlockStates(MBBuildingBlock targetBlock) {
        return getBlockStates(targetBlock, Block.class);
    }

    // ==========================================
    // VALIDATION & TICK LOGIC
    // ==========================================

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
            if (!entry.getValue().test(currentState)) {
                this.lastStructureSnapshot.clear();
                return false;
            }

            newSnapshot.put(relativePos, currentState);
        }

        boolean wasWorldLoad = this.lastStructureSnapshot.isEmpty();

        if (this.built && hasStructureChanged(newSnapshot)) {
            onStructureBlocksUpdated();
        } else if (this.built && wasWorldLoad) {
            // Re-bind ports after world load/restart!
            bindAllPorts();
        }

        this.lastStructureSnapshot = newSnapshot;
        return true;
    }

    private boolean hasStructureChanged(Map<BlockPos, BlockState> newSnapshot) {
        if (lastStructureSnapshot.isEmpty()) return false;
        if (lastStructureSnapshot.size() != newSnapshot.size()) return true;

        for (Map.Entry<BlockPos, BlockState> entry : newSnapshot.entrySet()) {
            BlockState oldState = lastStructureSnapshot.get(entry.getKey());
            BlockState newState = entry.getValue();
            if (oldState == null || !oldState.equals(newState)) return true;
        }
        return false;
    }

    public void tick() {
        if (level == null || level.isClientSide()) return;

        if (countdown > 0) {
            countdown--;
            return;
        }
        countdown = 10;

        // Check if all multiblock chunks are loaded before validating
        if (!areAllStructureChunksLoaded()) {
            return; // Pause validation until chunks load
        }

        boolean currentlyValid = isStructureValid();
        if (this.built != currentlyValid) {
            this.built = currentlyValid;
            handleBuiltStateChange(built);
            setChanged();
        }
    }

    private boolean areAllStructureChunksLoaded() {
        if (level == null) return false;
        for (BlockPos relativePos : getRelativeStructure().keySet()) {
            if (!level.isLoaded(worldPosition.offset(relativePos))) {
                return false;
            }
        }
        return true;
    }

    public boolean isBuilt() {
        return built;
    }

    // ==========================================
    // SERIALIZATION & PACKETS
    // ==========================================

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
