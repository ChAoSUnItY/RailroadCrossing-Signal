package chaos.unity.signal.common.item;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class SignalItems {
    public static final SignalSurveyorItem SIGNAL_SURVEYOR_ITEM = new SignalSurveyorItem();
    public static final SignalTunerItem SIGNAL_TUNER_ITEM = new SignalTunerItem();

    public static void register() {
        Registry.register(Registry.ITEM, new Identifier("rc_signal", "signal_surveyor"), SIGNAL_SURVEYOR_ITEM);
        Registry.register(Registry.ITEM, new Identifier("rc_signal", "signal_tuner"), SIGNAL_TUNER_ITEM);
    }
}
