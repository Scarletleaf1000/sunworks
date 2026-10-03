package me.scarletleaf1000.sunworks.client.overlay;

import me.scarletleaf1000.sunworks.block.entity.api.IOverlayInfoProvider;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
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
        for (OverlayBar b : bars) contentW = Math.max(contentW, b.getLength() * 6);
        int h = PAD * 2 + 10 + lines.size() * 10 + bars.size() * 12;

        int x = g.guiWidth() / 2 - contentW / 2 - PAD;
        int y = g.guiHeight() / 2 + 12;
        g.fill(x, y, x + contentW + PAD * 2, y + h, BG);

        int ty = y + PAD;
        g.renderItem(ctx.getIcon(), x + PAD, ty - 2);
        g.drawString(font, ctx.getTitle(), x + PAD + 20, ty, 0xFFFFFF, true);
        ty += 12;
        for (Component c : lines) { g.drawString(font, c, x + PAD, ty, 0xAAAAAA, false); ty += 10; }
        for (OverlayBar b : bars) {
            int bw = b.getLength() * 6;
            g.fill(x + PAD, ty, x + PAD + bw, ty + 6, b.getBackgroundColor().getValue() | 0xFF000000);
            g.fill(x + PAD, ty, x + PAD + (int) (bw * b.getProgress()), ty + 6,
                    b.getFilledColor().getValue() | 0xFF000000);
            ty += 12;
        }
    }
}
