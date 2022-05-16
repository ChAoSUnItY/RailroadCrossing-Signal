package chaos.unity.signal.client.render.hud;

import chaos.unity.signal.client.particle.SignalParticles;
import chaos.unity.signal.common.blockentity.SignalBlockEntity;
import chaos.unity.signal.common.util.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;

public class SignalBlockLinkRenderer {
    public static void render(MinecraftClient client, ClientWorld world, SignalBlockEntity sbe, MatrixStack matrixStack, float tickDelta) {
        var pos = Utils.asIntArray(sbe.getPos());

        if (sbe.railBindPos == null) {
            // Generates yellow glint particle at the center of current signal block
            world.addParticle(SignalParticles.YELLOW_GLINT, pos[0] + 0.5, pos[1] + 0.5, pos[2] + 0.5, 0, 0, 0);
        } else {
            // Generates green glint particles at the center of signal block and its bound rail block
            var railBindPos = Utils.asIntArray(sbe.railBindPos);

            world.addParticle(SignalParticles.GREEN_GLINT, pos[0] + 0.5, pos[1] + 0.5, pos[2] + 0.5, 0, 0, 0);
            world.addParticle(SignalParticles.GREEN_GLINT, railBindPos[0] + 0.5, railBindPos[1] + 0.5, railBindPos[2] + 0.5, 0, 0, 0);
        }
    }
}
