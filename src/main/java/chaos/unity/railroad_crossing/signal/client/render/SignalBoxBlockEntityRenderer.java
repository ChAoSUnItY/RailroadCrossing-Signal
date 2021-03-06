package chaos.unity.railroad_crossing.signal.client.render;

import chaos.unity.railroad_crossing.signal.common.block.entity.ISignalBox;
import chaos.unity.railroad_crossing.signal.common.block.entity.ISignalReceiver;
import chaos.unity.railroad_crossing.signal.common.data.SignalMode;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

public class SignalBoxBlockEntityRenderer<T extends BlockEntity & ISignalBox> extends SignalBlockEntityRenderer implements BlockEntityRenderer<T> {
    private static final float[][][] PRECALCULATED_VERTEXES = {
            {{0.39375f, 0.625f, 0.8755f}, {0.39375f, 0.675f, 0.8755f}, {0.44375f, 0.675f, 0.8755f}, {0.44375f, 0.625f, 0.8755f}}, // SOUTH
            {{0.1245f, 0.625f, 0.39375f}, {0.1245f, 0.675f, 0.39375f}, {0.1245f, 0.675f, 0.44375f}, {0.1245f, 0.625f, 0.44375f}}, // WEST
            {{0.60625f, 0.625f, 0.1245f}, {0.60625f, 0.675f, 0.1245f}, {0.55625f, 0.675f, 0.1245f}, {0.55625f, 0.625f, 0.1245f}}, // NORTH
            {{0.8755f, 0.625f, 0.60625f}, {0.8755f, 0.675f, 0.60625f}, {0.8755f, 0.675f, 0.55625f}, {0.8755f, 0.625f, 0.55625f}}, // EAST
    };

    public SignalBoxBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
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

        for (Direction dir : Direction.Type.HORIZONTAL) {
            renderSignalLight(
                    vertexConsumers.getBuffer(SignalLightRenderLayer.SIGNAL_LIGHT),
                    0,
                    matrices.peek().getPositionMatrix(),
                    dir,
                    signal.color
            );
        }

        matrices.pop();
    }

    @Override
    public float[][][] getPrecalculatedVertexes(int index) {
        return PRECALCULATED_VERTEXES;
    }
}
