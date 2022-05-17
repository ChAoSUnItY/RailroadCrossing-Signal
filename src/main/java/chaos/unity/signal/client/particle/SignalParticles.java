package chaos.unity.signal.client.particle;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import java.awt.*;

public final class SignalParticles {
    // DUST INSTANCES
    public static final ParticleEffect GREEN_DUST = new DustParticleEffect(fromColor(Color.GREEN.getRGB()), 1);
    public static final ParticleEffect YELLOW_DUST = new DustParticleEffect(fromColor(Color.YELLOW.getRGB()), 1);
    public static final ParticleEffect RED_DUST = new DustParticleEffect(fromColor(Color.RED.getRGB()), 1);
    public static final ParticleEffect CYAN_DUST = new DustParticleEffect(fromColor(Color.CYAN.getRGB()), 1);

    private static Vec3f fromColor(int rgb) {
        return new Vec3f(Vec3d.unpackRgb(rgb));
    }
}
