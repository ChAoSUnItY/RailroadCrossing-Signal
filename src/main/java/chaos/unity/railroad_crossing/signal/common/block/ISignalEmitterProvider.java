package chaos.unity.railroad_crossing.signal.common.block;

import chaos.unity.railroad_crossing.signal.common.block.entity.ISignalEmitter;
import chaos.unity.railroad_crossing.signal.common.block.entity.ISignalReceiver;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ISignalEmitterProvider {
    default void unbindReceiver(final World world, final BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof ISignalEmitter emitter) {
            var receiverOwnerPos = emitter.getReceiverPos();

            if (receiverOwnerPos != null && world.getBlockEntity(receiverOwnerPos) instanceof ISignalReceiver receiver) {
                receiver.setEmitterPos(null);
            }
        }
    }
}
