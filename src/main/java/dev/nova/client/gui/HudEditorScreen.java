package dev.nova.client.gui;

import dev.nova.client.HudRenderer;
import dev.nova.client.HudWidget;
import dev.nova.client.NovaClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class HudEditorScreen extends Screen {
    private final Screen parent;
    private HudWidget selected = HudWidget.FPS;
    private HudWidget dragging;
    private double dragOffsetX;
    private double dragOffsetY;

    public HudEditorScreen(Screen parent) {
        super(Component.literal("NovaClient HUD Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonY = height - 30;
        addRenderableWidget(Button.builder(Component.literal("Reset positions"), button -> {
            NovaClient.CONFIG.resetLayout();
            NovaClient.CONFIG.save();
        }).bounds(width / 2 - 166, buttonY, 104, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Scale selected: " + scaleLabel()), button -> {
            NovaClient.CONFIG.widget(selected).cycleScale();
            NovaClient.CONFIG.save();
            button.setMessage(Component.literal("Scale selected: " + scaleLabel()));
        }).bounds(width / 2 - 54, buttonY, 128, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
            .bounds(width / 2 + 80, buttonY, 86, 20).build());
    }

    private String scaleLabel() {
        return Math.round(NovaClient.CONFIG.widget(selected).scale() * 100) + "%";
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xA9000000);
        graphics.fill(12, 12, width - 12, 53, 0xE7111420);
        graphics.fill(12, 12, width - 12, 14, NovaClient.CONFIG.accentColor());
        graphics.drawString(font, "EDIT HUD LAYOUT", 24, 24, 0xFFF4F0FF, false);
        graphics.drawString(font, "Drag widgets to move them. Toggle and style each widget from the NovaClient HUD menu.",
            24, 38, 0xFFBEB4D5, false);

        for (HudWidget widget : HudWidget.values()) {
            HudRenderer.renderWidget(graphics, Minecraft.getInstance(), widget, true);
            HudRenderer.Bounds bounds = HudRenderer.bounds(Minecraft.getInstance(), widget);
            if (widget == selected) {
                graphics.fill(bounds.x() - 1, bounds.y() - 1, bounds.x() + bounds.width() + 1, bounds.y() + 1,
                    NovaClient.CONFIG.accentColor());
                graphics.fill(bounds.x() - 1, bounds.y() + bounds.height() - 1,
                    bounds.x() + bounds.width() + 1, bounds.y() + bounds.height() + 1, NovaClient.CONFIG.accentColor());
                graphics.fill(bounds.x() - 1, bounds.y(), bounds.x() + 1, bounds.y() + bounds.height(), NovaClient.CONFIG.accentColor());
                graphics.fill(bounds.x() + bounds.width() - 1, bounds.y(),
                    bounds.x() + bounds.width() + 1, bounds.y() + bounds.height(), NovaClient.CONFIG.accentColor());
            }
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            for (HudWidget widget : HudWidget.values()) {
                HudRenderer.Bounds bounds = HudRenderer.bounds(Minecraft.getInstance(), widget);
                if (bounds.contains(event.x(), event.y())) {
                    selected = widget;
                    dragging = widget;
                    dragOffsetX = event.x() - bounds.x();
                    dragOffsetY = event.y() - bounds.y();
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (event.button() == 0 && dragging != null) {
            HudRenderer.Bounds bounds = HudRenderer.bounds(Minecraft.getInstance(), dragging);
            int x = (int) Math.round(event.x() - dragOffsetX);
            int y = (int) Math.round(event.y() - dragOffsetY);
            x = Math.max(0, Math.min(width - bounds.width(), x));
            y = Math.max(58, Math.min(height - 36 - bounds.height(), y));
            NovaClient.CONFIG.widget(dragging).setPosition(x, y);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && dragging != null) {
            dragging = null;
            NovaClient.CONFIG.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public void onClose() {
        NovaClient.CONFIG.save();
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}