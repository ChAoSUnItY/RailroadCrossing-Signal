package chaos.unity.railroad_crossing.signal.common.block;

import net.minecraft.block.*;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"deprecation"})
public abstract class AbstractSignalBlock extends BlockWithEntity implements Waterloggable {
    protected AbstractSignalBlock(Settings settings) {
        super(settings);
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

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.NORTH, Properties.EAST, Properties.SOUTH, Properties.WEST, Properties.UP, Properties.DOWN, Properties.HORIZONTAL_FACING, Properties.WATERLOGGED);
    }
}
