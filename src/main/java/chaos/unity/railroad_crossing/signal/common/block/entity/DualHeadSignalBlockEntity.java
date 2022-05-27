package chaos.unity.railroad_crossing.signal.common.block.entity;

import chaos.unity.railroad_crossing.signal.common.data.SignalMode;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DualHeadSignalBlockEntity extends AbstractBlockSignalBlockEntity implements ISignalReceiver {
    public @Nullable BlockPos emitterPos;

    public DualHeadSignalBlockEntity(BlockPos pos, BlockState state) {
        super(SignalBlockEntities.DUAL_HEAD_SIGNAL_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, DualHeadSignalBlockEntity blockEntity) {
        blockEntity.tick(world);
    }

    @Override
    public SignalMode getSignal(int index) {
        return switch (index) {
            case 0 -> getReceivingSignal();
            case 1 -> signalMode;
            default -> null;
        };
    }

    @Override
    public @Nullable SignalMode getReceivingSignal() {
        if (world == null)
            return null;

        if (emitterPos != null && world.getBlockEntity(emitterPos) instanceof ISignalEmitter emitter) {
            return emitter.getSignal(0);
        }

        return null;
    }

    @Override
    public @Nullable BlockPos getEmitterPos() {
        return emitterPos;
    }

    @Override
    public void setEmitterPos(@Nullable BlockPos emitterPos) {
        this.emitterPos = emitterPos;
        markDirtyAndSync();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("emitter_pos"))
            emitterPos = NbtHelper.toBlockPos(nbt.getCompound("emitter_pos"));
        super.readNbt(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        if (emitterPos != null)
            nbt.put("emitter_pos", NbtHelper.fromBlockPos(emitterPos));
        super.writeNbt(nbt);
    }
}
