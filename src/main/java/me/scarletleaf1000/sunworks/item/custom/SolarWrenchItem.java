package me.scarletleaf1000.sunworks.item.custom;

import me.scarletleaf1000.sunworks.block.ModBlocks;
import me.scarletleaf1000.sunworks.block.entity.custom.generator.ReflectionPanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class SolarWrenchItem extends Item {
    private static final String TAG_SELECTED = "selected_panel";
    private static final DustParticleOptions ORANGE_DUST = new DustParticleOptions(new Vector3f(1.0f, 0.5f, 0.0f), 1.0f);
    private static final int MAX_LINK_DISTANCE = 6;
    private static final double MIN_COS = Math.cos(Math.toRadians(80));

    public SolarWrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos clicked = ctx.getClickedPos();
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();
        if (player == null) {
            return InteractionResult.PASS;
        }

        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        boolean clickedPanel = level.getBlockState(clicked).is(ModBlocks.REFLECTION_PANEL.get());
        boolean clickedReceiver = level.getBlockState(clicked).is(ModBlocks.HELIORECEIVER.get());

        if (clickedPanel) {
            if (!level.isClientSide()) {
                BlockPos previous = getSelected(stack);
                setSelected(stack, clicked);
                if (previous != null && !previous.equals(clicked)) {
                    actionBarMessage(player, "message.sunworks.wrench.discarded", ChatFormatting.YELLOW);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (clickedReceiver) {
            if (!level.isClientSide()) {
                BlockPos selected = getSelected(stack);
                if (selected == null || !(level.getBlockEntity(selected) instanceof ReflectionPanelBlockEntity panel)) {
                    actionBarMessage(player, "message.sunworks.wrench.failed", ChatFormatting.RED);
                } else {
                    Vec3 panelCenter = selected.getCenter();
                    Vec3 targetCenter = clicked.getCenter();
                    Vec3 dir = targetCenter.subtract(panelCenter).normalize();
                    if (dir.y >= MIN_COS
                            && selected.distSqr(clicked) <= (long) MAX_LINK_DISTANCE * MAX_LINK_DISTANCE) {
                        panel.setTarget(clicked);
                        if (level instanceof ServerLevel serverLevel) {
                            spawnConnectionParticles(serverLevel, selected, clicked);
                        }
                        actionBarMessage(player, "message.sunworks.wrench.formed", ChatFormatting.GREEN);
                    } else {
                        actionBarMessage(player, "message.sunworks.wrench.failed", ChatFormatting.RED);
                    }
                    clearSelected(stack);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (!level.isClientSide() && getSelected(stack) != null) {
            clearSelected(stack);
            actionBarMessage(player, "message.sunworks.wrench.discarded", ChatFormatting.YELLOW);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static void actionBarMessage(Player player, String key, ChatFormatting color) {
        player.displayClientMessage(Component.translatable(key).withStyle(color), true);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!(entity instanceof Player player)) {
            return;
        }

        if (level.isClientSide()) {
            if (isHolding(stack, player)) {
                spawnOutlineParticles(level, stack, player);
            }
        } else {
            if (!isHolding(stack, player)) {
                clearSelected(stack);
            }
        }
    }

    private boolean isHolding(ItemStack stack, Player player) {
        return player.getMainHandItem() == stack || player.getOffhandItem() == stack;
    }

    private void setSelected(ItemStack stack, BlockPos pos) {
        CompoundTag tag = getCustomTag(stack);
        tag.putLong(TAG_SELECTED, pos.asLong());
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
    }

    private BlockPos getSelected(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return null;
        }
        CompoundTag tag = data.copyTag();
        if (tag.contains(TAG_SELECTED, CompoundTag.TAG_LONG)) {
            return BlockPos.of(tag.getLong(TAG_SELECTED));
        }
        return null;
    }

    private void clearSelected(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return;
        }
        CompoundTag tag = data.copyTag();
        tag.remove(TAG_SELECTED);
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        }
    }

    private CompoundTag getCustomTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
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

    private void spawnOutlineParticles(Level level, ItemStack stack, Player player) {
        BlockPos selected = getSelected(stack);
        if (selected == null || level.getGameTime() % 5 != 0) {
            return;
        }

        for (int i = 0; i < 8; i++) {
            double x = selected.getX() + ((i & 1) == 0 ? 0.1 : 0.9);
            double y = selected.getY() + ((i & 2) == 0 ? 0.1 : 0.9);
            double z = selected.getZ() + ((i & 4) == 0 ? 0.1 : 0.9);
            level.addParticle(ORANGE_DUST, x, y, z, 0, 0, 0);
        }
    }
}
