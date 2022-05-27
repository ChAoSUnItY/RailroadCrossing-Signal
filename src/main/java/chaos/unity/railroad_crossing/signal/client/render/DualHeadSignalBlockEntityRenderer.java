package chaos.unity.railroad_crossing.signal.client.render;

import chaos.unity.railroad_crossing.signal.common.block.entity.DualHeadSignalBlockEntity;
import chaos.unity.railroad_crossing.signal.common.data.SignalMode;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;

public class DualHeadSignalBlockEntityRenderer extends SignalBlockEntityRenderer implements BlockEntityRenderer<DualHeadSignalBlockEntity> {
    private static final float[][][] PRECALCULATED_VERTEXES_A = {
            {{0.625f, 0.1875f, 0.755F}, {0.625f, 0.4375f, .755F}, {0.375f, 0.4375f, 0.755F}, {0.375f, 0.1875f, 0.755F}}, // SOUTH
            {{0.245f, 0.1875f, 0.375f}, {0.245f, 0.4375f, 0.625f}, {0.245f, 0.4375f, 0.625f}, {0.245f, 0.1875f, 0.375f}}, // WEST
            {{0.625f, 0.1875f, 0.245F}, {0.625f, 0.4375f, 0.245F}, {0.375f, 0.4375f, 0.245F}, {0.375f, 0.1875f, 0.245F}}, // NORTH
            {{0.755F, 0.1875f, 0.375f}, {0.755F, 0.4375f, 0.625f}, {0.755F, 0.4375f, 0.625f}, {0.755F, 0.1875f, 0.375f}}, // EAST
    };
    private static final float[][][] PRECALCULATED_VERTEXES_B = {
            {{0.625f, 0.5625f, 0.755F}, {0.625f, 0.8125f, .755F}, {0.375f, 0.8125f, 0.755F}, {0.375f, 0.5625f, 0.755F}}, // SOUTH
            {{0.245f, 0.5625f, 0.375f}, {0.245f, 0.8125f, 0.625f}, {0.245f, 0.8125f, 0.625f}, {0.245f, 0.5625f, 0.375f}}, // WEST
            {{0.625f, 0.5625f, 0.245F}, {0.625f, 0.8125f, 0.245F}, {0.375f, 0.8125f, 0.245F}, {0.375f, 0.5625f, 0.245F}}, //NORTH
            {{0.755F, 0.5625f, 0.375f}, {0.755F, 0.8125f, 0.625f}, {0.755F, 0.8125f, 0.625f}, {0.755F, 0.5625f, 0.375f}} // EAST
    };

    public DualHeadSignalBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(DualHeadSignalBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0, 0, 0);

        SignalMode signal;

        for (int i = 0; i < 2; i++) {
            signal = entity.getSignal(i);

            if (signal == null)
                continue;

            if (signal.isBlink()) {
                // Renders solid color block for 10 ticks and renders nothing for the rest 10 ticks
                if ((entity.getWorld().getTime() + tickDelta) % 20 >= 10) continue;
            }

            renderSignalLight(
                    vertexConsumers.getBuffer(SignalLightRenderLayer.SIGNAL_LIGHT),
                    i,
                    matrices.peek().getPositionMatrix(),
                    entity.getCachedState().get(Properties.HORIZONTAL_FACING),
                    signal.color
            );
        }

        matrices.pop();
    }

    @Override
    public float[][][] getPrecalculatedVertexes(int index) {
        return switch (index) {
            case 0 -> PRECALCULATED_VERTEXES_A;
            case 1 -> PRECALCULATED_VERTEXES_B;
            default -> null;
        };
    }
}
