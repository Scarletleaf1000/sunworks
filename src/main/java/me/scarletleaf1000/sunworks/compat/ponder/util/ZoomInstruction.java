package me.scarletleaf1000.sunworks.compat.ponder.util;

import me.scarletleaf1000.sunworks.mixin.PonderSceneAccessor;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.instruction.PonderInstruction;

public class ZoomInstruction extends PonderInstruction {
    private final float targetScale;
    private final int ticks;
    private float startScale;
    private int elapsed;

    public ZoomInstruction(float targetScale, int ticks) {
        this.targetScale = targetScale;
        this.ticks = Math.max(1, ticks);
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    @Override
    public void onScheduled(PonderScene scene) {
        startScale = ((PonderSceneAccessor) scene).pnp$getScaleFactor();
        elapsed = 0;
    }

    @Override
    public void reset(PonderScene scene) {
        elapsed = 0;
    }

    @Override
    public void tick(PonderScene scene) {
        elapsed++;
        float t = Math.min(1f, (float) elapsed / ticks);
        float eased = t * t * (3f - 2f * t); // smoothstep
        ((PonderSceneAccessor) scene).pnp$setScaleFactor(startScale + (targetScale - startScale) * eased);
    }

    @Override
    public boolean isComplete() {
        return elapsed >= ticks;
    }
}
