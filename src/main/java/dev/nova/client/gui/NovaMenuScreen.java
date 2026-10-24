package dev.nova.client.gui;

import dev.nova.client.HudWidget;
import dev.nova.client.NovaClient;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsPreset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class NovaMenuScreen extends Screen {
    private static final int PANEL_WIDTH = 390;
    private static final int PANEL_HEIGHT = 330;
    private Page page;

    public NovaMenuScreen() {
        this(Page.HUD);
    }

    private NovaMenuScreen(Page page) {
        super(Component.literal("NovaClient"));
        this.page = page;
    }

    @Override
    protected void init() {
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        addButton("HUD", left + 22, top + 44, 164, () -> changePage(Page.HUD), page == Page.HUD);
        addButton("PERFORMANCE", left + 204, top + 44, 164, () -> changePage(Page.PERFORMANCE), page == Page.PERFORMANCE);

        if (page == Page.HUD) {
            int y = top + 81;
            for (HudWidget widget : HudWidget.values()) {
                boolean enabled = NovaClient.CONFIG.widget(widget).enabled();
                addButton(widget.displayName() + "  •  " + (enabled ? "ON" : "OFF"), left + 24, y, 342,
                    () -> toggleWidget(widget), false);
                y += 26;
            }
            addButton("Edit HUD layout", left + 24, y + 1, 342, () -> minecraft.setScreen(new HudEditorScreen(this)), false);
            addButton("Panel opacity: " + NovaClient.CONFIG.opacity() + "%", left + 24, y + 27, 166,
                () -> { NovaClient.CONFIG.cycleOpacity(); saveAndRefresh(); }, false);
            addButton("Accent: " + NovaClient.CONFIG.accentName(), left + 200, y + 27, 166,
                () -> { NovaClient.CONFIG.cycleAccent(); saveAndRefresh(); }, false);
        } else {
            int y = top + 81;
            addButton("Apply " + presetName() + " preset", left + 24, y, 342, this::togglePreset, false);
            y += 29;
            addButton("Render distance: " + minecraft.options.renderDistance().get() + " chunks", left + 24, y, 258,
                () -> {}, false);
            addButton("−", left + 290, y, 34, () -> changeRenderDistance(-2), false);
            addButton("+", left + 332, y, 34, () -> changeRenderDistance(2), false);
            y += 27;
            addButton("Simulation distance: " + minecraft.options.simulationDistance().get() + " chunks", left + 24, y, 258,
                () -> {}, false);
            addButton("−", left + 290, y, 34, () -> changeSimulationDistance(-1), false);
            addButton("+", left + 332, y, 34, () -> changeSimulationDistance(1), false);
            y += 27;
            addButton("Particles: " + minecraft.options.particles().get().name(), left + 24, y, 342,
                this::cycleParticles, false);
            y += 27;
            addButton("Clouds: " + minecraft.options.cloudStatus().get().name(), left + 24, y, 342,
                this::cycleClouds, false);
            y += 27;
            addButton("Entity shadows: " + (minecraft.options.entityShadows().get() ? "ON" : "OFF"), left + 24, y, 342,
                this::toggleShadows, false);
        }
        addButton("Done", left + 24, top + PANEL_HEIGHT - 31, 342, this::onClose, false);
    }

    private void addButton(String label, int x, int y, int buttonWidth, Runnable action, boolean selected) {
        Button button = Button.builder(Component.literal(label), clicked -> action.run())
            .bounds(x, y, buttonWidth, 20).build();
        if (selected) {
            button.setMessage(Component.literal("• " + label));
        }
        addRenderableWidget(button);
    }

    private void changePage(Page target) {
        minecraft.setScreen(new NovaMenuScreen(target));
    }

    private void toggleWidget(HudWidget widget) {
        var config = NovaClient.CONFIG.widget(widget);
        config.setEnabled(!config.enabled());
        saveAndRefresh();
    }

    private void saveAndRefresh() {
        NovaClient.CONFIG.save();
        minecraft.setScreen(new NovaMenuScreen(page));
    }

    private void changeRenderDistance(int delta) {
        var option = minecraft.options.renderDistance();
        option.set(Math.max(2, Math.min(32, option.get() + delta)));
        minecraft.options.save();
        refreshHere();
    }

    private void changeSimulationDistance(int delta) {
        var option = minecraft.options.simulationDistance();
        option.set(Math.max(5, Math.min(32, option.get() + delta)));
        minecraft.options.save();
        refreshHere();
    }

    private void cycleParticles() {
        ParticleStatus[] values = ParticleStatus.values();
        ParticleStatus current = minecraft.options.particles().get();
        minecraft.options.particles().set(values[(current.ordinal() + 1) % values.length]);
        minecraft.options.save();
        refreshHere();
    }

    private void cycleClouds() {
        CloudStatus[] values = CloudStatus.values();
        CloudStatus current = minecraft.options.cloudStatus().get();
        minecraft.options.cloudStatus().set(values[(current.ordinal() + 1) % values.length]);
        minecraft.options.save();
        refreshHere();
    }

    private void toggleShadows() {
        var option = minecraft.options.entityShadows();
        option.set(!option.get());
        minecraft.options.save();
        refreshHere();
    }

    private void togglePreset() {
        if ("Performance".equals(presetName())) {
            applyBalancedPreset();
        } else {
            applyPerformancePreset();
        }
        minecraft.options.save();
        refreshHere();
    }

    private String presetName() {
        return minecraft.options.renderDistance().get() <= 8
            && minecraft.options.simulationDistance().get() <= 6
            && minecraft.options.particles().get() == ParticleStatus.MINIMAL
            && minecraft.options.cloudStatus().get() == CloudStatus.OFF
            ? "Performance" : "Balanced";
    }

    private void applyPerformancePreset() {
        minecraft.options.applyGraphicsPreset(GraphicsPreset.FAST);
        minecraft.options.renderDistance().set(8);
        minecraft.options.simulationDistance().set(5);
        minecraft.options.particles().set(ParticleStatus.MINIMAL);
        minecraft.options.cloudStatus().set(CloudStatus.OFF);
        minecraft.options.entityShadows().set(false);
    }

    private void applyBalancedPreset() {
        minecraft.options.renderDistance().set(10);
        minecraft.options.simulationDistance().set(8);
        minecraft.options.particles().set(ParticleStatus.DECREASED);
        minecraft.options.cloudStatus().set(CloudStatus.FAST);
        minecraft.options.entityShadows().set(false);
    }

    private void refreshHere() {
        minecraft.setScreen(new NovaMenuScreen(page));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xA9000000);
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        graphics.fill(left - 1, top - 1, left + PANEL_WIDTH + 1, top + PANEL_HEIGHT + 1, NovaClient.CONFIG.accentColor());
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xF20D1018);
        graphics.fill(left, top, left + PANEL_WIDTH, top + 3, NovaClient.CONFIG.accentColor());
        graphics.drawString(font, "NOVACLIENT", left + 22, top + 15, 0xFFF4F0FF, false);
        graphics.drawString(font, "HUD + PERFORMANCE", left + 22, top + 28, 0xFFBEB4D5, false);
        graphics.drawString(font, "Right Shift opens this menu  •  Drag widgets in HUD editor",
            left + 22, top + PANEL_HEIGHT - 48, 0xFF9C95AA, false);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private enum Page {
        HUD,
        PERFORMANCE
    }
}