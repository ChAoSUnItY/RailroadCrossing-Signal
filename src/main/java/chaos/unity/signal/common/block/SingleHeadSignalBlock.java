package chaos.unity.signal.common.block;

import chaos.unity.signal.common.block.entity.ISignalEmitter;
import chaos.unity.signal.common.block.entity.ISignalReceiver;
import chaos.unity.signal.common.block.entity.SignalBlockEntities;
import chaos.unity.signal.common.block.entity.SingleHeadSignalBlockEntity;
import chaos.unity.signal.common.data.SignalMode;
import chaos.unity.signal.common.world.IntervalData;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SingleHeadSignalBlock extends HorizontalFacingBlock implements BlockEntityProvider {
    private static final VoxelShape DEFAULT_SHAPE = VoxelShapes.cuboid(.25, .25, .25, .75, .75, .75);

    public SingleHeadSignalBlock() {
        super(FabricBlockSettings.of(Material.METAL));
        setDefaultState(getStateManager().getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (world.getBlockEntity(pos) instanceof SingleHeadSignalBlockEntity sbe) {
            if (sbe.pairedSignalPos != null) {
                if (world instanceof ServerWorld serverWorld) {
                    var intervalData = IntervalData.getOrCreate(serverWorld);
                    var removedInterval = intervalData.removeBySignal(pos);

                    if (removedInterval != null) {
                        intervalData.markDirty();
                        removedInterval.unbindAllRelatives(serverWorld);
                    }
                }

                if (world.getBlockEntity(sbe.pairedSignalPos) instanceof SingleHeadSignalBlockEntity pairedSignalBE) {
                    pairedSignalBE.pairedSignalPos = null;
                    pairedSignalBE.setSignalMode(SignalMode.BLINK_RED);
                }
            }
            if (sbe.receiverPos != null && world.getBlockEntity(sbe.receiverPos) instanceof ISignalReceiver receiver) {
                receiver.setReceivingOwnerPos(null);
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return DEFAULT_SHAPE;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SingleHeadSignalBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return BlockWithEntity.checkType(type, SignalBlockEntities.SIGNAL_BLOCK_ENTITY, SingleHeadSignalBlockEntity::tick);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.HORIZONTAL_FACING);
    }
}
