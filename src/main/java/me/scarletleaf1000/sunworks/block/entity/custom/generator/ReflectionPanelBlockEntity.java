package me.scarletleaf1000.sunworks.block.entity.custom.generator;

import me.scarletleaf1000.sunworks.block.entity.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class ReflectionPanelBlockEntity extends BlockEntity {
    @Nullable
    private BlockPos target;

    public ReflectionPanelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REFLECTION_PANEL_BE.get(), pos, state);
    }

    @Nullable
    public BlockPos getTarget() {
        return target;
    }

    public void setTarget(@Nullable BlockPos target) {
        this.target = target;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    private static final DustParticleOptions ORANGE_DUST = new DustParticleOptions(new Vector3f(1.0f, 0.5f, 0.0f), 1.0f);

    public void renderTarget(Player player) {
        if (this.target != null) {
            if (level instanceof ServerLevel serverLevel) {
                spawnConnectionParticles(serverLevel, getBlockPos(), target);
                spawnOutlineParticles(serverLevel, target);
            }
        } else {
            actionBarMessage(player, "message.sunworks.panel.notarget", ChatFormatting.RED);
        }
    }

    private void spawnConnectionParticles(ServerLevel level, BlockPos panelPos, BlockPos targetPos) {
        Vec3 start = panelPos.getCenter();
        Vec3 end = targetPos.getCenter();
        Vec3 dir = end.subtract(start);
        double length = dir.length();
        Vec3 step = dir.normalize().scale(0.5);
        int steps = (int) Math.ceil(length / 0.5);
        for (int i = 0; i <= steps; i++) {
            Vec3 point = start.add(step.scale(i));
            level.sendParticles(ORANGE_DUST, point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }
    }

    private void spawnOutlineParticles(Level level, BlockPos pos) {
        for (int i = 0; i < 8; i++) {
            double x = pos.getX() + ((i & 1) == 0 ? 0.1 : 0.9);
            double y = pos.getY() + ((i & 2) == 0 ? 0.1 : 0.9);
            double z = pos.getZ() + ((i & 4) == 0 ? 0.1 : 0.9);
            level.addParticle(ORANGE_DUST, x, y, z, 0, 0, 0);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (target != null) {
            tag.putLong("target", target.asLong());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        target = tag.contains("target") ? BlockPos.of(tag.getLong("target")) : null;
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

    private static void actionBarMessage(Player player, String key, ChatFormatting color) {
        player.displayClientMessage(Component.translatable(key).withStyle(color), true);
    }
}
