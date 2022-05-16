package chaos.unity.signal.common.util;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public final class Utils {
    public static int[] asIntArray(@NotNull BlockPos pos) {
        return new int[]{pos.getX(), pos.getY(), pos.getZ()};
    }

    public static BlockPos fromIntArray(int @NotNull [] intArray) {
        assert intArray.length == 3;

        return new BlockPos(intArray[0], intArray[1], intArray[2]);
    }
}
