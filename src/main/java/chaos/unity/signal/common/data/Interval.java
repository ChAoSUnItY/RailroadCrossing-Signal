package chaos.unity.signal.common.data;

import chaos.unity.signal.common.block.entity.SingleHeadSignalBlockEntity;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Interval must be a straight line or a straight slope, curved line is not supported.
 *
 * @param signalPosA   signal A's {@link BlockPos}
 * @param signalPosB   signal B's {@link BlockPos}
 * @param intervalPath all {@link BlockPos} of the rail blocks in the interval path
 * @param entityStatus used in runtime only, for signal checking
 */
public record Interval(@NotNull BlockPos signalPosA, @NotNull BlockPos signalPosB,
                       @NotNull List<@NotNull BlockPos> intervalPath,
                       @NotNull Interval.EntityStatus entityStatus) {
    public Interval(@NotNull BlockPos signalPosA, @NotNull BlockPos signalPosB,
                    @NotNull List<@NotNull BlockPos> intervalPath) {
        this(signalPosA, signalPosB, intervalPath, new EntityStatus());
    }

    /**
     * Unbind all signal instances, this does not unbind signal instances' rail bound pos
     */
    public void unbindAllRelatives(final ServerWorld world) {
        if (world.getBlockEntity(signalPosA) instanceof SingleHeadSignalBlockEntity sbe) {
            sbe.pairedSignalPos = null;
            sbe.setSignalMode(SignalMode.BLINK_RED);
            sbe.markDirtyAndSync();
        }

        if (world.getBlockEntity(signalPosB) instanceof SingleHeadSignalBlockEntity sbe) {
            sbe.pairedSignalPos = null;
            sbe.setSignalMode(SignalMode.BLINK_RED);
            sbe.markDirtyAndSync();
        }
    }

    public void markBlocked(final ServerWorld world, final AbstractMinecartEntity minecartEntity) {
        entityStatus.minecartEntities.compute(minecartEntity, (k, v) -> EntityStatus.Status.BLOCKING);

        if (!entityStatus.signalForceLockA && world.getBlockEntity(signalPosA) instanceof SingleHeadSignalBlockEntity sbe && sbe.mode != SignalMode.RED) {
            sbe.setSignalMode(SignalMode.RED);
            sbe.markDirtyAndSync();
        }

        if (!entityStatus.signalForceLockB && world.getBlockEntity(signalPosB) instanceof SingleHeadSignalBlockEntity sbe && sbe.mode != SignalMode.RED) {
            sbe.setSignalMode(SignalMode.RED);
            sbe.markDirtyAndSync();
        }
    }

    public void markMoving(final ServerWorld world, final AbstractMinecartEntity minecartEntity) {
        entityStatus.minecartEntities.compute(minecartEntity, (k, v) -> EntityStatus.Status.MOVING);

        // If there's already at least one minecart blocking the interval, then skip the changing
        if (entityStatus.isBlocked())
            return;

        // Check which signal the minecart is approaching, the light of signal which minecart is approaching will turn into red,
        // and the other signal's light will turn into yellow
        var minecartPos = new BlockPos(minecartEntity.getPos());
        var direction = minecartEntity.getMovementDirection();
        var commonAxis = intervalPath.get(0).getX() == intervalPath.get(intervalPath.size() - 1).getX() ? Direction.Axis.X : Direction.Axis.Z;
        boolean isApproachingSignalA = commonAxis == Direction.Axis.X
                ? signalPosA.getZ() - minecartPos.getZ() > 0 ? direction == Direction.NORTH : direction == Direction.SOUTH
                : signalPosA.getX() - minecartPos.getX() > 0 ? direction == Direction.WEST : direction == Direction.EAST;

        if (isApproachingSignalA) {
            if (world.getBlockEntity(signalPosA) instanceof SingleHeadSignalBlockEntity sbe && sbe.mode != SignalMode.YELLOW) {
                sbe.setSignalMode(SignalMode.YELLOW);
                sbe.markDirtyAndSync();
            }

            if (world.getBlockEntity(signalPosB) instanceof SingleHeadSignalBlockEntity sbe && sbe.mode != SignalMode.RED) {
                sbe.setSignalMode(SignalMode.RED);
                sbe.markDirtyAndSync();
            }
        } else {
            if (world.getBlockEntity(signalPosA) instanceof SingleHeadSignalBlockEntity sbe && sbe.mode != SignalMode.RED) {
                sbe.setSignalMode(SignalMode.RED);
                sbe.markDirtyAndSync();
            }

            if (world.getBlockEntity(signalPosB) instanceof SingleHeadSignalBlockEntity sbe && sbe.mode != SignalMode.YELLOW) {
                sbe.setSignalMode(SignalMode.YELLOW);
                sbe.markDirtyAndSync();
            }
        }
    }

    public void markCleared(final ServerWorld world, final AbstractMinecartEntity minecartEntity) {
        entityStatus.minecartEntities.remove(minecartEntity);

        // If there's already at least one minecart blocking the interval, then skip the changing
        if (!entityStatus.isBlocked() && entityStatus.minecartEntities.isEmpty()) {
            if (world.getBlockEntity(signalPosA) instanceof SingleHeadSignalBlockEntity sbe && sbe.mode != SignalMode.GREEN) {
                sbe.setSignalMode(SignalMode.GREEN);
                sbe.markDirtyAndSync();
            }

            if (world.getBlockEntity(signalPosB) instanceof SingleHeadSignalBlockEntity sbe && sbe.mode != SignalMode.GREEN) {
                sbe.setSignalMode(SignalMode.GREEN);
                sbe.markDirtyAndSync();
            }
        }
    }

    public boolean isSignal(BlockPos pos) {
        return pos.equals(signalPosA) || pos.equals(signalPosB);
    }

    public boolean isInIntervalPath(BlockPos pos) {
        return Collections.binarySearch(intervalPath, pos) != -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Interval interval = (Interval) o;
        return signalPosA.equals(interval.signalPosA) && signalPosB.equals(interval.signalPosB);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signalPosA, signalPosB);
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
        compound.put("signal_a_pos", NbtHelper.fromBlockPos(signalPosA));
        compound.put("signal_b_pos", NbtHelper.fromBlockPos(signalPosB));
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
    public static Interval getInterval(final World world, final SingleHeadSignalBlockEntity signalA, final SingleHeadSignalBlockEntity signalB) {
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

    public static List<BlockPos> findIntervalPath(final World world, final BlockPos startPos, final int start, final int end, BiConsumer<BlockPos.Mutable, Integer> setter) {
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

        return intervalPath;
    }

    static class EntityStatus {
        public final Object2ObjectOpenHashMap<AbstractMinecartEntity, Status> minecartEntities = new Object2ObjectOpenHashMap<>();
        public boolean signalForceLockA;
        public boolean signalForceLockB;

        /**
         * Checks if there's one or more minecart(s) blocking the interval.
         *
         * @return true if there is at least one minecart is blocking the interval.
         */
        public boolean isBlocked() {
            return minecartEntities.containsValue(Status.BLOCKING);
        }

        enum Status {
            BLOCKING,
            MOVING
        }
    }
}
