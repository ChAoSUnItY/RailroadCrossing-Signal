package chaos.unity.railroad_crossing.signal.common.block.entity;

import chaos.unity.railroad_crossing.signal.common.data.SignalMode;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SignalBoxEmitterBlockEntity extends SyncableBlockEntity implements ISignalEmitter, ISignalBox {
    public @Nullable BlockPos receiverPos;
    public @NotNull SignalMode emittingSignalMode = SignalMode.BLINK_RED;

    public SignalBoxEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(SignalBlockEntities.SIGNAL_BOX_EMITTER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public @NotNull SignalMode[] getSignals() {
        return new SignalMode[]{emittingSignalMode};
    }

    @Override
    public @Nullable SignalMode getSignal(int index) {
        return world != null && world.isReceivingRedstonePower(pos) ? emittingSignalMode : null;
    }

    @Override
    public @Nullable SignalMode getSignal() {
        return getSignal(0);
    }

    @Override
    public void startTuningSession() {
        if (world == null)
            return;

        if (receiverPos != null) {
            if (world.getBlockEntity(receiverPos) instanceof ISignalReceiver receiver) {
                receiver.setEmitterPos(null);
            }

            world.updateNeighborsAlways(receiverPos, world.getBlockState(receiverPos).getBlock());
            receiverPos = null;
        }

        markDirtyAndSync();
    }

    @Override
    public void endTuningSession(@Nullable BlockPos targetPos) {
        if (world == null)
            return;

        if (targetPos != null) {
            if (world.getBlockEntity(targetPos) instanceof ISignalReceiver receiver) {
                receiver.setEmitterPos(pos);
            }
            receiverPos = targetPos;

            world.updateNeighborsAlways(targetPos, world.getBlockState(targetPos).getBlock());
        }

        markDirtyAndSync();
    }

    @Override
    public @Nullable BlockPos getReceiverPos() {
        return receiverPos;
    }

    @Override
    public void setReceiverPos(@Nullable BlockPos receiverPos) {
        this.receiverPos = receiverPos;

        markDirtyAndSync();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("receiver_pos"))
            receiverPos = NbtHelper.toBlockPos(nbt.getCompound("receiver_pos"));
        emittingSignalMode = SignalMode.values[nbt.getInt("signal_mode")];
        super.readNbt(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        if (receiverPos != null)
            nbt.put("receiver_pos", NbtHelper.fromBlockPos(receiverPos));
        nbt.putInt("signal_mode", emittingSignalMode.ordinal());
        super.writeNbt(nbt);
    }
}
