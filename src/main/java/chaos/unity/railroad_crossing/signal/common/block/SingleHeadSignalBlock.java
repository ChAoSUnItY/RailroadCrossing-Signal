package chaos.unity.railroad_crossing.signal.common.block;

import chaos.unity.railroad_crossing.signal.common.block.entity.ISignalReceiver;
import chaos.unity.railroad_crossing.signal.common.block.entity.SignalBlockEntities;
import chaos.unity.railroad_crossing.signal.common.block.entity.SingleHeadSignalBlockEntity;
import chaos.unity.railroad_crossing.signal.common.data.SignalMode;
import chaos.unity.railroad_crossing.signal.common.item.SignalItems;
import chaos.unity.railroad_crossing.signal.common.world.IntervalData;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SingleHeadSignalBlock extends BlockWithEntity implements BlockEntityProvider, Waterloggable {
    static final VoxelShape COLLISION_SHAPE = VoxelShapes.cuboid(.25, .25, .25, .75, 1, .75);
    static final VoxelShape DEFAULT_SHAPE = VoxelShapes.cuboid(.25, .25, .25, .75, .75, .75);

    public SingleHeadSignalBlock() {
        super(FabricBlockSettings.of(Material.METAL));
        setDefaultState(getStateManager().getDefaultState()
                .with(Properties.HORIZONTAL_FACING, Direction.NORTH)
                .with(Properties.NORTH, false)
                .with(Properties.EAST, false)
                .with(Properties.SOUTH, false)
                .with(Properties.WEST, false)
                .with(Properties.UP, false)
                .with(Properties.DOWN, false));
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (newState.isOf(this))
            return;

        if (world.getBlockEntity(pos) instanceof SingleHeadSignalBlockEntity sbe) {
            if (sbe.hasPaired()) {
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

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(Properties.NORTH) || state.get(Properties.EAST) || state.get(Properties.SOUTH) || state.get(Properties.WEST) || state.get(Properties.UP)
                ? COLLISION_SHAPE
                : DEFAULT_SHAPE;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(Properties.HORIZONTAL_FACING, rotation.rotate(state.get(Properties.HORIZONTAL_FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(Properties.HORIZONTAL_FACING)));
    }

    protected boolean canConnect(WorldAccess world, BlockPos pos, Direction facingDir, Direction dir) {
        var state = world.getBlockState(pos);
        var block = state.getBlock();
        var bl2 = block instanceof FenceGateBlock && FenceGateBlock.canWallConnect(state, dir);
        return facingDir != dir.getOpposite() && !FenceBlock.cannotConnect(state) && state.isSideSolidFullSquare(world, pos, dir) || bl2 || state.getBlock() instanceof SingleHeadSignalBlock;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        var world = ctx.getWorld();
        var pos = ctx.getBlockPos();
        var facingDir = ctx.getPlayerFacing().getOpposite();
        var fluidState = world.getFluidState(pos);
        var northPos = pos.north();
        var eastPos = pos.east();
        var southPos = pos.south();
        var westPos = pos.west();
        var upPos = pos.up();
        var downPos = pos.down();
        return getDefaultState().with(Properties.HORIZONTAL_FACING, facingDir)
                .with(Properties.NORTH, canConnect(world, northPos, facingDir, Direction.SOUTH))
                .with(Properties.EAST, canConnect(world, eastPos, facingDir, Direction.WEST))
                .with(Properties.SOUTH, canConnect(world, southPos, facingDir, Direction.NORTH))
                .with(Properties.WEST, canConnect(world, westPos, facingDir, Direction.EAST))
                .with(Properties.UP, canConnect(world, upPos, facingDir, Direction.DOWN))
                .with(Properties.DOWN, canConnect(world, downPos, facingDir, Direction.UP))
                .with(Properties.WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(Properties.WATERLOGGED)) {
            world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return state.with(ConnectingBlock.FACING_PROPERTIES.get(direction), this.canConnect(world, neighborPos, state.get(Properties.HORIZONTAL_FACING), direction.getOpposite()));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SingleHeadSignalBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, SignalBlockEntities.SINGLE_HEAD_SIGNAL_BLOCK_ENTITY, SingleHeadSignalBlockEntity::tick);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.NORTH, Properties.EAST, Properties.SOUTH, Properties.WEST, Properties.UP, Properties.DOWN, Properties.HORIZONTAL_FACING, Properties.WATERLOGGED);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableText("tooltip.rc_signal.single_head_signal.line1"));
            tooltip.add(new TranslatableText("tooltip.rc_signal.related_tools"));
            tooltip.add(new TranslatableText("tooltip.rc_signal.related_tool_entry", new TranslatableText(SignalItems.SIGNAL_SURVEYOR_ITEM.getTranslationKey())).formatted(Formatting.GOLD));
            tooltip.add(new TranslatableText("tooltip.rc_signal.related_tool_entry", new TranslatableText(SignalItems.SIGNAL_TUNER_ITEM.getTranslationKey())).formatted(Formatting.GOLD));
        } else {
            tooltip.add(new TranslatableText("tooltip.rc_signal.shift_tip").formatted(Formatting.GOLD));
        }
    }
}
