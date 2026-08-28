package me.scarletleaf1000.sunworks.screen.custom;

import me.scarletleaf1000.sunworks.block.ModBlocks;
import me.scarletleaf1000.sunworks.block.entity.custom.generator.HelioreceiverBlockEntity;
import me.scarletleaf1000.sunworks.screen.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class HelioreceiverMenu extends AbstractContainerMenu {
    public final HelioreceiverBlockEntity blockEntity;
    private final Level level;

    public HelioreceiverMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public HelioreceiverMenu(int containerId, Inventory inv, BlockEntity blockEntity) {
        super(ModMenuTypes.HELIORECEIVER_MENU.get(), containerId);
        this.blockEntity = ((HelioreceiverBlockEntity) blockEntity);
        this.level = inv.player.level();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, ModBlocks.HELIORECEIVER.get());
    }
}
