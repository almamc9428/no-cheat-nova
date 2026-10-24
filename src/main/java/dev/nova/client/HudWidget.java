package dev.nova.client;

/** The configurable, movable pieces of the on-screen HUD. */
public enum HudWidget {
    FPS("FPS", 10, 10),
    CPS("CPS", 10, 29),
    COORDINATES("Coordinates", 10, 48),
    PING("Ping", 10, 67),
    KEYSTROKES("Keystrokes", 10, 90);

    private final String displayName;
    private final int defaultX;
    private final int defaultY;

    HudWidget(String displayName, int defaultX, int defaultY) {
        this.displayName = displayName;
        this.defaultX = defaultX;
        this.defaultY = defaultY;
    }

    public String displayName() {
        return displayName;
    }

    public int defaultX() {
        return defaultX;
    }

    public int defaultY() {
        return defaultY;
    }

    public String configKey() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}