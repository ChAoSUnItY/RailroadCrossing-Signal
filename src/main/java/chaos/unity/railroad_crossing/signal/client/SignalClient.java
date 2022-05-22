package chaos.unity.railroad_crossing.signal.client;

import chaos.unity.railroad_crossing.signal.SignalNetworking;
import chaos.unity.railroad_crossing.signal.client.render.SignalBoxBlockEntityRenderer;
import chaos.unity.railroad_crossing.signal.client.render.SingleHeadSignalBlockEntityRenderer;
import chaos.unity.railroad_crossing.signal.common.block.entity.SignalBlockEntities;
import chaos.unity.railroad_crossing.signal.common.block.entity.SingleHeadSignalBlockEntity;
import chaos.unity.railroad_crossing.signal.common.item.SignalSurveyorItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class SignalClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        registerBlockEntityRenderer();
        registerClientEvent();
    }

    private void registerBlockEntityRenderer() {
        BlockEntityRendererRegistry.register(SignalBlockEntities.SINGLE_HEAD_SIGNAL_BLOCK_ENTITY, SingleHeadSignalBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(SignalBlockEntities.SIGNAL_BOX_RECEIVER_BLOCK_ENTITY, SignalBoxBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(SignalBlockEntities.DISTANT_SINGLE_HEAD_SIGNAL_BLOCK_ENTITY, SingleHeadSignalBlockEntityRenderer::new);
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

                if (world.getBlockEntity(targetPos) instanceof SingleHeadSignalBlockEntity sbe && player.getStackInHand(player.getActiveHand()).getItem() instanceof SignalSurveyorItem) {
                    var buf = PacketByteBufs.create();
                    buf.writeBlockPos(sbe.getPos());
                    ClientPlayNetworking.send(SignalNetworking.REQUEST_HIGHLIGHT_SIGNALS, buf);
                }
            }
        });
    }
}
