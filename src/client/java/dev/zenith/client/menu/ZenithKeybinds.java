package dev.zenith.client.menu;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class ZenithKeybinds {

    private static KeyMapping openMenuKey;

    private ZenithKeybinds() {
    }

    public static void register() {
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.zenith-client.open_menu",
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.zenith-client.general"
        ));
    }

    public static void tick(Minecraft client) {
        while (openMenuKey.consumeClick()) {
            if (client.screen == null) {
                client.setScreen(new ZenithMenuScreen());
            }
        }
    }
}