package chaos.unity.railroad_crossing.signal.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

public class SignalLightRenderLayer extends RenderLayer {
    public static final SignalLightRenderLayer SIGNAL_LIGHT = new SignalLightRenderLayer();

    private SignalLightRenderLayer() {
        super("signal_light",
                VertexFormats.POSITION_COLOR,
                VertexFormat.DrawMode.QUADS,
                256,
                false,
                false,
                () -> {
                    RenderSystem.setShader(GameRenderer::getPositionColorShader);
                    RenderSystem.disableCull();
                    RenderSystem.enableDepthTest();
                }, () -> {
                    RenderSystem.enableCull();
                    RenderSystem.disableDepthTest();
                });
    }
}
