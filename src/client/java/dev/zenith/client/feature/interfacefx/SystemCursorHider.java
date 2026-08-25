package dev.zenith.client.feature.interfacefx;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

final class SystemCursorHider {

    private boolean hidden = false;

    void hide() {
        if (hidden) {
            return;
        }
        long windowHandle = Minecraft.getInstance().getWindow().getWindow();
        GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
        hidden = true;
    }

    void restore() {
        if (!hidden) {
            return;
        }
        long windowHandle = Minecraft.getInstance().getWindow().getWindow();
        GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        hidden = false;
    }
}