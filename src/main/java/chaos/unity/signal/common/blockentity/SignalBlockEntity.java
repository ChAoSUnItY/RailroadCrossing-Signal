package chaos.unity.signal.common.blockentity;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class SignalBlockEntity extends BlockEntity {
    public BlockPos railBindPos;
    public BlockPos pairedSignalPos;

    public SignalBlockEntity(BlockPos pos, BlockState state) {
        super(SignalBlockEntities.SIGNAL_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, SignalBlockEntity blockEntity) {
        if (world instanceof ServerWorld serverWorld) {
            if (blockEntity.railBindPos != null &&
                    blockEntity.pairedSignalPos == null &&
                    !(serverWorld.getBlockState(blockEntity.railBindPos).getBlock() instanceof AbstractRailBlock)) {
                /** Unbind railBindPos, if this signal is part of exist interval instance, then the action would be
                 *  done by either {@link chaos.unity.signal.common.block.SignalBlock#onBroken(WorldAccess, BlockPos, BlockState)}
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
        super.readNbt(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        if (railBindPos != null)
            nbt.put("rail_bound_pos", NbtHelper.fromBlockPos(railBindPos));
        if (pairedSignalPos != null)
            nbt.put("paired_signal_pos", NbtHelper.fromBlockPos(pairedSignalPos));
        super.writeNbt(nbt);
    }
}
