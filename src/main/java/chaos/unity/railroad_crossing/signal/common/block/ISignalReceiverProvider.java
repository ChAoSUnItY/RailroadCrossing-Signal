package chaos.unity.railroad_crossing.signal.common.block;

import chaos.unity.railroad_crossing.signal.common.block.entity.ISignalEmitter;
import chaos.unity.railroad_crossing.signal.common.block.entity.ISignalReceiver;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ISignalReceiverProvider {
    default void unbind(final World world, final BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof ISignalReceiver receiver) {
            var receiverOwnerPos = receiver.getReceivingOwnerPos();

            if (receiverOwnerPos != null && world.getBlockEntity(receiverOwnerPos) instanceof ISignalEmitter emitter) {
                emitter.setReceiverPos(null);
            }
        }
    }
}
