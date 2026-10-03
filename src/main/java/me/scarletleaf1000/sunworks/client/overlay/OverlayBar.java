package me.scarletleaf1000.sunworks.client.overlay;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

public class OverlayBar {
    private final Component title;
    private final float progress;
    private final TextColor filledColor;
    private final TextColor backgroundColor;
    private final int length;

    public OverlayBar(Component title, float progress) {
        this.title = title;
        this.progress = progress;
        this.filledColor = TextColor.fromLegacyFormat(ChatFormatting.DARK_GREEN);
        this.backgroundColor = TextColor.fromLegacyFormat(ChatFormatting.DARK_RED);
        this.length = 10;
    }

    public OverlayBar(Component title, float progress, TextColor filledColor, TextColor backgroundColor) {
        this.title = title;
        this.progress = progress;
        this.filledColor = filledColor;
        this.backgroundColor = backgroundColor;
        this.length = 10;
    }

    public OverlayBar(Component title, float progress, TextColor filledColor, TextColor backgroundColor, int length) {
        this.title = title;
        this.progress = progress;
        this.filledColor = filledColor;
        this.backgroundColor = backgroundColor;
        this.length = length;
    }

    public Component getTitle() {
        return title;
    }

    public float getProgress() {
        return progress;
    }

    public TextColor getFilledColor() {
        return filledColor;
    }

    public TextColor getBackgroundColor() {
        return backgroundColor;
    }

    public int getLength() {
        return length;
    }
}
