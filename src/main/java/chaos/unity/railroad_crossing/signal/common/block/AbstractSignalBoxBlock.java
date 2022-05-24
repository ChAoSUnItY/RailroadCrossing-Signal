package chaos.unity.railroad_crossing.signal.common.block;

import net.minecraft.block.*;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public abstract class AbstractSignalBoxBlock extends BlockWithEntity implements Waterloggable {
    protected static final VoxelShape DEFAULT_SHAPE = VoxelShapes.cuboid(0.125f, 0, 0.125f, 0.875f, 0.9375f, 0.875f);

    protected AbstractSignalBoxBlock(Settings settings) {
        super(settings);

        setDefaultState(getStateManager().getDefaultState().with(Properties.WATERLOGGED, false));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return DEFAULT_SHAPE;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.WATERLOGGED);
    }
}
