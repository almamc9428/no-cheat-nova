package dev.nova.client;

import dev.nova.client.gui.NovaMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public final class NovaClient implements ClientModInitializer {
    public static final String MOD_ID = "novaclient";
    public static final NovaConfig CONFIG = new NovaConfig();
    public static final ClickTracker CLICKS = new ClickTracker();
    private static final Identifier HUD_LAYER = Identifier.fromNamespaceAndPath(MOD_ID, "hud");

    @Override
    public void onInitializeClient() {
        CONFIG.load();

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));
        KeyMapping openMenu = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key." + MOD_ID + ".open_menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            CLICKS.tick(client);
            while (openMenu.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new NovaMenuScreen());
                }
            }
        });

        HudElementRegistry.attachElementAfter(VanillaHudElements.BOSS_BAR, HUD_LAYER, (graphics, tickCounter) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.screen == null && client.level != null) {
                HudRenderer.renderEnabled(graphics, client);
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> CONFIG.save());
    }
}