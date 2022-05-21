package chaos.unity.signal.common.block;

import chaos.unity.signal.common.block.entity.ISignalEmitter;
import chaos.unity.signal.common.block.entity.ISignalReceiver;
import chaos.unity.signal.common.block.entity.SignalBlockEntities;
import chaos.unity.signal.common.block.entity.SignalBoxReceiverBlockEntity;
import chaos.unity.signal.common.item.SignalItems;
import net.minecraft.block.*;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SignalBoxReceiverBlock extends Block implements BlockEntityProvider, ISignalReceiverProvider {
    public static final VoxelShape DEFAULT_SHAPE = VoxelShapes.cuboid(0.125f, 0, 0.125f, 0.875f, 0.9375f, 0.875f);

    public SignalBoxReceiverBlock() {
        super(Settings.of(Material.METAL));
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        unbind(world, pos);
        super.onStateReplaced(state, world, pos, newState, moved);
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

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return DEFAULT_SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SignalBoxReceiverBlockEntity(pos, state);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableText("tooltip.signal.signal_box_receiver.line1"));
            tooltip.add(new TranslatableText("tooltip.signal.related_tools"));
            tooltip.add(new TranslatableText("tooltip.signal.related_tool_entry", new TranslatableText(SignalItems.SIGNAL_TUNER_ITEM.getTranslationKey())).formatted(Formatting.GOLD));
        } else {
            tooltip.add(new TranslatableText("tooltip.signal.shift_tip").formatted(Formatting.GOLD));
        }
    }
}
