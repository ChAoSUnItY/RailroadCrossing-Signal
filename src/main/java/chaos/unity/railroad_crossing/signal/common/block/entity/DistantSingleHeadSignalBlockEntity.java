package chaos.unity.railroad_crossing.signal.common.block.entity;

import chaos.unity.railroad_crossing.signal.common.data.SignalMode;
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

public final class DistantSingleHeadSignalBlockEntity extends SyncableBlockEntity implements ISignalReceiver, ISingleHeadSignal {
    public @Nullable BlockPos emitterPos;

    public DistantSingleHeadSignalBlockEntity(BlockPos pos, BlockState state) {
        super(SignalBlockEntities.DISTANT_SINGLE_HEAD_SIGNAL_BLOCK_ENTITY, pos, state);
    }

    @Override
    public @Nullable BlockPos getEmitterPos() {
        return emitterPos;
    }

    @Override
    public void setEmitterPos(@Nullable BlockPos receivingOwnerPos) {
        this.emitterPos = receivingOwnerPos;

        markDirtyAndSync();
    }

    @Override
    public @Nullable SignalMode getReceivingSignal() {
        if (world == null)
            return null;

        if (emitterPos != null && world.getBlockEntity(emitterPos) instanceof ISignalEmitter emitter) {
            return emitter.getSignal(0);
        }

        return ISignalReceiver.super.getReceivingSignal();
    }

    @Override
    public @Nullable SignalMode getSignal() {
        return getReceivingSignal();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("emitter_pos"))
            emitterPos = NbtHelper.toBlockPos(nbt.getCompound("emitter_pos"));
        super.readNbt(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        if (emitterPos != null)
            nbt.put("emitter_pos", NbtHelper.fromBlockPos(emitterPos));
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
