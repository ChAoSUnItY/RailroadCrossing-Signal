package chaos.unity.railroad_crossing.signal.common.block;

import chaos.unity.railroad_crossing.signal.common.block.entity.AbstractBlockSignalBlockEntity;
import chaos.unity.railroad_crossing.signal.common.block.entity.ISignalReceiver;
import chaos.unity.railroad_crossing.signal.common.block.entity.SignalBlockEntities;
import chaos.unity.railroad_crossing.signal.common.block.entity.SingleHeadSignalBlockEntity;
import chaos.unity.railroad_crossing.signal.common.data.SignalMode;
import chaos.unity.railroad_crossing.signal.common.item.SignalItems;
import chaos.unity.railroad_crossing.signal.common.world.IntervalData;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings({"deprecation"})
public class SingleHeadSignalBlock extends AbstractSignalBlock implements Waterloggable {
    protected static final VoxelShape COLLISION_SHAPE = VoxelShapes.cuboid(.25, .25, .25, .75, 1, .75);
    protected static final VoxelShape DEFAULT_SHAPE = VoxelShapes.cuboid(.25, .25, .25, .75, .75, .75);

    public SingleHeadSignalBlock() {
        super(FabricBlockSettings.of(Material.METAL));
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

                if (world.getBlockEntity(sbe.pairedSignalPos) instanceof AbstractBlockSignalBlockEntity pairedSignalBE) {
                    pairedSignalBE.pairedSignalPos = null;
                    pairedSignalBE.setSignalMode(SignalMode.BLINK_RED);
                }
            }
            if (sbe.receiverPos != null && world.getBlockEntity(sbe.receiverPos) instanceof ISignalReceiver receiver) {
                receiver.setEmitterPos(null);
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
