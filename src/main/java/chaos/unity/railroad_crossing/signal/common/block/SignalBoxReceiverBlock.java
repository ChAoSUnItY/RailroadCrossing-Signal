package chaos.unity.railroad_crossing.signal.common.block;

import chaos.unity.railroad_crossing.signal.client.screen.SignalBoxConfigurationScreen;
import chaos.unity.railroad_crossing.signal.common.block.entity.SignalBoxReceiverBlockEntity;
import chaos.unity.railroad_crossing.signal.common.item.SignalItems;
import chaos.unity.railroad_crossing.signal.common.item.SignalTunerItem;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings({"deprecation"})
public class SignalBoxReceiverBlock extends AbstractSignalBoxBlock implements ISignalReceiverProvider, Waterloggable {
    public SignalBoxReceiverBlock() {
        super(FabricBlockSettings.of(Material.METAL));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        var item = player.getStackInHand(Hand.MAIN_HAND).getItem();

        if (!(item instanceof SignalTunerItem) && world.getBlockEntity(pos) instanceof SignalBoxReceiverBlockEntity blockEntity) {
            if (world.isClient) {
                MinecraftClient.getInstance().setScreen(new SignalBoxConfigurationScreen<>("screen.rc_signal.signal_box_receiver.title", blockEntity));
            }
            return ActionResult.SUCCESS;
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (newState.isOf(this))
            return;

        unbindEmitter(world, pos);
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState
            neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(Properties.WATERLOGGED)) {
            world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (world.getBlockEntity(pos) instanceof SignalBoxReceiverBlockEntity signalBoxReceiverBE) {
            BlockPos ownerPos = signalBoxReceiverBE.getEmitterPos();

            if (ownerPos == null)
                return 0;

            return signalBoxReceiverBE.getReceivingSignal() == signalBoxReceiverBE.detectSignal ? 15 : 0;
        }

        return 0;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return DEFAULT_SHAPE;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SignalBoxReceiverBlockEntity(pos, state);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableText("tooltip.rc_signal.signal_box_receiver.line1"));
            tooltip.add(new TranslatableText("tooltip.rc_signal.related_tools"));
            tooltip.add(new TranslatableText("tooltip.rc_signal.related_tool_entry", new TranslatableText(SignalItems.SIGNAL_TUNER_ITEM.getTranslationKey())).formatted(Formatting.GOLD));
        } else {
            tooltip.add(new TranslatableText("tooltip.rc_signal.shift_tip").formatted(Formatting.GOLD));
        }
    }
}
