package chaos.unity.signal;

import chaos.unity.signal.client.particle.SignalParticles;
import chaos.unity.signal.common.data.Interval;
import chaos.unity.signal.common.world.IntervalData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public final class SignalNetworking {
    // CLIENT 2 SERVER
    public static final Identifier REQUEST_ADD_INTERVAL = new Identifier("signal", "request_add_interval");
    public static final Identifier REQUEST_HIGHLIGHT_INTERVAL_INSTANCE = new Identifier("signal", "request_highlight_interval_instance");
    // SERVER 2 CLIENT
    public static final Identifier CALLBACK_ADD_RESULT = new Identifier("signal", "callback_add_result");
    public static final Identifier HIGHLIGHT_INTERVAL_INSTANCE = new Identifier("signal", "highlight_interval_instance");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(REQUEST_HIGHLIGHT_INTERVAL_INSTANCE, SignalNetworking::requestHighlightIntervalInstance);
        ServerPlayNetworking.registerGlobalReceiver(REQUEST_ADD_INTERVAL, SignalNetworking::requestAddInterval);

        ClientPlayNetworking.registerGlobalReceiver(HIGHLIGHT_INTERVAL_INSTANCE, SignalNetworking::highlightIntervalInstance);
        ClientPlayNetworking.registerGlobalReceiver(CALLBACK_ADD_RESULT, SignalNetworking::callbackAddResult);
    }

    private static void requestHighlightIntervalInstance(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        var signalPos = buf.readBlockPos();
        var interval = IntervalData.getOrCreate(player.getWorld()).getBySignal(signalPos);

        if (interval != null) {
            var responseBuf = PacketByteBufs.create();

            responseBuf.writeBlockPos(interval.signalPosA())
                    .writeBlockPos(interval.signalPosB())
                    .writeInt(interval.intervalPath().size());

            for (var pos : interval.intervalPath()) {
                responseBuf.writeBlockPos(pos);
            }

            responseSender.sendPacket(HIGHLIGHT_INTERVAL_INSTANCE, responseBuf);
        }
    }

    private static void requestAddInterval(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        var interval = readInterval(buf);
        var intervalData = IntervalData.getOrCreate(player.getWorld());
        var result = intervalData.addInterval(interval);

        if (result)
            intervalData.markDirty();

        var responseBuf = PacketByteBufs.create();
        responseBuf.writeBoolean(result);
        responseSender.sendPacket(CALLBACK_ADD_RESULT, responseBuf);
    }

    private static void highlightIntervalInstance(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        var interval = readInterval(buf);
        BlockPos p1 = interval.signalPosA(), p2 = interval.signalPosB();

        if (client.world != null) {
            client.execute(() -> {
                client.world.addParticle(SignalParticles.CYAN_DUST, p1.getX() + .5, p1.getY() + .8, p1.getZ() + .5, 0, 0, 0);
                client.world.addParticle(SignalParticles.CYAN_DUST, p2.getX() + .5, p2.getY() + .8, p2.getZ() + .5, 0, 0, 0);

                for (var pos : interval.intervalPath()) {
                    client.world.addParticle(SignalParticles.GREEN_DUST, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, 0, 0, 0);
                }
            });
        }
    }

    private static void callbackAddResult(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        if (client.player == null)
            return;

        if (buf.readBoolean()) {
            client.player.sendMessage(new LiteralText("Successfully bind two signals as paired signal").formatted(Formatting.GREEN), false);
        } else {
            client.player.sendMessage(new LiteralText("Failed to bind two signals as paired signal").formatted(Formatting.RED), false);
        }
    }

    private static Interval readInterval(PacketByteBuf buf) {
        var signalPosA = buf.readBlockPos();
        var signalPosB = buf.readBlockPos();
        var pathLength = buf.readInt();
        List<BlockPos> intervalPath = new ArrayList<>();
        for (int i = 0; i < pathLength; i++) {
            intervalPath.add(buf.readBlockPos());
        }

        return new Interval(signalPosA, signalPosB, intervalPath);
    }
}
