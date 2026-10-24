package dev.nova.client;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Properties;

/** Persistent HUD layout and appearance settings. */
public final class NovaConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("novaclient/config");
    private static final int[] ACCENT_COLORS = {0xFF9B5CFF, 0xFF4CC9F0, 0xFF59D98E, 0xFFFFA94D};
    private static final String[] ACCENT_NAMES = {"Violet", "Cyan", "Green", "Orange"};

    private final Path file = FabricLoader.getInstance().getConfigDir().resolve("novaclient.properties");
    private final EnumMap<HudWidget, WidgetConfig> widgets = new EnumMap<>(HudWidget.class);
    private int opacity = 58;
    private int accentIndex;

    public NovaConfig() {
        for (HudWidget widget : HudWidget.values()) {
            widgets.put(widget, new WidgetConfig(widget.defaultX(), widget.defaultY()));
        }
    }

    public WidgetConfig widget(HudWidget widget) {
        return widgets.get(widget);
    }

    public int opacity() {
        return opacity;
    }

    public void cycleOpacity() {
        int[] values = {35, 58, 78, 92};
        int next = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == opacity) {
                next = (i + 1) % values.length;
                break;
            }
        }
        opacity = values[next];
    }

    public int accentColor() {
        return ACCENT_COLORS[accentIndex];
    }

    public String accentName() {
        return ACCENT_NAMES[accentIndex];
    }

    public void cycleAccent() {
        accentIndex = (accentIndex + 1) % ACCENT_COLORS.length;
    }

    public void load() {
        if (!Files.exists(file)) {
            return;
        }
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file)) {
            properties.load(reader);
        } catch (IOException | RuntimeException error) {
            LOGGER.warn("Could not read NovaClient settings", error);
            return;
        }

        opacity = parseInt(properties, "hud.opacity", opacity, 20, 95);
        accentIndex = parseInt(properties, "hud.accent", accentIndex, 0, ACCENT_COLORS.length - 1);
        for (HudWidget widget : HudWidget.values()) {
            WidgetConfig config = widget(widget);
            String prefix = "widget." + widget.configKey() + ".";
            config.enabled = Boolean.parseBoolean(properties.getProperty(prefix + "enabled", Boolean.toString(config.enabled)));
            config.x = parseInt(properties, prefix + "x", config.x, 0, 10000);
            config.y = parseInt(properties, prefix + "y", config.y, 0, 10000);
            config.scale = parseDouble(properties, prefix + "scale", config.scale, 0.75, 1.75);
        }
    }

    private static int parseInt(Properties properties, String key, int fallback, int min, int max) {
        try {
            return Math.max(min, Math.min(max, Integer.parseInt(properties.getProperty(key, Integer.toString(fallback)).trim())));
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private static double parseDouble(Properties properties, String key, double fallback, double min, double max) {
        try {
            return Math.max(min, Math.min(max, Double.parseDouble(properties.getProperty(key, Double.toString(fallback)).trim())));
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    public void resetLayout() {
        for (HudWidget widget : HudWidget.values()) {
            WidgetConfig config = widget(widget);
            config.x = widget.defaultX();
            config.y = widget.defaultY();
            config.scale = 1.0;
        }
    }

    public void save() {
        Properties properties = new Properties();
        properties.setProperty("hud.opacity", Integer.toString(opacity));
        properties.setProperty("hud.accent", Integer.toString(accentIndex));
        for (HudWidget widget : HudWidget.values()) {
            WidgetConfig config = widget(widget);
            String prefix = "widget." + widget.configKey() + ".";
            properties.setProperty(prefix + "enabled", Boolean.toString(config.enabled));
            properties.setProperty(prefix + "x", Integer.toString(config.x));
            properties.setProperty(prefix + "y", Integer.toString(config.y));
            properties.setProperty(prefix + "scale", Double.toString(config.scale));
        }
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                properties.store(writer, "NovaClient HUD settings");
            }
        } catch (IOException error) {
            LOGGER.warn("Could not save NovaClient settings", error);
        }
    }

    public static final class WidgetConfig {
        private boolean enabled = true;
        private int x;
        private int y;
        private double scale = 1.0;

        private WidgetConfig(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public boolean enabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int x() {
            return x;
        }

        public int y() {
            return y;
        }

        public void setPosition(int x, int y) {
            this.x = Math.max(0, x);
            this.y = Math.max(0, y);
        }

        public double scale() {
            return scale;
        }

        public void cycleScale() {
            scale += 0.25;
            if (scale > 1.75) {
                scale = 0.75;
            }
        }
    }
}