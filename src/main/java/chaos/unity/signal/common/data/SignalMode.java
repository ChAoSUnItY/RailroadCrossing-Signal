package chaos.unity.signal.common.data;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Locale;

public enum SignalMode {
    BLINK_RED(Color.RED),
    RED(Color.RED),
    BLINK_YELLOW(Color.YELLOW),
    YELLOW(Color.YELLOW),
    GREEN(Color.GREEN);

    public static final SignalMode[] values = values();
    public final Color color;

    SignalMode(Color color) {
        this.color = color;
    }

    public boolean isBlink() {
        return this == BLINK_RED || this == BLINK_YELLOW;
    }

    public @NotNull String getTranslationKey() {
        return "component.signal." + name().toLowerCase(Locale.ROOT);
    }
}
