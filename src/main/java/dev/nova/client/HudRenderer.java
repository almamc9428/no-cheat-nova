package dev.nova.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;

/** Draws the movable HUD cards and their edit-mode previews. */
public final class HudRenderer {
    private static final int PAD_X = 5;
    private static final int PAD_Y = 4;
    private static final int LINE_HEIGHT = 11;
    private static final int KEY_SIZE = 21;
    private static final int KEY_GAP = 3;

    private HudRenderer() {
    }

    public static void renderEnabled(GuiGraphics graphics, Minecraft client) {
        for (HudWidget widget : HudWidget.values()) {
            if (NovaClient.CONFIG.widget(widget).enabled()) {
                renderWidget(graphics, client, widget, false);
            }
        }
    }

    public static void renderWidget(GuiGraphics graphics, Minecraft client, HudWidget widget, boolean preview) {
        NovaConfig.WidgetConfig config = NovaClient.CONFIG.widget(widget);
        if (!preview && !config.enabled()) {
            return;
        }
        int x = config.x();
        int y = config.y();
        double scale = config.scale();
        int[] baseSize = baseSize(client, widget);
        int w = (int) Math.ceil(baseSize[0] * scale);
        int h = (int) Math.ceil(baseSize[1] * scale);
        int panelAlpha = (int) Math.round(NovaClient.CONFIG.opacity() * 2.55);
        if (preview && !config.enabled()) {
            panelAlpha = panelAlpha / 2;
        }
        int background = (panelAlpha << 24) | 0x0010121C;
        int colorAlpha = preview && !config.enabled() ? 0x80 : 0xFF;

        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale((float) scale, (float) scale);
        graphics.fill(0, 0, baseSize[0], baseSize[1], background);
        graphics.fill(0, 0, 2, baseSize[1], NovaClient.CONFIG.accentColor());

        if (widget == HudWidget.KEYSTROKES) {
            drawKeystrokes(graphics, client, colorAlpha);
        } else {
            graphics.drawString(client.font, widgetText(client, widget), PAD_X, PAD_Y,
                colorAlpha << 24 | 0x00F4F0FF, false);
        }
        graphics.pose().popMatrix();
    }

    public static Bounds bounds(Minecraft client, HudWidget widget) {
        NovaConfig.WidgetConfig config = NovaClient.CONFIG.widget(widget);
        int[] size = baseSize(client, widget);
        return new Bounds(config.x(), config.y(), (int) Math.ceil(size[0] * config.scale()),
            (int) Math.ceil(size[1] * config.scale()));
    }

    private static int[] baseSize(Minecraft client, HudWidget widget) {
        if (widget == HudWidget.KEYSTROKES) {
            return new int[]{PAD_X * 2 + KEY_SIZE * 4 + KEY_GAP * 3, PAD_Y * 2 + KEY_SIZE * 3 + KEY_GAP * 2};
        }
        String text = widgetText(client, widget);
        return new int[]{Math.max(54, client.font.width(text) + PAD_X * 2 + 2), LINE_HEIGHT + PAD_Y * 2};
    }

    private static String widgetText(Minecraft client, HudWidget widget) {
        LocalPlayer player = client.player;
        return switch (widget) {
            case FPS -> "FPS: " + client.getFps();
            case CPS -> "LMB " + NovaClient.CLICKS.leftCps() + "   RMB " + NovaClient.CLICKS.rightCps();
            case COORDINATES -> player == null ? "XYZ: -- / -- / --" : String.format(java.util.Locale.ROOT,
                "XYZ: %.1f / %.1f / %.1f", player.getX(), player.getY(), player.getZ());
            case PING -> "Ping: " + pingText(client);
            case KEYSTROKES -> "";
        };
    }

    private static String pingText(Minecraft client) {
        if (client.player == null || client.getConnection() == null) {
            return "--";
        }
        var info = client.getConnection().getPlayerInfo(client.player.getUUID());
        return info == null ? "--" : info.getLatency() + " ms";
    }

    private static void drawKeystrokes(GuiGraphics graphics, Minecraft client, int colorAlpha) {
        int x = PAD_X + 3;
        int y = PAD_Y + 2;
        drawKey(graphics, "W", x + KEY_SIZE + KEY_GAP, y, client.options.keyUp.isDown(), colorAlpha);
        drawKey(graphics, "A", x, y + KEY_SIZE + KEY_GAP, client.options.keyLeft.isDown(), colorAlpha);
        drawKey(graphics, "S", x + KEY_SIZE + KEY_GAP, y + KEY_SIZE + KEY_GAP, client.options.keyDown.isDown(), colorAlpha);
        drawKey(graphics, "D", x + (KEY_SIZE + KEY_GAP) * 2, y + KEY_SIZE + KEY_GAP, client.options.keyRight.isDown(), colorAlpha);
        drawKey(graphics, "SHIFT", x, y + (KEY_SIZE + KEY_GAP) * 2,
            client.options.keyShift.isDown(), colorAlpha, KEY_SIZE * 2 + KEY_GAP);
        drawKey(graphics, "SPACE", x + KEY_SIZE * 2 + KEY_GAP * 2, y + (KEY_SIZE + KEY_GAP) * 2,
            client.options.keyJump.isDown(), colorAlpha, KEY_SIZE * 2 + KEY_GAP);
    }

    private static void drawKey(GuiGraphics graphics, String label, int x, int y, boolean down, int colorAlpha) {
        drawKey(graphics, label, x, y, down, colorAlpha, KEY_SIZE);
    }

    private static void drawKey(GuiGraphics graphics, String label, int x, int y, boolean down,
                                int colorAlpha, int width) {
        int fill = down ? NovaClient.CONFIG.accentColor() : 0xFF272A37;
        if (colorAlpha != 0xFF) {
            fill = (0x80 << 24) | (fill & 0x00FFFFFF);
        }
        graphics.fill(x, y, x + width, y + KEY_SIZE, fill);
        graphics.drawCenteredString(Minecraft.getInstance().font, label, x + width / 2,
            y + (KEY_SIZE - 8) / 2, colorAlpha << 24 | 0x00FFFFFF);
    }

    public record Bounds(int x, int y, int width, int height) {
        public boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
    }
}