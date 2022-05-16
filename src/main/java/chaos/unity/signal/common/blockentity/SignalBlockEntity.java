package chaos.unity.signal.common.blockentity;

import chaos.unity.signal.common.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class SignalBlockEntity extends BlockEntity {
    public BlockPos railBindPos;
    public BlockPos pairedSignalPos;

    public SignalBlockEntity(BlockPos pos, BlockState state) {
        super(SignalBlockEntities.SIGNAL_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("rail_bound_pos"))
            railBindPos = Utils.fromIntArray(nbt.getIntArray("rail_bound_pos"));
        if (nbt.contains("paired_signal_pos"))
            pairedSignalPos = Utils.fromIntArray(nbt.getIntArray("paired_signal_pos"));
        super.readNbt(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        if (railBindPos != null)
            nbt.putIntArray("rail_bound_pos", Utils.asIntArray(railBindPos));
        if (pairedSignalPos != null)
            nbt.putIntArray("paired_signal_pos", Utils.asIntArray(pairedSignalPos));
        super.writeNbt(nbt);
    }
}
