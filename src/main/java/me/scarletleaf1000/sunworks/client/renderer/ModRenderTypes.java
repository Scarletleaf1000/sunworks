package me.scarletleaf1000.sunworks.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

/**
 * Custom render types for Sunworks. Extends {@link RenderType} so
 * {@link RenderType#create} is accessible regardless of its visibility.
 */
public class ModRenderTypes extends RenderType {

    private ModRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                           boolean affectsCrumbling, boolean sortOnUpload,
                           Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    /**
     * Same state as {@link RenderType#lightning()} plus {@code VIEW_OFFSET_Z_LAYERING},
     * which biases beam geometry slightly toward the camera in depth so beam caps
     * coplanar with block faces don't z-fight.
     */
    public static final RenderType BEAM = create(
            "sunworks:beam",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            true,
            CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LIGHTNING_SHADER)
                    .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setOutputState(RenderStateShard.WEATHER_TARGET)
                    .createCompositeState(false)
    );
}
