package chaos.unity.signal.common.block.entity;

import chaos.unity.signal.SignalNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.server.world.ServerWorld;

public interface ISyncable {
    default void markDirtyAndSync() {
        var blockEntity = (BlockEntity) this;

        blockEntity.markDirty();
        sync();
    }

    default void sync() {
        var blockEntity = (BlockEntity) this;

        if (blockEntity.getWorld() instanceof ServerWorld serverWorld) {
            Packet<ClientPlayPacketListener> packet;
            var buf = PacketByteBufs.create();

            if ((packet = blockEntity.toUpdatePacket()) != null)
                packet.write(buf);

            for (var player : PlayerLookup.world(serverWorld))
                ServerPlayNetworking.send(player, SignalNetworking.SYNC_BLOCK_ENTITY, buf);
        } else if (blockEntity.getWorld() instanceof ClientWorld world) {
            Packet<ClientPlayPacketListener> packet;
            var buf = PacketByteBufs.create();

            if ((packet = blockEntity.toUpdatePacket()) != null)
                packet.write(buf);

            ClientPlayNetworking.send(SignalNetworking.SYNC_BLOCK_ENTITY, buf);
        }
    }
}
