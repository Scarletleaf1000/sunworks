package me.scarletleaf1000.sunworks.client.overlay;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

public class OverlayBar {
    private final Component title;
    private final int currentValue;
    private final int maxValue;
    private final TextColor filledColor;
    private final TextColor backgroundColor;
    private final int length;
    private final boolean usePercentage;

    private final int DEFAULT_LENGTH = 20;

    public OverlayBar(Component title, float progress) {
        this.title = title;
        this.maxValue = 1000;
        this.currentValue = Math.round(progress * 1000);
        this.filledColor = TextColor.fromLegacyFormat(ChatFormatting.DARK_GREEN);
        this.backgroundColor = TextColor.fromLegacyFormat(ChatFormatting.DARK_RED);
        this.length = DEFAULT_LENGTH;
        this.usePercentage = true;
    }

    public OverlayBar(Component title, int currentValue, int maxValue, boolean usePercentage) {
        this.title = title;
        this.currentValue = currentValue;
        this.maxValue = maxValue;
        this.filledColor = TextColor.fromLegacyFormat(ChatFormatting.DARK_GREEN);
        this.backgroundColor = TextColor.fromLegacyFormat(ChatFormatting.DARK_RED);
        this.length = DEFAULT_LENGTH;
        this.usePercentage = usePercentage;
    }

    public OverlayBar(Component title, int currentValue, int maxValue, TextColor filledColor, TextColor backgroundColor, boolean usePercentage) {
        this.title = title;
        this.currentValue = currentValue;
        this.maxValue = maxValue;
        this.filledColor = filledColor;
        this.backgroundColor = backgroundColor;
        this.length = DEFAULT_LENGTH;
        this.usePercentage = usePercentage;
    }

    public OverlayBar(Component title,  int currentValue, int maxValue, TextColor filledColor, TextColor backgroundColor, int length, boolean usePercentage) {
        this.title = title;
        this.currentValue = currentValue;
        this.maxValue = maxValue;
        this.filledColor = filledColor;
        this.backgroundColor = backgroundColor;
        this.length = length;
        this.usePercentage = usePercentage;
    }

    public Component getTitle() {
        return title;
    }

    public float getProgress() {
        return((float)currentValue)/((float)maxValue);
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public int getMaxValue() {
        return maxValue;
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

    public boolean isPercentage() {
        return usePercentage;
    }

}
