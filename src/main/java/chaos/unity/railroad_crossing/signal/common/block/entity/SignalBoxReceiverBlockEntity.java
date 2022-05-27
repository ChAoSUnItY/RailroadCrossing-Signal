package chaos.unity.railroad_crossing.signal.common.block.entity;

import chaos.unity.railroad_crossing.signal.common.data.SignalMode;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SignalBoxReceiverBlockEntity extends SyncableBlockEntity implements ISignalReceiver, ISignalBox {
    public @Nullable BlockPos emitterPos;
    public @NotNull SignalMode detectSignal = SignalMode.RED;

    public SignalBoxReceiverBlockEntity(BlockPos pos, BlockState state) {
        super(SignalBlockEntities.SIGNAL_BOX_RECEIVER_BLOCK_ENTITY, pos, state);
    }

    @Nullable
    @Override
    public BlockPos getEmitterPos() {
        return emitterPos;
    }

    @Override
    public void setEmitterPos(@Nullable BlockPos emitterPos) {
        if (world == null)
            return;

        this.emitterPos = emitterPos;

        markDirtyAndSync();
        world.updateNeighborsAlways(getPos(), world.getBlockState(getPos()).getBlock());
    }

    @Override
    public @Nullable SignalMode getReceivingSignal() {
        if (world == null)
            return null;

        if (emitterPos != null && world.getBlockEntity(emitterPos) instanceof ISignalEmitter emitter) {
            return emitter.getSignal(1);
        }

        return null;
    }

    @Override
    public void setSignal(@NotNull SignalMode signal) {
        detectSignal = signal;
        markDirtyAndSync();
    }

    @Override
    public @Nullable SignalMode getSignal() {
        return detectSignal;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("emitter_pos"))
            emitterPos = NbtHelper.toBlockPos(nbt.getCompound("emitter_pos"));
        detectSignal = SignalMode.values[nbt.getInt("detect_signal")];
        super.readNbt(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        if (emitterPos != null)
            nbt.put("emitter_pos", NbtHelper.fromBlockPos(emitterPos));
        nbt.putInt("detect_signal", detectSignal.ordinal());
        super.writeNbt(nbt);
    }
}
