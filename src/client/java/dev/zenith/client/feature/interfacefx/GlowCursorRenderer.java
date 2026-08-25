package dev.zenith.client.feature.interfacefx;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

final class GlowCursorRenderer {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("zenith-client", "textures/gui/sprites/cursor_glow.png");

    private static final int TEXTURE_SIZE = 64;

    private static final int CORE_SIZE = 15;
    private static final int HALO_SIZE = 20;
    private static final int HALO_ALPHA = 90;

    private static final ResourceLocation SPARK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("zenith-client", "textures/gui/sprites/spark.png");
    private static final int SPARK_TEXTURE_SIZE = 32;

    void draw(GuiGraphics context, int mouseX, int mouseY) {
        drawLayer(context, mouseX, mouseY, HALO_SIZE, HALO_ALPHA);
        drawLayer(context, mouseX, mouseY, CORE_SIZE, 255);
    }

    private static void drawLayer(GuiGraphics context, int x, int y, int size, int alpha) {
        if (alpha <= 0 || size <= 0) {
            return;
        }
        int argbColor = (alpha << 24) | 0xFFFFFF;
        int half = size / 2;
        context.blit(
                RenderPipelines.GUI_TEXTURED,
                SPARK_TEXTURE,
                x - half, y - half,
                0f, 0f,
                size, size,
                SPARK_TEXTURE_SIZE, SPARK_TEXTURE_SIZE,
                SPARK_TEXTURE_SIZE, SPARK_TEXTURE_SIZE,
                argbColor
        );
    }
}