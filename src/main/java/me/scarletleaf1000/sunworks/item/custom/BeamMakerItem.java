package me.scarletleaf1000.sunworks.item.custom;

import me.scarletleaf1000.sunworks.beams.BeamUtil;
import me.scarletleaf1000.sunworks.beams.ServerBeamManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BeamMakerItem extends Item {

    private static final String TAG_SELECTED = "selected_block";

    public BeamMakerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos clicked = ctx.getClickedPos();
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();

        if (player == null) return InteractionResult.PASS;

        // --- 1. Shift + Right-Click: Clear all beams ---
        if (player.isSecondaryUseActive()) {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                ServerBeamManager.clearAllBeams(serverLevel);
                player.displayClientMessage(Component.literal("Cleared all beams."), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        BlockPos selected = getSelected(stack);

        // --- 2. Step 1: Save start position ---
        if (selected == null) {
            if (!level.isClientSide()) {
                setSelected(stack, clicked);
                player.displayClientMessage(
                        Component.literal("Selected start position at " + clicked.toShortString()), true
                );
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        // --- 3. Step 2a: Deselect if clicking same block ---
        if (selected.equals(clicked)) {
            if (!level.isClientSide()) {
                clearSelected(stack);
                player.displayClientMessage(Component.literal("Cleared selection."), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        // --- 4. Step 2b: Create beam between selected and clicked ---
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Vec3 startVec = Vec3.atCenterOf(selected);
            Vec3 endVec = Vec3.atCenterOf(clicked);

            BeamUtil.createPersistentOrangeBeam(serverLevel, startVec, endVec);
            clearSelected(stack);

            player.displayClientMessage(
                    Component.literal("Created laser beam between " + selected.toShortString() + " and " + clicked.toShortString()), true
            );
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
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
        if (data == null) return;

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
}
