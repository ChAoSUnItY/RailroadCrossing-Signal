package chaos.unity.signal.common.block;

import chaos.unity.signal.common.itemgroup.SignalItemGroups;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class SignalBlocks {
    public static final SingleHeadSignalBlock SINGLE_HEAD_SIGNAL = new SingleHeadSignalBlock();

    public static void register() {
        Registry.register(Registry.BLOCK, new Identifier("signal", "single_head_signal"), SINGLE_HEAD_SIGNAL);
        Registry.register(Registry.ITEM, new Identifier("signal", "single_head_signal"), new BlockItem(SINGLE_HEAD_SIGNAL, new FabricItemSettings().group(SignalItemGroups.SIGNAL_ITEM_GROUP)));
    }
}
