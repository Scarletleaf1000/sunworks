package me.scarletleaf1000.sunworks.block.entity.custom.generator;

import me.scarletleaf1000.sunworks.block.ModBlocks;
import me.scarletleaf1000.sunworks.block.custom.cable.AbstractPipeBlock;
import me.scarletleaf1000.sunworks.block.entity.ModBlockEntities;
import me.scarletleaf1000.sunworks.block.entity.energy.ModEnergyStorage;
import me.scarletleaf1000.sunworks.block.entity.energy.ModEnergyUtil;
import me.scarletleaf1000.sunworks.block.entity.io.ConfigurableMachine;
import me.scarletleaf1000.sunworks.block.entity.io.IOType;
import me.scarletleaf1000.sunworks.block.entity.io.RelativeSide;
import me.scarletleaf1000.sunworks.block.entity.io.SideConfiguration;
import me.scarletleaf1000.sunworks.screen.custom.HelioreceiverMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HelioreceiverBlockEntity extends BlockEntity implements MenuProvider, ConfigurableMachine {
    private static final Set<IOType> SUPPORTED_TYPES = Set.of(IOType.ENERGY_OUTPUT);
    private static final int MAX_STORAGE = 65536;
    private static final int MAX_TRANSFER = 1000;
    private static final int SCAN_INTERVAL = 40;
    private static final int MAX_DISTANCE = 9;
    private static final double MIN_COS = Math.cos(Math.toRadians(80));

    private final SideConfiguration sideConfiguration = new SideConfiguration();
    private final List<BlockPos> connectedPanels = new ArrayList<>();
    private boolean ejectEnabled = false;
    private int scanCooldown = 0;

    public HelioreceiverBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HELIORECEIVER_BE.get(), pos, state);
        for (RelativeSide side : RelativeSide.values()) {
            sideConfiguration.set(side, IOType.ENERGY_OUTPUT);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.menu.sunworks.helioreceiver");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new HelioreceiverMenu(containerId, inventory, this);
    }

    private final ModEnergyStorage ENERGY_STORAGE = createEnergyStorage();

    private ModEnergyStorage createEnergyStorage() {
        return new ModEnergyStorage(MAX_STORAGE, MAX_TRANSFER) {
            @Override
            public void onEnergyChanged() {
                setChanged();
                if (getLevel() != null) {
                    getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                }
            }
        };
    }

    private final IEnergyStorage EXTRACT_ONLY_STORAGE = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return ENERGY_STORAGE.extractEnergy(maxExtract, simulate);
        }

        @Override
        public int getEnergyStored() {
            return ENERGY_STORAGE.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return ENERGY_STORAGE.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    };

    public IEnergyStorage getEnergyStorage(@Nullable Direction direction) {
        if (direction == null) {
            return this.EXTRACT_ONLY_STORAGE;
        }
        RelativeSide side = RelativeSide.fromAbsolute(getFacing(), direction);
        return sideConfiguration.get(side) == IOType.ENERGY_OUTPUT ? this.EXTRACT_ONLY_STORAGE : null;
    }

    @Override
    public SideConfiguration getSideConfiguration() {
        return sideConfiguration;
    }

    @Override
    public Set<IOType> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public boolean isSideConfigurable(RelativeSide side) {
        return true;
    }

    @Override
    public boolean supportsEject() {
        return true;
    }

    @Override
    public boolean isEjectEnabled() {
        return ejectEnabled;
    }

    @Override
    public void setEjectEnabled(boolean enabled) {
        this.ejectEnabled = enabled;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        if (scanCooldown++ >= SCAN_INTERVAL) {
            scanCooldown = 0;
            scanReflectors(level, pos, state);
        }
        generatePower(level);
        ejectEnergy(level, pos);
    }

    private void generatePower(Level level) {
        int fe = 0;
        for (BlockPos panelPos : connectedPanels) {
            if (level.getBlockState(panelPos).is(ModBlocks.REFLECTION_PANEL.get())) {
                fe += 10 * getSunlightPower(level, panelPos);
            }
        }
        if (fe > 0) {
            ENERGY_STORAGE.receiveEnergy(fe, false);
        }
    }

    private void ejectEnergy(Level level, BlockPos pos) {
        if (!ejectEnabled) return;

        for (RelativeSide side : RelativeSide.values()) {
            if (sideConfiguration.get(side) != IOType.ENERGY_OUTPUT) continue;

            Direction absolute = side.toAbsolute(getFacing());
            BlockPos neighborPos = pos.relative(absolute);
            if (level.getBlockState(neighborPos).getBlock() instanceof AbstractPipeBlock) continue;

            ModEnergyUtil.move(pos, neighborPos, MAX_TRANSFER, level);
        }
    }

    private void scanReflectors(Level level, BlockPos pos, BlockState state) {
        connectedPanels.clear();
        BlockPos center = getBlockPos();
        int r = MAX_DISTANCE;
        for (BlockPos panelPos : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
            if (panelPos.distSqr(center) > (long) MAX_DISTANCE * MAX_DISTANCE) continue;
            if (!level.getBlockState(panelPos).is(ModBlocks.REFLECTION_PANEL.get())) continue;
            if (level.getBlockEntity(panelPos) instanceof ReflectionPanelBlockEntity panel) {
                BlockPos target = panel.getTarget();
                if (!center.equals(target)) continue;

                Vec3 panelCenter = panelPos.getCenter();
                Vec3 receiverCenter = center.getCenter();
                Vec3 dir = receiverCenter.subtract(panelCenter).normalize();
                if (dir.y < MIN_COS) continue;

                Vec3 start = panelCenter.add(dir.scale(0.75));
                BlockHitResult hit = level.clip(new ClipContext(start, receiverCenter, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
                if (hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(center)) {
                    connectedPanels.add(panelPos.immutable());
                }
            }
        }
    }

    private int getSunlightPower(Level level, BlockPos pos) {
        if (level.isClientSide) return 0;
        int minLight = 14;
        if (level.getDayTime() > 23000 || level.getDayTime() < 13000) {
            minLight = 9;
        }
        if (level.isRainingAt(pos)) minLight += 2;

        if (level.getBrightness(LightLayer.SKY, pos.above()) > minLight) {
            return level.getBrightness(LightLayer.SKY, pos.above()) - minLight;
        }
        return 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("helioreceiver.energy", ENERGY_STORAGE.getEnergyStored());
        tag.putBoolean("helioreceiver.eject_enabled", ejectEnabled);
        CompoundTag sideConfigTag = new CompoundTag();
        sideConfiguration.save(sideConfigTag);
        tag.put("side_config", sideConfigTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ENERGY_STORAGE.setEnergy(tag.getInt("helioreceiver.energy"));
        ejectEnabled = tag.getBoolean("helioreceiver.eject_enabled");
        if (tag.contains("side_config")) {
            sideConfiguration.load(tag.getCompound("side_config"));
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        super.onDataPacket(net, pkt, registries);
    }
}
