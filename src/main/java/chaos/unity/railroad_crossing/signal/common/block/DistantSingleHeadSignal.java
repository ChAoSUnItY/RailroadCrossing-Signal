package chaos.unity.railroad_crossing.signal.common.block;

import chaos.unity.railroad_crossing.signal.common.item.SignalItems;
import chaos.unity.railroad_crossing.signal.common.block.entity.DistantSingleHeadSignalBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DistantSingleHeadSignal extends SingleHeadSignalBlock implements ISignalReceiverProvider {
    public DistantSingleHeadSignal() {
        super();
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (newState.isOf(this))
            return;

        unbindEmitter(world, pos);
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SingleHeadSignalBlock.DEFAULT_SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DistantSingleHeadSignalBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableText("tooltip.rc_signal.distant_single_head_signal.line1"));
            tooltip.add(new TranslatableText("tooltip.rc_signal.related_tools"));
            tooltip.add(new TranslatableText("tooltip.rc_signal.related_tool_entry", new TranslatableText(SignalItems.SIGNAL_TUNER_ITEM.getTranslationKey())).formatted(Formatting.GOLD));
        } else {
            tooltip.add(new TranslatableText("tooltip.rc_signal.shift_tip").formatted(Formatting.GOLD));
        }
    }
}
