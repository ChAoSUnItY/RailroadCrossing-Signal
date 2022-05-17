package chaos.unity.signal;

import chaos.unity.signal.client.particle.SignalParticles;
import chaos.unity.signal.common.world.IntervalData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public final class SignalNetworking {
    // CLIENT 2 SERVER
    public static final Identifier REQUEST_HIGHLIGHT_INTERVAL_INSTANCE = new Identifier("signal", "request_highlight_interval_instance");
    // SERVER 2 CLIENT
    public static final Identifier HIGHLIGHT_INTERVAL_INSTANCE = new Identifier("signal", "highlight_interval_instance");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(REQUEST_HIGHLIGHT_INTERVAL_INSTANCE, (server, player, handler, buf, responseSender) -> {
            var signalPos = buf.readBlockPos();
            var interval = IntervalData.getOrCreate(player.getWorld()).getBySignal(signalPos);

            if (interval != null) {
                var responseBuf = PacketByteBufs.create();

                responseBuf.writeBlockPos(interval.signalAPos())
                        .writeBlockPos(interval.signalBPos())
                        .writeInt(interval.intervalPath().size());

                for (var pos : interval.intervalPath()) {
                    responseBuf.writeBlockPos(pos);
                }

                responseSender.sendPacket(HIGHLIGHT_INTERVAL_INSTANCE, responseBuf);
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(HIGHLIGHT_INTERVAL_INSTANCE, (client, handler, buf, responseSender) -> {
            var signalAPos = buf.readBlockPos();
            var signalBPos = buf.readBlockPos();
            var pathLength = buf.readInt();
            List<BlockPos> intervalPath = new ArrayList<>();
            for (int i = 0; i < pathLength; i++) {
                intervalPath.add(buf.readBlockPos());
            }

            if (client.world != null) {
                client.execute(() -> {
                    client.world.addParticle(SignalParticles.CYAN_DUST, signalAPos.getX() + .5, signalAPos.getY() + .8, signalAPos.getZ() + .5, 0, 0, 0);
                    client.world.addParticle(SignalParticles.CYAN_DUST, signalBPos.getX() + .5, signalBPos.getY() + .8, signalBPos.getZ() + .5, 0, 0, 0);

                    for (var pos: intervalPath) {
                        client.world.addParticle(SignalParticles.GREEN_DUST, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, 0, 0, 0);
                    }
                });
            }
        });
    }
}
