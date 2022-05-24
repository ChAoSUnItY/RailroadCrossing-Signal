package chaos.unity.railroad_crossing.signal.client.render;

import chaos.unity.railroad_crossing.signal.common.block.entity.ISingleHeadSignal;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;

@Environment(EnvType.CLIENT)
public class SingleHeadSignalBlockEntityRenderer<T extends BlockEntity & ISingleHeadSignal> extends SignalBlockEntityRenderer implements BlockEntityRenderer<T> {
    private static final float[][][] PRECALCULATED_VERTEXES = {
            {{0.625f, 0.375f, 0.755F}, {0.625f, 0.625f, .755F}, {0.375f, 0.625f, 0.755F}, {0.375f, 0.375f, 0.755F}}, // SOUTH
            {{0.245f, 0.625f, 0.375f}, {0.245f, 0.625f, 0.625f}, {0.245f, 0.375f, 0.625f}, {0.245f, 0.375f, 0.375f}}, // WEST
            {{0.625f, 0.375f, 0.245F}, {0.625f, 0.625f, 0.245F}, {0.375f, 0.625f, 0.245F}, {0.375f, 0.375f, 0.245F}}, // NORTH
            {{0.755F, 0.625f, 0.375f}, {0.755F, 0.625f, 0.625f}, {0.755F, 0.375f, 0.625f}, {0.755F, 0.375f, 0.375f}}, // EAST
    };

    public SingleHeadSignalBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var signal = entity.getSignal();

        if (signal == null)
            return;

        if (signal.isBlink()) {
            // Renders solid color block for 10 ticks and renders nothing for the rest 10 ticks
            if ((entity.getWorld().getTime() + tickDelta) % 20 >= 10) return;
        }

        matrices.push();
        matrices.translate(0, 0, 0);

        renderSignalLight(
                vertexConsumers.getBuffer(SignalLightRenderLayer.SIGNAL_LIGHT),
                matrices.peek().getPositionMatrix(),
                entity.getCachedState().get(Properties.HORIZONTAL_FACING),
                entity.getSignal().color
        );

        matrices.pop();
    }

    @Override
    public float[][][] getPrecalculatedVertexes() {
        return PRECALCULATED_VERTEXES;
    }
}
