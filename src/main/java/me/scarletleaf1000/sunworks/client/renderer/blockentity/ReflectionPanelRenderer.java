package me.scarletleaf1000.sunworks.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.block.entity.custom.generator.ReflectionPanelBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ReflectionPanelRenderer implements BlockEntityRenderer<ReflectionPanelBlockEntity> {
    private static final float PIVOT_X = 0.5f;
    private static final float PIVOT_Y = 0.5f;
    private static final float PIVOT_Z = 0.5f;
    private static final double MIN_COS = Math.cos(Math.toRadians(80));

    private final BakedModel panelModel;

    public ReflectionPanelRenderer(BlockEntityRendererProvider.Context ctx) {
        ModelResourceLocation location = ModelResourceLocation.standalone(
                ResourceLocation.fromNamespaceAndPath(Sunworks.MOD_ID, "block/reflection_panel_panel"));
        this.panelModel = Minecraft.getInstance().getModelManager().getModel(location);
    }

    @Override
    public void render(ReflectionPanelBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockPos target = be.getTarget();
        if (target == null) {
            return;
        }

        Vec3 panelCenter = be.getBlockPos().getCenter();
        Vec3 targetCenter = target.getCenter();
        Vec3 dir = targetCenter.subtract(panelCenter).normalize();

        if (dir.y < MIN_COS) {
            return;
        }

        Vector3f normal = new Vector3f(0, 1, 0).add(new Vector3f((float) dir.x, (float) dir.y, (float) dir.z)).normalize();
        if (!normal.isFinite() || normal.lengthSquared() < 1e-5f) {
            return;
        }

        pose.pushPose();
        pose.translate(PIVOT_X, PIVOT_Y, PIVOT_Z);
        pose.mulPose(new Quaternionf().rotationTo(new Vector3f(0, 1, 0), normal));
        pose.translate(-PIVOT_X, -PIVOT_Y, -PIVOT_Z);

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        ModelBlockRenderer modelRenderer = dispatcher.getModelRenderer();
        modelRenderer.renderModel(pose.last(), bufferSource.getBuffer(RenderType.cutout()),
                be.getBlockState(), panelModel, 1f, 1f, 1f, packedLight, packedOverlay);

        pose.popPose();
    }
}
