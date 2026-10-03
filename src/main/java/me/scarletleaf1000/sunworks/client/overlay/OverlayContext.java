package me.scarletleaf1000.sunworks.client.overlay;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class OverlayContext {
    private Component title;
    private ItemStack icon;
    private List<Component> description;
    private List<OverlayBar> bars;

    public OverlayContext(Component title, ItemStack icon, List<Component> description) {
        this.title = title;
        this.icon = icon;
        this.description = description;
        this.bars = List.of();
    }

    public OverlayContext(Component title, ItemStack icon, List<Component> description, List<OverlayBar> bars) {
        this.title = title;
        this.icon = icon;
        this.description = description;
        this.bars = bars;
    }

    public void setTitle(Component title) {
        this.title = title;
    }

    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }

    public void setDescription(List<Component> description) {
        this.description = description;
    }

    public void addDescription (Component description) {
        this.description.add(description);
    }

    public void setBars(List<OverlayBar> bars) {
        this.bars = bars;
    }

    public void addBar(OverlayBar bar) {
        this.bars.add(bar);
    }

    public Component getTitle() {
        return title;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public List<Component> getDescription() {
        return description;
    }

    public List<OverlayBar> getBars() {
        return bars;
    }

}
