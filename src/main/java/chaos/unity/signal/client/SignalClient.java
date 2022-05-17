package chaos.unity.signal.client;

import chaos.unity.signal.SignalNetworking;
import chaos.unity.signal.client.particle.SignalParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.particle.AbstractDustParticle;
import net.minecraft.client.particle.RedDustParticle;
import net.minecraft.client.particle.SpellParticle;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SignalClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
    }
}
