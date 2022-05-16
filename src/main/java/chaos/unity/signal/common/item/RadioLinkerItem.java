package chaos.unity.signal.common.item;

import chaos.unity.signal.common.block.SignalBlock;
import chaos.unity.signal.common.blockentity.SignalBlockEntity;
import chaos.unity.signal.common.itemgroup.SignalItemGroups;
import chaos.unity.signal.common.util.Utils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

import java.util.Objects;

public class RadioLinkerItem extends Item {
    public RadioLinkerItem() {
        super(new FabricItemSettings().group(SignalItemGroups.SIGNAL_ITEM_GROUP).maxCount(1));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var player = Objects.requireNonNull(context.getPlayer());
        var pos = context.getBlockPos();
        var world = context.getWorld();
        var blockState = world.getBlockState(pos);
        var nbt = context.getStack().getOrCreateNbt();

        if (blockState.getBlock() instanceof AbstractRailBlock) {
            if (nbt.contains("signal_bind_pos", NbtElement.INT_ARRAY_TYPE)) {
                // Complete current session
                var signalBindPos = Utils.fromIntArray(nbt.getIntArray("signal_bind_pos"));
                var blockEntity = world.getBlockEntity(signalBindPos);

                if (blockEntity instanceof SignalBlockEntity sbe) {
                    sbe.railBindPos = pos;
                    // Reset session
                    nbt.remove("signal_bind_pos");
                    nbt.remove("rail_bind_pos");

                    if (world.isClient)
                        player.sendMessage(new LiteralText("Successfully bind rail to signal block"), false);
                } else {
                    // Abandon current session and create new session since latest session info is incomplete
                    nbt.remove("signal_bind_pos");
                    nbt.putIntArray("rail_bind_pos", Utils.asIntArray(pos));

                    if (world.isClient)
                        context.getPlayer().sendMessage(new LiteralText("Starts a new binding session (previous session abandoned)").formatted(Formatting.YELLOW), false);
                }
            } else if (nbt.contains("rail_bind_pos")) {
                var railBindPos = Utils.fromIntArray(nbt.getIntArray("rail_bind_pos"));

                if (railBindPos.equals(pos)) {
                    // Reset current session
                    nbt.remove("rail_bind_pos");

                    if (world.isClient)
                        player.sendMessage(new LiteralText("Current binding session terminated"), false);
                } else if (!(world.getBlockState(railBindPos).getBlock() instanceof AbstractRailBlock)) {
                    // Abandon current session and create new session since original block is not rail anymore
                    nbt.putIntArray("signal_bind_pos", Utils.asIntArray(pos));

                    if (world.isClient)
                        context.getPlayer().sendMessage(new LiteralText("Starts a new binding session (previous session abandoned)").formatted(Formatting.YELLOW), false);
                } else {
                    // Current session is not ended
                    if (world.isClient)
                        player.sendMessage(new LiteralText("Current binding session is not ended").formatted(Formatting.RED), false);
                }
            } else {
                // Create a new session
                nbt.putIntArray("rail_bind_pos", Utils.asIntArray(pos));
                if (world.isClient)
                    context.getPlayer().sendMessage(new LiteralText("Starts a new binding session"), false);
            }

            return ActionResult.SUCCESS;
        } else if (blockState.getBlock() instanceof SignalBlock) {
            if (nbt.contains("rail_bind_pos", NbtElement.INT_ARRAY_TYPE)) {
                // Complete current session
                var railBindPos = Utils.fromIntArray(nbt.getIntArray("rail_bind_pos"));
                var blockEntity = world.getBlockEntity(pos);

                if (blockEntity instanceof SignalBlockEntity sbe) {
                    sbe.railBindPos = railBindPos;
                    // Reset session
                    nbt.remove("signal_bind_pos");
                    nbt.remove("rail_bind_pos");

                    if (world.isClient)
                        player.sendMessage(new LiteralText("Successfully bind rail to signal block"), false);
                } else {
                    // Abandon current session and create new session since latest session info is incomplete
                    nbt.putIntArray("signal_bind_pos", Utils.asIntArray(pos));
                    nbt.remove("rail_bind_pos");

                    if (world.isClient)
                        context.getPlayer().sendMessage(new LiteralText("Starts a new binding session (previous session abandoned)").formatted(Formatting.YELLOW), false);
                }
            } else if (nbt.contains("signal_bind_pos")) {
                var signalBindPos = Utils.fromIntArray(nbt.getIntArray("signal_bind_pos"));

                if (signalBindPos.equals(pos)) {
                    // Reset current session
                    nbt.remove("signal_bind_pos");

                    if (world.isClient)
                        player.sendMessage(new LiteralText("Current binding session terminated"), false);
                } else if (!(world.getBlockState(signalBindPos).getBlock() instanceof SignalBlock)) {
                    // Abandon current session and create new session since original block is not rail anymore
                    nbt.putIntArray("signal_bind_pos", Utils.asIntArray(pos));

                    if (world.isClient)
                        context.getPlayer().sendMessage(new LiteralText("Starts a new binding session (previous session abandoned)").formatted(Formatting.YELLOW), false);
                } else {
                    // Current session is not ended
                    if (world.isClient)
                        player.sendMessage(new LiteralText("Current binding session is not ended").formatted(Formatting.RED), false);
                }
            } else {
                // Create a new session
                nbt.putIntArray("signal_bind_pos", Utils.asIntArray(pos));
                if (world.isClient)
                    context.getPlayer().sendMessage(new LiteralText("Starts a new binding session"), false);
            }

            return ActionResult.SUCCESS;
        }
        return super.useOnBlock(context);
    }
}
