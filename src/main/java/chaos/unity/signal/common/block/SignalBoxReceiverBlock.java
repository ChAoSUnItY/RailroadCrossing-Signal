package chaos.unity.signal.common.block;

import chaos.unity.signal.common.block.entity.ISignalEmitter;
import chaos.unity.signal.common.block.entity.ISignalReceiver;
import chaos.unity.signal.common.block.entity.SignalBlockEntities;
import chaos.unity.signal.common.block.entity.SignalBoxReceiverBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SignalBoxReceiverBlock extends Block implements BlockEntityProvider {
    public SignalBoxReceiverBlock() {
        super(Settings.of(Material.METAL));
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
        if (world.getBlockEntity(pos) instanceof ISignalReceiver receiver) {
            var receiverOwnerPos = receiver.getReceivingOwnerPos();

            if (receiverOwnerPos != null && world.getBlockEntity(receiverOwnerPos) instanceof ISignalEmitter emitter) {
                emitter.setReceiverPos(null);
            }
        }
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (world.getBlockEntity(pos) instanceof SignalBoxReceiverBlockEntity signalBoxReceiverBE) {
            BlockPos ownerPos = signalBoxReceiverBE.getReceivingOwnerPos();

            if (ownerPos == null)
                return 0;

            return world.getBlockEntity(ownerPos) instanceof ISignalEmitter emitter && emitter.getSignal(0) == signalBoxReceiverBE.detectMode ? 15 : 0;
        }

        return 0;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SignalBoxReceiverBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return BlockWithEntity.checkType(type, SignalBlockEntities.SIGNAL_BOX_RECEIVER_BLOCK_ENTITY, SignalBoxReceiverBlockEntity::tick);
    }
}
