package chaos.unity.railroad_crossing.signal;

import chaos.unity.railroad_crossing.signal.common.block.entity.AbstractBlockSignalBlockEntity;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public final class Util {
    public static  <T extends AbstractBlockSignalBlockEntity> @Nullable BlockPos locateRail(T blockEntity) {
        final var world = blockEntity.getWorld();

        if (world == null)
            return null;

        var pos = blockEntity.getPos().mutableCopy().move(0, 1, 0);

        for (int y = 0; y < 5; y++) {
            pos.move(0, -1, 0);

            if (world.getBlockState(pos).getBlock() instanceof AbstractRailBlock) {
                return pos.toImmutable();
            }

            for (Direction direction : Direction.Type.HORIZONTAL) {
                BlockPos currentPos;

                for (var i = 1; i <= 2; i++) {
                    if (world.getBlockState((currentPos = pos.offset(direction, i))).getBlock() instanceof AbstractRailBlock) {
                        return currentPos;
                    }
                }
            }
        }

        return null;
    }
}
