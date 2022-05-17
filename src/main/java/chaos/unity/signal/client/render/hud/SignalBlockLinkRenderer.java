package chaos.unity.signal.client.render.hud;

import chaos.unity.signal.SignalNetworking;
import chaos.unity.signal.client.particle.SignalParticles;
import chaos.unity.signal.common.blockentity.SignalBlockEntity;
import chaos.unity.signal.common.util.Utils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import java.awt.*;

public class SignalBlockLinkRenderer {
    public static void render(MinecraftClient client, ClientWorld world, SignalBlockEntity sbe, MatrixStack matrixStack, float tickDelta) {
        var pos = Utils.asIntArray(sbe.getPos());

        if (sbe.railBindPos == null) {
            // Generates yellow glint particle at the center of current signal block
            world.addParticle(new DustParticleEffect(new Vec3f(Vec3d.unpackRgb(Color.YELLOW.getRGB())), 1), pos[0] + 0.5, pos[1] + 0.5, pos[2] + 0.5, 0, 0, 0);
        } else if (sbe.pairedSignalPos != null) {
            // Generates green glint particles at the center of current signal block and its paired companion signal block
            var buf = PacketByteBufs.create();

            buf.writeBlockPos(sbe.getPos());

            ClientPlayNetworking.send(SignalNetworking.REQUEST_HIGHLIGHT_INTERVAL_INSTANCE, buf);
        } else {
            // Generates green glint particles at the center of signal block and its bound rail block
            var railBindPos = Utils.asIntArray(sbe.railBindPos);

            world.addParticle(new DustParticleEffect(new Vec3f(Vec3d.unpackRgb(Color.GREEN.getRGB())), 1), pos[0] + 0.5, pos[1] + 0.5, pos[2] + 0.5, 0, 0, 0);
            world.addParticle(new DustParticleEffect(new Vec3f(Vec3d.unpackRgb(Color.GREEN.getRGB())), 1), railBindPos[0] + 0.5, railBindPos[1] + 0.5, railBindPos[2] + 0.5, 0, 0, 0);
        }
    }
}
