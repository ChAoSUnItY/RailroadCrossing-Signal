package chaos.unity.signal.common.block;

import chaos.unity.signal.common.itemgroup.SignalItemGroups;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class SignalBlocks {
    public static final SignalBlock SIGNAL_BLOCK = new SignalBlock();

    public static void registerBlock() {
        Registry.register(Registry.BLOCK, new Identifier("signal", "signal_block"), SIGNAL_BLOCK);
        Registry.register(Registry.ITEM, new Identifier("signal", "signal_block"), new BlockItem(SIGNAL_BLOCK, new FabricItemSettings().group(SignalItemGroups.SIGNAL_ITEM_GROUP)));
    }
}
