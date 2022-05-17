package chaos.unity.signal.common.world;

import chaos.unity.signal.common.data.Interval;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.List;

public class IntervalData extends PersistentState {
    public final List<Interval> intervals;

    public IntervalData() {
        this.intervals = new ArrayList<>();
    }

    public IntervalData(List<Interval> intervals) {
        this.intervals = intervals;
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
}
