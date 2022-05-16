package chaos.unity.signal.client;

import chaos.unity.signal.client.particle.SignalParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.particle.SpellParticle;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SignalClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register(((atlasTexture, registry) -> {
            registry.register(new Identifier("signal", "particle/yellow_glint"));
            registry.register(new Identifier("signal", "particle/green_glint"));
            registry.register(new Identifier("signal", "particle/red_glint"));
        }));

        ParticleFactoryRegistry.getInstance().register(SignalParticles.YELLOW_GLINT, SpellParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(SignalParticles.GREEN_GLINT, SpellParticle.DefaultFactory::new);
        ParticleFactoryRegistry.getInstance().register(SignalParticles.RED_GLINT, SpellParticle.DefaultFactory::new);
    }
}
