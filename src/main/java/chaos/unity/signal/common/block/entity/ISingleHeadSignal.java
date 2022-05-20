package chaos.unity.signal.common.block.entity;

import chaos.unity.signal.common.data.SignalMode;
import org.jetbrains.annotations.NotNull;

public sealed interface ISingleHeadSignal permits DistantSingleHeadSignalBlockEntity, SingleHeadSignalBlockEntity {
    @NotNull SignalMode getSignal();
}
