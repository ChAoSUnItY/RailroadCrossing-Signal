package chaos.unity.signal.common.block.entity;

import chaos.unity.signal.common.data.SignalMode;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DistantSingleHeadSignalBlockEntity extends BlockEntity implements ISyncable, ISignalReceiver, ISingleHeadSignal {
    public @Nullable BlockPos receiverOwnerPos;

    public DistantSingleHeadSignalBlockEntity(BlockPos pos, BlockState state) {
        super(SignalBlockEntities.DISTANT_SINGLE_HEAD_SIGNAL_BLOCK_ENTITY, pos, state);
    }

    @Override
    public @Nullable BlockPos getReceivingOwnerPos() {
        return receiverOwnerPos;
    }

    @Override
    public void setReceivingOwnerPos(@Nullable BlockPos receivingOwnerPos) {
        this.receiverOwnerPos = receivingOwnerPos;

        markDirtyAndSync();
    }

    @Override
    public @NotNull SignalMode getSignal() {
        if (receiverOwnerPos != null && world != null && world.getBlockEntity(receiverOwnerPos) instanceof ISignalEmitter emitter) {
            return emitter.getSignal(0);
        }

        return ISignalReceiver.super.getReceivingSignal();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("receiver_owner_pos"))
            receiverOwnerPos = NbtHelper.toBlockPos(nbt.getCompound("receiver_owner_pos"));
        super.readNbt(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        if (receiverOwnerPos != null)
            nbt.put("receiver_owner_pos", NbtHelper.fromBlockPos(receiverOwnerPos));
        super.writeNbt(nbt);
    }

    @Override
    public @NotNull Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}
