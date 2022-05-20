package chaos.unity.signal.common.block.entity;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ISignalReceiver {
    @Nullable BlockPos getReceivingOwnerPos();

    void setReceivingOwnerPos(@NotNull BlockPos receivingOwnerPos);
}
