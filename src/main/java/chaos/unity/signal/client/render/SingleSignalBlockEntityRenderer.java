package chaos.unity.signal.client.render;

import chaos.unity.signal.common.block.entity.SingleHeadSignalBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;

import java.awt.*;

@Environment(EnvType.CLIENT)
public class SingleSignalBlockEntityRenderer implements BlockEntityRenderer<SingleHeadSignalBlockEntity> {
    private static final float[][][] PRECALCULATED_VERTEXES = {
            {{0.625f, 0.375f, 0.755F}, {0.625f, 0.625f, .755F}, {0.375f, 0.625f, 0.755F}, {0.375f, 0.375f, 0.755F}}, // SOUTH
            {{0.245f, 0.625f, 0.375f}, {0.245f, 0.625f, 0.625f}, {0.245f, 0.375f, 0.625f}, {0.245f, 0.375f, 0.375f}}, // WEST
            {{0.625f, 0.375f, 0.245F}, {0.625f, 0.625f, 0.245F}, {0.375f, 0.625f, 0.245F}, {0.375f, 0.375f, 0.245F}}, // NORTH
            {{0.755F, 0.625f, 0.375f}, {0.755F, 0.625f, 0.625f}, {0.755F, 0.375f, 0.625f}, {0.755F, 0.375f, 0.375f}}, // EAST
    };

    public SingleSignalBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(SingleHeadSignalBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.mode.isBlink()) {
            // Renders solid color block for 10 ticks and renders nothing for the rest 10 ticks
            if ((entity.getWorld().getTime() + tickDelta) % 20 >= 10) return;
        }

        matrices.push();
        matrices.translate(0, 0, 0);

        renderSignalLight(
                vertexConsumers.getBuffer(SignalLightRenderLayer.SIGNAL_LIGHT),
                matrices.peek().getPositionMatrix(),
                entity.getCachedState().get(Properties.HORIZONTAL_FACING),
                entity.mode.color
        );

        matrices.pop();
    }

    public void renderSignalLight(VertexConsumer consumer, Matrix4f matrixPos, Direction direction, Color color) {
        var vec = PRECALCULATED_VERTEXES[direction.getHorizontal()];

        for (var i = 0; i < 4; i++)
            consumer.vertex(matrixPos, vec[i][0], vec[i][1], vec[i][2])
                    .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                    .next();
    }
}
