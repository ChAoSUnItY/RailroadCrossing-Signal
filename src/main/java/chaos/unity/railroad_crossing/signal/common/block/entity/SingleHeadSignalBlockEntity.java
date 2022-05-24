package chaos.unity.railroad_crossing.signal.common.block.entity;

import chaos.unity.railroad_crossing.signal.common.world.IntervalData;
import chaos.unity.railroad_crossing.signal.common.block.SingleHeadSignalBlock;
import chaos.unity.railroad_crossing.signal.common.data.SignalMode;
import chaos.unity.railroad_crossing.signal.mixin.BlockMixin;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public final class SingleHeadSignalBlockEntity extends BlockEntity implements ISyncable, ISignalEmitter, ISingleHeadSignal {
    public @Nullable BlockPos railBindPos;
    public @Nullable BlockPos pairedSignalPos;
    public @Nullable BlockPos receiverPos;
    public @NotNull SignalMode signalMode = SignalMode.BLINK_RED;

    public SingleHeadSignalBlockEntity(BlockPos pos, BlockState state) {
        super(SignalBlockEntities.SINGLE_HEAD_SIGNAL_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, SingleHeadSignalBlockEntity blockEntity) {
        if (blockEntity.railBindPos == null) {
            if ((blockEntity.railBindPos = blockEntity.locateRail()) != null)
                blockEntity.markDirtyAndSync();
        }

        if (blockEntity.railBindPos != null &&
                blockEntity.pairedSignalPos == null &&
                !(world.getBlockState(blockEntity.railBindPos).getBlock() instanceof AbstractRailBlock)) {
            /** Unbind railBindPos, if this signal is part of exist interval instance, then the action would be
             *  done by either {@link SingleHeadSignalBlock#onBroken(WorldAccess, BlockPos, BlockState)}
             *  or {@link BlockMixin#onBroken(WorldAccess, BlockPos, BlockState, CallbackInfo)},
             *  based on the context to determine should also cancel its interval instance entry in
             *  {@link IntervalData}.
             */
            blockEntity.railBindPos = null;
            blockEntity.markDirtyAndSync();
        }
    }

    public @Nullable BlockPos locateRail() {
        if (world == null)
            return null;

        var pos = getPos().mutableCopy().move(0, 1, 0);

        for (int y = 0; y < 5; y++) {
            pos.move(0, -1, 0);

            if (world.getBlockState(pos).getBlock() instanceof AbstractRailBlock) {
                return pos.toImmutable();
            }

            for (Direction direction : Direction.Type.HORIZONTAL) {
                BlockPos currentPos;

                for (var i = 1; i <= 2; i++) {
                    if (world.getBlockState((currentPos = pos.offset(direction, i))).getBlock() instanceof AbstractRailBlock) {
                        return currentPos;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Begin the linkage session, this will automatically deregister interval if it is part of interval instance
     */
    public void startSurveySession() {
        signalMode = SignalMode.BLINK_YELLOW;

        if (getWorld() instanceof ServerWorld serverWorld) {
            var intervalData = IntervalData.getOrCreate(serverWorld);
            var removedInterval = intervalData.removeBySignal(getPos());

            if (removedInterval != null) {
                removedInterval.unbindAllRelatives(serverWorld);
            }
        }

        markDirtyAndSync();
    }

    @Override
    public void startTuningSession() {
        if (world == null)
            return;

        signalMode = SignalMode.BLINK_YELLOW;

        if (receiverPos != null) {
            // Take the ownership of current receiver
            if (world.getBlockEntity(receiverPos) instanceof ISignalReceiver receiver) {
                receiver.setReceivingOwnerPos(null);
            }

            world.updateNeighbors(receiverPos, world.getBlockState(receiverPos).getBlock());
            receiverPos = null;
        }

        markDirtyAndSync();
    }

    /**
     * Complete the linkage session, this will deregister interval instance which target signal is in if available.
     *
     * @param signalPos if session is incomplete, pass null, otherwise pass target signal's {@link BlockPos}
     */
    public void endSurveySession(@Nullable BlockPos signalPos) {
        if (signalPos != null) {
            pairedSignalPos = signalPos;
            signalMode = SignalMode.GREEN;
        } else {
            signalMode = SignalMode.BLINK_RED;
        }

        markDirtyAndSync();
    }

    @Override
    public void endTuningSession(@Nullable BlockPos receiverPos) {
        if (world == null)
            return;

        if (receiverPos != null) {
            if (this.receiverPos != null && world.getBlockEntity(this.receiverPos) instanceof ISignalReceiver receiver) {
                receiver.setReceivingOwnerPos(null);
            }

            this.receiverPos = receiverPos;
            world.updateNeighbors(receiverPos, world.getBlockState(receiverPos).getBlock());
        }

        signalMode = pairedSignalPos != null ? SignalMode.GREEN : SignalMode.BLINK_RED;

        markDirtyAndSync();
    }

    public void setSignalMode(@NotNull SignalMode mode) {
        if (world == null)
            return;

        if (this.signalMode == SignalMode.BLINK_YELLOW)
            return; // Occupied by either surveying or tuning session

        this.signalMode = mode;

        if (receiverPos != null) {
            world.updateNeighbors(receiverPos, world.getBlockState(receiverPos).getBlock());
        }
    }

    public boolean isInInterval() {
        return getWorld() instanceof ServerWorld serverWorld && IntervalData.getOrCreate(serverWorld).getBySignal(getPos()) != null;
    }

    public boolean hasPaired() {
        return pairedSignalPos != null;
    }

    public boolean hasRail() {
        return railBindPos != null;
    }

    @Override
    public SignalMode[] getSignals() {
        return new SignalMode[]{signalMode};
    }

    @Override
    public SignalMode getSignal(int index) {
        return signalMode;
    }

    @Override
    public @NotNull SignalMode getSignal() {
        return signalMode;
    }

    @Nullable
    @Override
    public BlockPos getReceiverPos() {
        return receiverPos;
    }

    @Override
    public void setReceiverPos(BlockPos receiverPos) {
        this.receiverPos = receiverPos;

        markDirtyAndSync();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("rail_bound_pos"))
            railBindPos = NbtHelper.toBlockPos(nbt.getCompound("rail_bound_pos"));
        if (nbt.contains("paired_signal_pos"))
            pairedSignalPos = NbtHelper.toBlockPos(nbt.getCompound("paired_signal_pos"));
        if (nbt.contains("receiver_pos"))
            receiverPos = NbtHelper.toBlockPos(nbt.getCompound("receiver_pos"));
        signalMode = SignalMode.values[nbt.getInt("signal_mode")];
        super.readNbt(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        if (railBindPos != null)
            nbt.put("rail_bound_pos", NbtHelper.fromBlockPos(railBindPos));
        if (pairedSignalPos != null)
            nbt.put("paired_signal_pos", NbtHelper.fromBlockPos(pairedSignalPos));
        if (receiverPos != null)
            nbt.put("receiver_pos", NbtHelper.fromBlockPos(receiverPos));
        nbt.putInt("signal_mode", signalMode.ordinal());
        super.writeNbt(nbt);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}
