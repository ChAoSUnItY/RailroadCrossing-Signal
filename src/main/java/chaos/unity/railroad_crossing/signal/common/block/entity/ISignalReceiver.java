package chaos.unity.railroad_crossing.signal.common.block.entity;

import chaos.unity.railroad_crossing.signal.common.data.SignalMode;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ISignalReceiver {
    @Nullable BlockPos getEmitterPos();

    void setEmitterPos(@Nullable BlockPos emitterPos);

    default @Nullable SignalMode getReceivingSignal() {
        return SignalMode.BLINK_RED;
    }
}
