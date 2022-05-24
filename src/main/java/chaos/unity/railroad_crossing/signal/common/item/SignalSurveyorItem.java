package chaos.unity.railroad_crossing.signal.common.item;

import chaos.unity.railroad_crossing.signal.common.block.entity.SingleHeadSignalBlockEntity;
import chaos.unity.railroad_crossing.signal.common.data.Interval;
import chaos.unity.railroad_crossing.signal.common.itemgroup.SignalItemGroups;
import chaos.unity.railroad_crossing.signal.common.world.IntervalData;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class SignalSurveyorItem extends Item {
    public SignalSurveyorItem() {
        super(new FabricItemSettings().group(SignalItemGroups.COMMON_ITEM_GROUP).maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var stack = user.getStackInHand(hand);
        var nbt = stack.getOrCreateNbt();

        if (user.isSneaking() && nbt.contains("signal_pos")) {
            /// Reset current session
            if (world.getBlockEntity(NbtHelper.toBlockPos(nbt.getCompound("signal_pos"))) instanceof SingleHeadSignalBlockEntity originalSignalBE) {
                originalSignalBE.endSurveySession(null);
            }
            nbt.remove("signal_pos");

            if (world.isClient)
                user.sendMessage(new TranslatableText("chat.rc_signal.survey_terminated").formatted(Formatting.YELLOW), false);
        }
        return super.use(world, user, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var player = Objects.requireNonNull(context.getPlayer());
        var pos = context.getBlockPos();
        var world = context.getWorld();
        var nbt = context.getStack().getOrCreateNbt();

        if (world.getBlockEntity(pos) instanceof SingleHeadSignalBlockEntity currentSignalBE) {
            if (nbt.contains("signal_pos")) {
                var originalPos = NbtHelper.toBlockPos(nbt.getCompound("signal_pos"));

                if (world.getBlockEntity(originalPos) instanceof SingleHeadSignalBlockEntity originalSignalBE) {
                    if (originalPos.equals(pos)) {
                        // Reset current session
                        originalSignalBE.endSurveySession(null);
                        nbt.remove("signal_pos");

                        if (world.isClient)
                            player.sendMessage(new TranslatableText("chat.rc_signal.survey_terminated").formatted(Formatting.YELLOW), false);
                    } else {
                        if (!originalSignalBE.hasRail()) {
                            if (world.isClient)
                                player.sendMessage(new TranslatableText("chat.rc_signal.survey_invalid.previous_unbound").formatted(Formatting.RED), false);
                        } else if (!currentSignalBE.hasRail()) {
                            if (world.isClient)
                                player.sendMessage(new TranslatableText("chat.rc_signal.survey_invalid.current_unbound").formatted(Formatting.RED), false);
                        } else {
                            var interval = Interval.getInterval(world, originalSignalBE, currentSignalBE);

                            if (interval == null) {
                                // Invalid interval
                                if (world.isClient)
                                    player.sendMessage(new TranslatableText("chat.rc_signal.survey_invalid.invalid_signal_block").formatted(Formatting.RED), false);
                            } else {
                                // Complete session
                                if (world instanceof ServerWorld serverWorld) {
                                    // Capture signals' ownership and try to deregister their original interval instance
                                    var intervalData = IntervalData.getOrCreate(serverWorld);
                                    Interval removedSignal;

                                    if ((removedSignal = intervalData.removeBySignal(originalPos)) != null) {
                                        removedSignal.unbindAllRelatives(serverWorld);
                                    }

                                    if ((removedSignal = intervalData.removeBySignal(pos)) != null) {
                                        removedSignal.unbindAllRelatives(serverWorld);
                                    }
                                }

                                originalSignalBE.endSurveySession(pos);
                                currentSignalBE.endSurveySession(originalPos);

                                if (world instanceof ServerWorld serverWorld) {
                                    // Register interval on server side only
                                    var intervalData = IntervalData.getOrCreate(serverWorld);
                                    intervalData.addInterval(interval);
                                    intervalData.markDirty();
                                }

                                if (world.isClient)
                                    player.sendMessage(new TranslatableText("chat.rc_signal.survey_success").formatted(Formatting.GREEN), false);
                            }
                        }

                        // Reset current session
                        nbt.remove("signal_pos");
                    }
                } else {
                    // Abandon current session and create new session since original block is not signal anymore
                    nbt.put("signal_pos", NbtHelper.fromBlockPos(pos));

                    if (world.isClient)
                        player.sendMessage(new TranslatableText("chat.rc_signal.survey_restart").formatted(Formatting.YELLOW), false);
                }
            } else {
                // Create a new session
                currentSignalBE.startSurveySession();
                nbt.put("signal_pos", NbtHelper.fromBlockPos(pos));

                if (world.isClient)
                    player.sendMessage(new TranslatableText("chat.rc_signal.survey_start"), false);
            }

            return ActionResult.SUCCESS;
        }
        return super.useOnBlock(context);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableText("tooltip.rc_signal.signal_surveyor.line1"));
        } else {
            tooltip.add(new TranslatableText("tooltip.rc_signal.shift_tip").formatted(Formatting.GOLD));
        }
    }
}
