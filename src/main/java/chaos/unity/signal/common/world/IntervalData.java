package chaos.unity.signal.common.world;

import chaos.unity.signal.common.data.Interval;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IntervalData extends PersistentState {
    public final List<Interval> intervals;

    public IntervalData() {
        this.intervals = new ArrayList<>();
    }

    public IntervalData(List<Interval> intervals) {
        this.intervals = intervals;
    }

    public boolean addInterval(Interval interval) {
        if (intervals.contains(interval)) {
            return false;
        }

        return intervals.add(interval);
    }

    public @Nullable Interval getBySignal(BlockPos signalPos) {
        for (Interval interval : intervals)
            if (interval.isSignal(signalPos))
                return interval;
        return null;
    }

    public @Nullable Interval removeBySignal(BlockPos signalPos) {
        for (var i = 0; i < intervals.size(); i++)
            if (intervals.get(i).isSignal(signalPos))
                return intervals.remove(i);
        return null;
    }

    public @Nullable Interval getByIntervalPath(BlockPos pos) {
        for (Interval interval : intervals)
            if (Collections.binarySearch(interval.intervalPath(), pos) != -1)
                return interval;
        return null;
    }

    public @Nullable Interval removeByIntervalPath(BlockPos pos) {
        for (var i = 0; i < intervals.size(); i++)
            if (intervals.get(i).isInIntervalPath(pos))
                return intervals.remove(i);
        return null;
    }

    public static IntervalData readNbt(NbtCompound nbt) {
        List<Interval> intervals = new ArrayList<>();

        for (var compound : nbt.getList("intervals", NbtElement.COMPOUND_TYPE)) {
            intervals.add(Interval.readNbt((NbtCompound) compound));
        }

        return new IntervalData(intervals);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        var intervals = new NbtList();

        for (var interval : this.intervals) {
            intervals.add(interval.writeNbt());
        }

        nbt.put("intervals", intervals);

        return nbt;
    }

    public static IntervalData getOrCreate(final ServerWorld serverWorld) {
        return serverWorld.getPersistentStateManager().getOrCreate(IntervalData::readNbt, IntervalData::new, "signal_intervals");
    }
}
