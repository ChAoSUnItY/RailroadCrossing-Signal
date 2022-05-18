package chaos.unity.signal;

import chaos.unity.signal.common.block.SignalBlocks;
import chaos.unity.signal.common.blockentity.SignalBlockEntities;
import chaos.unity.signal.common.item.SignalItems;
import net.fabricmc.api.ModInitializer;

public class Signal implements ModInitializer {
    @Override
    public void onInitialize() {
        SignalItems.register();
        SignalBlocks.register();
        SignalBlockEntities.register();
        SignalNetworking.register();
    }
}
