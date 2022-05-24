package chaos.unity.railroad_crossing.signal.common.block;

import chaos.unity.railroad_crossing.signal.common.block.entity.ISignalReceiver;
import chaos.unity.railroad_crossing.signal.common.block.entity.ISyncable;
import chaos.unity.railroad_crossing.signal.common.block.entity.SignalBoxEmitterBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class SignalBoxEmitterBlock extends AbstractSignalBoxBlock implements ISignalEmitterProvider {
    public SignalBoxEmitterBlock() {
        super(FabricBlockSettings.of(Material.METAL));
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (newState.isOf(this))
            return;

        unbind(world, pos);
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (world.getBlockEntity(pos) instanceof SignalBoxEmitterBlockEntity emitterBlockEntity) {
            var receiverPos = emitterBlockEntity.getReceiverPos();

            world.updateNeighborsAlways(receiverPos, world.getBlockState(receiverPos).getBlock());
        }
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(Properties.WATERLOGGED)) {
            world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SignalBoxEmitterBlockEntity(pos, state);
    }
}
