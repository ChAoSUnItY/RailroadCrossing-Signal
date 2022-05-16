package chaos.unity.signal.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.*;

public class BlockRenderHelper {
    private static final Vec3d[] MIDPOINTS = new Vec3d[]{
            new Vec3d(0.51, -0.01, 0.5),
            new Vec3d(0.5, 1.01, 0.5),
            new Vec3d(0.5, 0.5, -0.01),
            new Vec3d(0.5, 0.5, 1.01),
            new Vec3d(-0.01, 0.5, 0.5),
            new Vec3d(1.01, 0.5, 0.5)
    };

    public static void drawBlock(MinecraftClient client, MatrixStack matrixStack, VertexConsumer consumer) {
        var matrixPos = matrixStack.peek().getPositionMatrix();
        var matrixNormal = matrixStack.peek().getNormalMatrix();

        matrixStack.push();
        matrixStack.translate(0, 0, 0);

        for (var dir : Direction.values()) {
            addFace(
                    dir,
                    matrixPos,
                    matrixNormal,
                    consumer,
                    MIDPOINTS[dir.getId()]
            );
        }

        matrixStack.pop();
    }

    private static void addFace(Direction whichFace,
                                Matrix4f matrixPos,
                                Matrix3f matrixNormal,
                                VertexConsumer renderBuffer,
                                Vec3d centrePos) {
        Vec3f leftToRightDirection, bottomToTopDirection;

        switch (whichFace) {
            case NORTH -> {
                leftToRightDirection = new Vec3f(-1, 0, 0);
                bottomToTopDirection = new Vec3f(0, 1, 0);
            }
            case SOUTH -> {
                leftToRightDirection = new Vec3f(1, 0, 0);
                bottomToTopDirection = new Vec3f(0, 1, 0);
            }
            case EAST -> {
                leftToRightDirection = new Vec3f(0, 0, -1);
                bottomToTopDirection = new Vec3f(0, 1, 0);
            }
            case UP -> {
                leftToRightDirection = new Vec3f(-1, 0, 0);
                bottomToTopDirection = new Vec3f(0, 0, 1);
            }
            case DOWN -> {
                leftToRightDirection = new Vec3f(1, 0, 0);
                bottomToTopDirection = new Vec3f(0, 0, 1);
            }
            default -> {
                leftToRightDirection = new Vec3f(0, 0, 1);
                bottomToTopDirection = new Vec3f(0, 1, 0);
            }
        }
        leftToRightDirection.scale(0.5F);
        bottomToTopDirection.scale(0.5F);

        var bottomLeftPos = new Vec3f(centrePos);
        bottomLeftPos.subtract(leftToRightDirection);
        bottomLeftPos.subtract(bottomToTopDirection);

        var bottomRightPos = new Vec3f(centrePos);
        bottomRightPos.add(leftToRightDirection);
        bottomRightPos.subtract(bottomToTopDirection);

        var topRightPos = new Vec3f(centrePos);
        topRightPos.add(leftToRightDirection);
        topRightPos.add(bottomToTopDirection);

        var topLeftPos = new Vec3f(centrePos);
        topLeftPos.subtract(leftToRightDirection);
        topLeftPos.add(bottomToTopDirection);

        Vec3f normalVector = whichFace.getUnitVector();

        addQuad(
                matrixPos,
                matrixNormal,
                renderBuffer,
                bottomLeftPos,
                bottomRightPos,
                topRightPos,
                topLeftPos,
                normalVector
        );
    }

    private static void addQuad(Matrix4f matrixPos, Matrix3f matrixNormal, VertexConsumer renderBuffer,
                                Vec3f blpos, Vec3f brpos, Vec3f trpos, Vec3f tlpos,
                                Vec3f normalVector) {
        addQuadVertex(matrixPos, matrixNormal, renderBuffer, blpos, normalVector);
        addQuadVertex(matrixPos, matrixNormal, renderBuffer, brpos, normalVector);
        addQuadVertex(matrixPos, matrixNormal, renderBuffer, trpos, normalVector);
        addQuadVertex(matrixPos, matrixNormal, renderBuffer, tlpos, normalVector);
    }

    private static void addQuadVertex(Matrix4f matrixPos, Matrix3f matrixNormal, VertexConsumer renderBuffer,
                                      Vec3f pos,
                                      Vec3f normalVector) {
        renderBuffer.vertex(matrixPos, pos.getX(), pos.getY(), pos.getZ())
                .overlay(OverlayTexture.DEFAULT_UV)
                .normal(matrixNormal, normalVector.getX(), normalVector.getY(), normalVector.getZ())
                .next();
    }
}
