package chaos.unity.signal.common.data;

public enum SignalMode {
    BLINK_RED,
    GREEN;

    public static final SignalMode[] values = values();

    public boolean isBlink() {
        return this == BLINK_RED;
    }
}
