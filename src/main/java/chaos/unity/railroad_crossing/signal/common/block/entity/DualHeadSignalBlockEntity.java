package chaos.unity.railroad_crossing.signal.common.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DualHeadSignalBlockEntity extends SyncableBlockEntity {
    public DualHeadSignalBlockEntity(BlockPos pos, BlockState state) {
        super(SignalBlockEntities.DUAL_HEAD_SIGNAL_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, DualHeadSignalBlockEntity blockEntity) {

    }
}
