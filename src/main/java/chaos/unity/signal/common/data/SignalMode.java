package chaos.unity.signal.common.data;

import java.awt.*;

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
}
