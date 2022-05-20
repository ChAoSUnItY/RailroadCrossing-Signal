package chaos.unity.signal.common.item;

import chaos.unity.signal.common.block.entity.ISignalEmitter;
import chaos.unity.signal.common.block.entity.ISignalReceiver;
import chaos.unity.signal.common.itemgroup.SignalItemGroups;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class SignalTunerItem extends Item {
    public SignalTunerItem() {
        super(new FabricItemSettings().maxCount(1).group(SignalItemGroups.COMMON_ITEM_GROUP));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var player = Objects.requireNonNull(context.getPlayer());
        var pos = context.getBlockPos();
        var world = context.getWorld();
        var nbt = context.getStack().getOrCreateNbt();
        BlockEntity blockEntity = world.getBlockEntity(pos);

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
                        player.sendMessage(new LiteralText("Successfully complete tuning session").formatted(Formatting.GREEN), false);
                } else {
                    // Invalid session: original signal emitter does not exist
                    if (world.isClient)
                        player.sendMessage(new LiteralText("Invalid tuning session: original signal emitter does not exist").formatted(Formatting.RED), false);
                }
            } else {
                // Invalid session: wrong tuning order, it must click on signal emitter first
                if (world.isClient)
                    player.sendMessage(new LiteralText("Invalid tuning session: wrong tuning order").formatted(Formatting.RED), false);
            }
        } else if (blockEntity instanceof ISignalEmitter emitter) {
            if (nbt.contains("emitter_pos")) {
                var originalPos = NbtHelper.toBlockPos(nbt.getCompound("emitter_pos"));

                if (originalPos.equals(pos)) {
                    // Reset current session
                    emitter.endTuningSession(null);
                    nbt.remove("emitter_pos");

                    if (world.isClient)
                        player.sendMessage(new LiteralText("Current tuning session terminated").formatted(Formatting.YELLOW), false);
                } else {
                    // Abandon current session and create new session
                    nbt.put("emitter_pos", NbtHelper.fromBlockPos(pos));

                    if (world.isClient)
                        player.sendMessage(new LiteralText("Starts a new tuning session (previous session abandoned)").formatted(Formatting.YELLOW), false);
                }
            } else {
                // Create a new session
                emitter.startTuningSession();
                nbt.put("emitter_pos", NbtHelper.fromBlockPos(pos));

                if (world.isClient)
                    player.sendMessage(new LiteralText("Starts a new tuning session"), false);
            }
        }

        return super.useOnBlock(context);
    }
}
