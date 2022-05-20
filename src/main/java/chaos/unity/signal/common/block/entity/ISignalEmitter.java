package chaos.unity.signal.common.block.entity;

import chaos.unity.signal.common.data.SignalMode;

public interface ISignalEmitter {
    SignalMode[] getSignals();

    SignalMode getSignal(int index);
}
