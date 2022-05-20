package chaos.unity.signal.common.block.entity;

import chaos.unity.signal.common.data.SignalMode;
import org.jetbrains.annotations.NotNull;

public sealed interface ISingleHeadSignal permits SingleHeadSignalBlockEntity {
    @NotNull SignalMode getSignal();
}
