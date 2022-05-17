package chaos.unity.signal.common.data;

import chaos.unity.signal.common.blockentity.SignalBlockEntity;
import com.google.common.collect.ComparisonChain;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Interval must be a straight line or a straight slope, curved line is not supported.
 *
 * @param signalAPos
 * @param signalBPos
 * @param intervalPath
 */
public record Interval(@NotNull BlockPos signalAPos, @NotNull BlockPos signalBPos,
                       @NotNull List<@NotNull BlockPos> intervalPath) {
    public boolean isSignal(BlockPos pos) {
        return pos.equals(signalAPos) || pos.equals(signalBPos);
    }

    public boolean isInIntervalPath(BlockPos pos) {
        return Collections.binarySearch(intervalPath, pos) != -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Interval interval = (Interval) o;
        return signalAPos.equals(interval.signalAPos) && signalBPos.equals(interval.signalBPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signalAPos, signalBPos);
    }

    public static Interval readNbt(NbtCompound nbt) {
        BlockPos signalAPos = NbtHelper.toBlockPos(nbt.getCompound("signal_a_pos")), signalBPos = NbtHelper.toBlockPos(nbt.getCompound("signal_b_pos"));
        List<BlockPos> intervalPath = new ArrayList<>();

        for (var pos : nbt.getList("interval_path", NbtElement.COMPOUND_TYPE)) {
            intervalPath.add(NbtHelper.toBlockPos((NbtCompound) pos));
        }

        return new Interval(signalAPos, signalBPos, intervalPath);
    }

    public NbtCompound writeNbt() {
        var compound = new NbtCompound();
        compound.put("signal_a_pos", NbtHelper.fromBlockPos(signalAPos));
        compound.put("signal_b_pos", NbtHelper.fromBlockPos(signalBPos));
        var intervalPath = new NbtList();

        for (var pos : this.intervalPath) {
            intervalPath.add(NbtHelper.fromBlockPos(pos));
        }

        compound.put("interval_path", intervalPath);

        return compound;
    }

    /**
     * @param world
     * @param signalA
     * @param signalB
     * @return null when unable to retrieve interval
     */
    public static Interval getInterval(final World world, final SignalBlockEntity signalA, final SignalBlockEntity signalB) {
        BlockPos railStartPoint = signalA.railBindPos, railEndPoint = signalB.railBindPos, signalPosA = signalA.getPos(), signalPosB = signalB.getPos();

        if (railStartPoint == null || railEndPoint == null)
            return null;

        if (railStartPoint.getZ() == railEndPoint.getZ()) {
            // Interval's Z poses are same
            int x1 = railStartPoint.getX(), x2 = railEndPoint.getX(), startX = Math.min(x1, x2), endX = Math.max(x1, x2);
            return findIntervalPath(world, signalPosA, signalPosB, railStartPoint, startX, endX, BlockPos.Mutable::setX);
        } else if (railStartPoint.getX() == railEndPoint.getX()) {
            // Interval's X poses are same
            int z1 = railStartPoint.getZ(), z2 = railEndPoint.getZ(), startZ = Math.min(z1, z2), endZ = Math.max(z1, z2);
            return findIntervalPath(world, signalPosA, signalPosB, railStartPoint, startZ, endZ, BlockPos.Mutable::setZ);
        } else {
            // Invalid interval
            return null;
        }
    }

    private static Interval findIntervalPath(final World world, final BlockPos signalPosA, final BlockPos signalPosB, final BlockPos startPos, final int start, final int end, BiConsumer<BlockPos.Mutable, Integer> setter) {
        var intervalPath = new ArrayList<BlockPos>();
        var blockPosPool = startPos.mutableCopy();

        for (var i = start; i <= end; i++) {
            var found = false;
            int y = blockPosPool.getY();
            setter.accept(blockPosPool, i);

            for (var j = -1; j < 2; j++) {
                if (world.getBlockState(blockPosPool.setY(y - j)).getBlock() instanceof AbstractRailBlock) {
                    intervalPath.add(new BlockPos(blockPosPool));
                    found = true;
                    break;
                }
            }

            // Interval path incomplete
            if (!found)
                return null;
        }

        return new Interval(new BlockPos(signalPosA), new BlockPos(signalPosB), intervalPath);
    }
}
