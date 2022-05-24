package chaos.unity.railroad_crossing.signal.common.block.entity;

import chaos.unity.railroad_crossing.signal.common.data.SignalMode;
import org.jetbrains.annotations.Nullable;

public interface ISignalBox {
    @Nullable SignalMode getSignal();
}
