package chaos.unity.railroad_crossing.signal;

import chaos.unity.railroad_crossing.signal.client.particle.SignalParticles;
import chaos.unity.railroad_crossing.signal.common.block.entity.SingleHeadSignalBlockEntity;
import chaos.unity.railroad_crossing.signal.common.data.Interval;
import chaos.unity.railroad_crossing.signal.common.world.IntervalData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public final class SignalNetworking {
    // UNIVERSAL (S2C/C2S)
    public static final Identifier SYNC_BLOCK_ENTITY = new Identifier("rc_signal", "sync_block_entity");

    // CLIENT 2 SERVER
    public static final Identifier REQUEST_ADD_INTERVAL = new Identifier("rc_signal", "request_add_interval");
    public static final Identifier REQUEST_HIGHLIGHT_SIGNALS = new Identifier("rc_signal", "request_highlight_signals");

    // SERVER 2 CLIENT
    public static final Identifier CALLBACK_ADD_RESULT = new Identifier("rc_signal", "callback_add_result");
    public static final Identifier HIGHLIGHT_SIGNAL_NO_BOUND = new Identifier("rc_signal", "highlight_signal_no_bound");
    public static final Identifier HIGHLIGHT_SIGNAL = new Identifier("rc_signal", "highlight_signal");
    public static final Identifier HIGHLIGHT_INTERVAL_INSTANCE = new Identifier("rc_signal", "highlight_interval_instance");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(SYNC_BLOCK_ENTITY, SignalNetworking::syncBlockEntity);
        ServerPlayNetworking.registerGlobalReceiver(REQUEST_HIGHLIGHT_SIGNALS, SignalNetworking::requestHighlightSignals);
        ServerPlayNetworking.registerGlobalReceiver(REQUEST_ADD_INTERVAL, SignalNetworking::requestAddInterval);

        ClientPlayNetworking.registerGlobalReceiver(SYNC_BLOCK_ENTITY, SignalNetworking::syncBlockEntity);
        ClientPlayNetworking.registerGlobalReceiver(HIGHLIGHT_SIGNAL_NO_BOUND, SignalNetworking::highlightSignalNoBound);
        ClientPlayNetworking.registerGlobalReceiver(HIGHLIGHT_SIGNAL, SignalNetworking::highlightSignal);
        ClientPlayNetworking.registerGlobalReceiver(HIGHLIGHT_INTERVAL_INSTANCE, SignalNetworking::highlightIntervalInstance);
        ClientPlayNetworking.registerGlobalReceiver(CALLBACK_ADD_RESULT, SignalNetworking::callbackAddResult);
    }

    private static void syncBlockEntity(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        var pos = buf.readBlockPos();
        var rawId = buf.readVarInt();
        var nbt = buf.readNbt();
        ServerWorld world;

        if ((world = player.getWorld()) != null) {
            server.execute(() -> {
                BlockEntity blockEntity;

                if ((blockEntity = world.getBlockEntity(pos)) != null && blockEntity.getType() == Registry.BLOCK_ENTITY_TYPE.get(rawId)) {
                    blockEntity.readNbt(nbt);
                    blockEntity.markDirty();
                    world.updateNeighbors(pos, world.getBlockState(pos).getBlock());
                }
            });
        }
    }

    private static void requestHighlightSignals(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        var signalPos = buf.readBlockPos();
        var world = player.getEntityWorld();

        server.execute(() -> {
            if (world.getBlockEntity(signalPos) instanceof SingleHeadSignalBlockEntity sbe) {
                if (sbe.pairedSignalPos != null) {
                    var interval = IntervalData.getOrCreate(player.getWorld()).getBySignal(signalPos);

                    if (interval != null) {
                        var responseBuf = PacketByteBufs.create();

                        writeInterval(responseBuf, interval);

                        responseSender.sendPacket(HIGHLIGHT_INTERVAL_INSTANCE, responseBuf);
                    }
                } else if (sbe.railBindPos != null && world.getBlockState(sbe.railBindPos).getBlock() instanceof AbstractRailBlock) {
                    var responseBuf = PacketByteBufs.create();

                    responseBuf.writeBlockPos(signalPos)
                            .writeBlockPos(sbe.railBindPos);

                    responseSender.sendPacket(HIGHLIGHT_SIGNAL, responseBuf);
                } else {
                    var responseBuf = PacketByteBufs.create();

                    responseBuf.writeBlockPos(signalPos);

                    responseSender.sendPacket(HIGHLIGHT_SIGNAL_NO_BOUND, responseBuf);
                }
            }
        });
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

    private static void highlightSignalNoBound(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        var signalPos = buf.readBlockPos();

        if (client.world != null) {
            client.execute(() -> client.world.addParticle(SignalParticles.RED_DUST, signalPos.getX() + .5, signalPos.getY() + .8, signalPos.getZ() + .5, 0, 0, 0));
        }
    }

    private static void highlightSignal(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        var signalPos = buf.readBlockPos();
        var railPos = buf.readBlockPos();

        if (client.world != null) {
            client.execute(() -> {
                client.world.addParticle(SignalParticles.CYAN_DUST, signalPos.getX() + .5, signalPos.getY() + .8, signalPos.getZ() + .5, 0, 0, 0);
                client.world.addParticle(SignalParticles.GREEN_DUST, railPos.getX() + .5, railPos.getY() + .5, railPos.getZ() + .5, 0, 0, 0);
            });
        }
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

    private static void syncBlockEntity(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        var pos = buf.readBlockPos();
        var rawId = buf.readVarInt();
        var nbt = buf.readNbt();

        if (client.world != null) {
            client.execute(() -> {
                BlockEntity blockEntity;

                if ((blockEntity = client.world.getBlockEntity(pos)) != null && blockEntity.getType() == Registry.BLOCK_ENTITY_TYPE.get(rawId)) {
                    blockEntity.readNbt(nbt);
                    blockEntity.markDirty();
                }
            });
        }
    }

    public static void writeInterval(PacketByteBuf buf, Interval interval) {
        buf.writeBlockPos(interval.signalPosA())
                .writeBlockPos(interval.signalPosB())
                .writeInt(interval.intervalPath().size());

        for (var pos : interval.intervalPath())
            buf.writeBlockPos(pos);

    }

    public static Interval readInterval(PacketByteBuf buf) {
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
