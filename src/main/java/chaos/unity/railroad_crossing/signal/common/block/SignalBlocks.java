package chaos.unity.railroad_crossing.signal.common.block;

import chaos.unity.railroad_crossing.signal.common.itemgroup.SignalItemGroups;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class SignalBlocks {
    public static final SingleHeadSignalBlock SINGLE_HEAD_SIGNAL = new SingleHeadSignalBlock();
    public static final DistantSingleHeadSignal DISTANT_SINGLE_HEAD_SIGNAL = new DistantSingleHeadSignal();
    public static final SignalBoxReceiverBlock SIGNAL_BOX_RECEIVER = new SignalBoxReceiverBlock();
    public static final SignalBoxEmitterBlock SIGNAL_BOX_EMITTER = new SignalBoxEmitterBlock();

    public static void register() {
        simple(SINGLE_HEAD_SIGNAL, new Identifier("rc_signal", "single_head_signal"));
        simple(DISTANT_SINGLE_HEAD_SIGNAL, new Identifier("rc_signal", "distant_single_head_signal"));
        simple(SIGNAL_BOX_RECEIVER, new Identifier("rc_signal", "signal_box_receiver"));
        simple(SIGNAL_BOX_EMITTER, new Identifier("rc_signal", "signal_box_emitter"));
    }

    private static <T extends Block> void simple(T block, Identifier id) {
        Registry.register(Registry.BLOCK, id, block);
        Registry.register(Registry.ITEM, id, new BlockItem(block, new FabricItemSettings().group(SignalItemGroups.COMMON_ITEM_GROUP)));
    }
}
