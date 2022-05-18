package chaos.unity.signal.client;

import chaos.unity.signal.SignalNetworking;
import chaos.unity.signal.client.particle.SignalParticles;
import chaos.unity.signal.common.blockentity.SignalBlockEntity;
import chaos.unity.signal.common.item.RadioLinkerItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.AbstractDustParticle;
import net.minecraft.client.particle.RedDustParticle;
import net.minecraft.client.particle.SpellParticle;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.WorldEvents;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class SignalClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        registerClientEvent();
    }

    private void registerClientEvent() {
        ClientTickEvents.START_WORLD_TICK.register(world -> {
            var client = MinecraftClient.getInstance();
            var player = MinecraftClient.getInstance().player;

            if (player == null)
                return;

            var hitResult = player.raycast(Objects.requireNonNull(client.interactionManager).getReachDistance(), 0, false);

            if (hitResult instanceof BlockHitResult blockHitResult) {
                var targetPos = blockHitResult.getBlockPos();

                if (world.getBlockEntity(targetPos) instanceof SignalBlockEntity sbe && player.getStackInHand(player.getActiveHand()).getItem() instanceof RadioLinkerItem) {
                    var buf = PacketByteBufs.create();
                    buf.writeBlockPos(sbe.getPos());
                    ClientPlayNetworking.send(SignalNetworking.REQUEST_HIGHLIGHT_SIGNALS, buf);
                }
            }
        });
    }
}
