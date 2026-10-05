package me.scarletleaf1000.sunworks.client.overlay;

import me.scarletleaf1000.sunworks.block.entity.api.IOverlayInfoProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.*;

public class MachineOverlayRenderer implements LayeredDraw.Layer {

    private static final int BG = 0xC0101010;
    private static final int PAD = 4;

    @Override
    public void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.level == null || mc.player == null) return;
        if (!(mc.hitResult instanceof BlockHitResult hit) || hit.getType() == HitResult.Type.MISS) return;

        BlockPos pos = hit.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);

        IOverlayInfoProvider provider =
                state.getBlock() instanceof IOverlayInfoProvider p ? p :
                    mc.level.getBlockEntity(pos) instanceof IOverlayInfoProvider p ? p : null;
        if (provider == null) return;

        List<Component> lines = new ArrayList<>();
        List<OverlayBar> bars = new ArrayList<>();

        OverlayContext ctx = new OverlayContext(
                state.getBlock().getName(), new ItemStack(state.getBlock()), lines, bars);
        if (!provider.addOverlayInfo(ctx)) return;

        Font font = mc.font;
        int contentW = font.width(ctx.getTitle()) + 20;
        for (Component c : lines) contentW = Math.max(contentW, font.width(c));
        for (OverlayBar b : bars)
            contentW = Math.max(contentW, font.width("|".repeat(b.getLength()))
                + font.width(b.getTitle())
                + (b.isPercentage() ?
                    font.width("(" + b.getProgress() + "%)") :
                    font.width("(" + b.getCurrentValue() + "/" + b.getMaxValue() + ")") )
                + 8);
        int h = PAD * 2 + 10 + lines.size() * 10 + bars.size() * 10;

        int x = g.guiWidth() / 2 + 12;
        int y = g.guiHeight() / 2 + 12;
        TooltipRenderUtil.renderTooltipBackground(g, x + PAD, y + PAD, contentW, h - PAD * 2, 0);

        int ty = y + PAD;
        g.renderItem(ctx.getIcon(), x + PAD, ty - 2);
        g.drawString(font, ctx.getTitle(), x + PAD + 20, ty, 0xFFFFFF, true);
        ty += 12;
        for (Component c : lines) { g.drawString(font, c, x + PAD, ty, 0xAAAAAA, false); ty += 10; }
        for (OverlayBar b : bars) {
            int total = b.getLength();
            int filled = Math.min(total, Math.max(0, Math.round(total * b.getProgress())));
            g.drawString(font, b.getTitle(), x + PAD, ty, 0xAAAAAA, false);
            int bx = x + PAD + font.width(b.getTitle()) + 4;
            g.drawString(font, "|".repeat(total), bx, ty, b.getBackgroundColor().getValue(), false);
            g.drawString(font, "|".repeat(filled), bx, ty, b.getFilledColor().getValue(), false);
            Component barValue;
            if (b.isPercentage())
                barValue = Component.literal("(" + b.getProgress() + "%)");
            else
                barValue = Component.literal("(" + b.getCurrentValue() + "/" + b.getMaxValue() + ")");
            int cx = bx + font.width("|".repeat(b.getLength())) + 4;
            g.drawString(font, barValue, cx, ty, 0xAAAAAA, false);
            ty += 10;
        }
    }
}
