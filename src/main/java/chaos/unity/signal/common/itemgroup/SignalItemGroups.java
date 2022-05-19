package chaos.unity.signal.common.itemgroup;

import chaos.unity.signal.common.block.SignalBlocks;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public final class SignalItemGroups {
    public static final ItemGroup COMMON_ITEM_GROUP = FabricItemGroupBuilder.build(new Identifier("signal", "common"), () -> new ItemStack(SignalBlocks.SINGLE_HEAD_SIGNAL));
}
