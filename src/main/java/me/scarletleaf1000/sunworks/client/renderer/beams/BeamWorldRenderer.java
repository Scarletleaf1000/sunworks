package me.scarletleaf1000.sunworks.client.renderer.beams;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.beams.AbstractBeam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.List;

@EventBusSubscriber(modid = Sunworks.MOD_ID, value = Dist.CLIENT)
public class BeamWorldRenderer {

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        // Render during translucent pass for proper transparency/blending
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
            return;
        }

        List<AbstractBeam> beams = ClientBeamManager.getBeamsForCurrentLevel();
        if (beams.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        Vec3 cameraPos = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // Use lightning or translucent render type for glowing effect
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());

        poseStack.pushPose();
        // Shift matrix origin from absolute world (0,0,0) to current Camera Position
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        float partialTick = event.getPartialTick().getGameTimeDeltaTicks();
        for (AbstractBeam beam : beams) {
            beam.render(poseStack, consumer, partialTick);
        }

        poseStack.popPose();
        bufferSource.endBatch(RenderType.lightning());
    }
}
