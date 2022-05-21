package chaos.unity.signal.common.item;

import chaos.unity.signal.common.block.entity.ISignalEmitter;
import chaos.unity.signal.common.block.entity.ISignalReceiver;
import chaos.unity.signal.common.block.entity.SingleHeadSignalBlockEntity;
import chaos.unity.signal.common.itemgroup.SignalItemGroups;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class SignalTunerItem extends Item {
    public SignalTunerItem() {
        super(new FabricItemSettings().maxCount(1).group(SignalItemGroups.COMMON_ITEM_GROUP));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var stack = user.getStackInHand(hand);
        var nbt = stack.getOrCreateNbt();

        if (user.isSneaking() && nbt.contains("emitter_pos")) {
            /// Reset current session
            if (world.getBlockEntity(NbtHelper.toBlockPos(nbt.getCompound("emitter_pos"))) instanceof ISignalEmitter emitter) {
                emitter.endTuningSession(null);
            }
            nbt.remove("emitter_pos");

            if (world.isClient)
                user.sendMessage(new TranslatableText("chat.signal.tuning_terminated").formatted(Formatting.YELLOW), false);
        }
        return super.use(world, user, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var player = Objects.requireNonNull(context.getPlayer());
        var pos = context.getBlockPos();
        var world = context.getWorld();
        var nbt = context.getStack().getOrCreateNbt();
        var blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof ISignalReceiver receiver) {
            if (nbt.contains("emitter_pos")) {
                var emitterPos = NbtHelper.toBlockPos(nbt.getCompound("emitter_pos"));

                if (world.getBlockEntity(emitterPos) instanceof ISignalEmitter signalEmitter) {
                    // Complete current session
                    // Unbind all tuning relatives first to prevent collision
                    nbt.remove("emitter_pos");

                    signalEmitter.endTuningSession(pos);
                    receiver.setReceivingOwnerPos(emitterPos);

                    if (world.isClient)
                        player.sendMessage(new TranslatableText("chat.signal.tuning_success").formatted(Formatting.GREEN), false);
                } else {
                    // Invalid session: original signal emitter does not exist
                    if (world.isClient)
                        player.sendMessage(new TranslatableText("chat.signal.tuning_invalid.original_lost").formatted(Formatting.RED), false);
                }
            } else {
                // Invalid session: wrong tuning order, it must click on signal emitter first
                if (world.isClient)
                    player.sendMessage(new TranslatableText("chat.signal.tuning_invalid.wrong_order").formatted(Formatting.RED), false);
            }
        } else if (blockEntity instanceof ISignalEmitter emitter) {
            if (nbt.contains("emitter_pos")) {
                var originalPos = NbtHelper.toBlockPos(nbt.getCompound("emitter_pos"));

                if (originalPos.equals(pos)) {
                    // Reset current session
                    emitter.endTuningSession(null);
                    nbt.remove("emitter_pos");

                    if (world.isClient)
                        player.sendMessage(new TranslatableText("chat.signal.tuning_terminated").formatted(Formatting.YELLOW), false);
                } else {
                    // Abandon current session and create new session
                    nbt.put("emitter_pos", NbtHelper.fromBlockPos(pos));

                    if (world.isClient)
                        player.sendMessage(new TranslatableText("chat.signal.tuning_restart").formatted(Formatting.YELLOW), false);
                }
            } else {
                // Create a new session
                emitter.startTuningSession();
                nbt.put("emitter_pos", NbtHelper.fromBlockPos(pos));

                if (world.isClient)
                    player.sendMessage(new TranslatableText("chat.signal.tuning_start"), false);
            }
        }

        return super.useOnBlock(context);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableText("tooltip.signal.signal_surveyor.line1"));
        } else {
            tooltip.add(new TranslatableText("tooltip.signal.shift_tip").formatted(Formatting.GOLD));
        }
    }
}
