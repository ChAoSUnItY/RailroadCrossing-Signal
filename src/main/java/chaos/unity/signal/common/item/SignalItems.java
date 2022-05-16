package chaos.unity.signal.common.item;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class SignalItems {
    public static final RadioLinkerItem RADIO_LINKER_ITEM = new RadioLinkerItem();

    public static void register() {
        Registry.register(Registry.ITEM, new Identifier("signal", "radio_linker"), RADIO_LINKER_ITEM);
    }
}
