package chaos.unity.railroad_crossing.signal;

import chaos.unity.railroad_crossing.signal.common.block.SignalBlocks;
import chaos.unity.railroad_crossing.signal.common.block.entity.SignalBlockEntities;
import chaos.unity.railroad_crossing.signal.common.item.SignalItems;
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
