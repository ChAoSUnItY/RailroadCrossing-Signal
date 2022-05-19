package chaos.unity.signal.common.blockentity;

import chaos.unity.signal.common.block.SingleHeadSignalBlock;
import chaos.unity.signal.common.data.SignalMode;
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

public class SingleHeadSignalBlockEntity extends BlockEntity {
    public BlockPos railBindPos;
    public BlockPos pairedSignalPos;
    public @NotNull SignalMode mode = SignalMode.BLINK_RED;

    public SingleHeadSignalBlockEntity(BlockPos pos, BlockState state) {
        super(SignalBlockEntities.SIGNAL_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, SingleHeadSignalBlockEntity blockEntity) {
        if (world instanceof ServerWorld serverWorld) {
            if (blockEntity.railBindPos != null &&
                    blockEntity.pairedSignalPos == null &&
                    !(serverWorld.getBlockState(blockEntity.railBindPos).getBlock() instanceof AbstractRailBlock)) {
                /** Unbind railBindPos, if this signal is part of exist interval instance, then the action would be
                 *  done by either {@link SingleHeadSignalBlock#onBroken(WorldAccess, BlockPos, BlockState)}
                 *  or {@link chaos.unity.signal.mixin.BlockMixin#onBroken(WorldAccess, BlockPos, BlockState, CallbackInfo)},
                 *  based on the context to determine should also cancel its interval instance entry in
                 *  {@link chaos.unity.signal.common.world.IntervalData}.
                 */
                blockEntity.railBindPos = null;
            }
        }
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
