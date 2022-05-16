package chaos.unity.signal.client.particle;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class SignalParticles {
    public static final DefaultParticleType YELLOW_GLINT = FabricParticleTypes.simple();
    public static final DefaultParticleType GREEN_GLINT = FabricParticleTypes.simple();
    public static final DefaultParticleType RED_GLINT = FabricParticleTypes.simple();

    public static void register() {
        Registry.register(Registry.PARTICLE_TYPE, new Identifier("signal", "yellow_glint"), YELLOW_GLINT);
        Registry.register(Registry.PARTICLE_TYPE, new Identifier("signal", "green_glint"), GREEN_GLINT);
        Registry.register(Registry.PARTICLE_TYPE, new Identifier("signal", "red_glint"), RED_GLINT);
    }
}
