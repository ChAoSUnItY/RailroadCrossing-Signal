package chaos.unity.railroad_crossing.signal.common.block.entity;

import chaos.unity.railroad_crossing.signal.common.data.SignalMode;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SingleHeadSignalBlockEntity extends AbstractBlockSignalBlockEntity implements ISingleHeadSignal {
    public @Nullable BlockPos railBindPos;
    public @Nullable BlockPos pairedSignalPos;
    public @Nullable BlockPos receiverPos;
    public @NotNull SignalMode signalMode = SignalMode.BLINK_RED;

    public SingleHeadSignalBlockEntity(BlockPos pos, BlockState state) {
        super(SignalBlockEntities.SINGLE_HEAD_SIGNAL_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, SingleHeadSignalBlockEntity blockEntity) {
        blockEntity.tick(world);
    }


    @Override
    public @NotNull SignalMode getSignal() {
        return signalMode;
    }
}
