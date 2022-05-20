package chaos.unity.signal.common.block.entity;

import chaos.unity.signal.common.data.SignalMode;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ISignalReceiver {
    @Nullable BlockPos getReceivingOwnerPos();

    void setReceivingOwnerPos(@Nullable BlockPos receivingOwnerPos);

    default @NotNull SignalMode getReceivingMode() {
        return SignalMode.BLINK_RED;
    }
}
