package chaos.unity.signal.common.block.entity;

import chaos.unity.signal.common.block.SingleHeadSignalBlock;
import chaos.unity.signal.common.data.SignalMode;
import chaos.unity.signal.common.world.IntervalData;
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
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class SingleHeadSignalBlockEntity extends BlockEntity implements ISyncable {
    public BlockPos railBindPos;
    public BlockPos pairedSignalPos;
    public @NotNull SignalMode mode = SignalMode.BLINK_RED;

    public SingleHeadSignalBlockEntity(BlockPos pos, BlockState state) {
        super(SignalBlockEntities.SIGNAL_BLOCK_ENTITY, pos, state);
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
             *  or {@link chaos.unity.signal.mixin.BlockMixin#onBroken(WorldAccess, BlockPos, BlockState, CallbackInfo)},
             *  based on the context to determine should also cancel its interval instance entry in
             *  {@link chaos.unity.signal.common.world.IntervalData}.
             */
            blockEntity.railBindPos = null;
            blockEntity.markDirtyAndSync();
        }
    }

    public @Nullable BlockPos locateRail() {
        var pos = getPos().mutableCopy();
        int my = pos.getY() - 5, mx = pos.getX() + 2, mz = pos.getZ() + 2;
        int ox = pos.getX() - 2, oz = pos.getZ() - 2;

        for (int y = pos.getY() + 1; y > my; y--) {
            pos.setY(y);

            for (int x = ox; x < mx; x++) {
                pos.setX(x);

                for (int z = oz; z < mz; z++) {
                    pos.setZ(z);

                    if (world.getBlockState(pos).getBlock() instanceof AbstractRailBlock) {
                        return pos.toImmutable();
                    }
                }
            }
        }

        return null;
    }

    /**
     * Begin the linkage session, this will automatically deregister interval if it is part of interval instance
     */
    public void startLinkSession() {
        mode = SignalMode.BLINK_YELLOW;

        if (getWorld() instanceof ServerWorld serverWorld) {
            var intervalData = IntervalData.getOrCreate(serverWorld);
            var removedInterval = intervalData.removeBySignal(getPos());

            if (removedInterval != null) {
                removedInterval.unbindAllRelatives(serverWorld);
            }
        }

        markDirtyAndSync();
    }

    /**
     * Complete the linkage session, this will deregister interval instance which target signal is in if available.
     *
     * @param signalPos if session is incomplete, pass null, otherwise pass target signal's {@link BlockPos}
     */
    public void endLinkSession(@Nullable BlockPos signalPos) {
        if (signalPos != null) {
            pairedSignalPos = signalPos;
            mode = SignalMode.GREEN;
        } else {
            mode = SignalMode.BLINK_RED;
        }

        markDirtyAndSync();
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
    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("rail_bound_pos"))
            railBindPos = NbtHelper.toBlockPos(nbt.getCompound("rail_bound_pos"));
        if (nbt.contains("paired_signal_pos"))
            pairedSignalPos = NbtHelper.toBlockPos(nbt.getCompound("paired_signal_pos"));
        mode = SignalMode.values[nbt.getInt("signal_mode")];
        super.readNbt(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        if (railBindPos != null)
            nbt.put("rail_bound_pos", NbtHelper.fromBlockPos(railBindPos));
        if (pairedSignalPos != null)
            nbt.put("paired_signal_pos", NbtHelper.fromBlockPos(pairedSignalPos));
        nbt.putInt("signal_mode", mode.ordinal());
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
