package chaos.unity.railroad_crossing.signal.common.block.entity;

import chaos.unity.railroad_crossing.signal.common.data.SignalMode;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ISignalEmitter {
    @NotNull SignalMode[] getSignals();

    @Nullable SignalMode getSignal(int index);

    void startTuningSession();

    void endTuningSession(@Nullable BlockPos targetPos);

    @Nullable BlockPos getReceiverPos();

    void setReceiverPos(@Nullable BlockPos receiverPos);
}
