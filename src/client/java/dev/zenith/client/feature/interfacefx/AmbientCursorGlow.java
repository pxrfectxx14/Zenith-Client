package dev.zenith.client.feature.interfacefx;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

/**
 * Большое, очень тусклое пятно света, которое плавно (с отставанием)
 * следует за курсором — создаёт ощущение, что курсор подсвечивает
 * пространство вокруг себя, а не просто оставляет след из частиц.
 *
 * Отдельный класс, потому что у этого эффекта своя логика жизненного цикла:
 * он не рождается и не гаснет, как искры, а один-единственный экземпляр
 * плавно "плывёт" к позиции мыши каждый кадр.
 */
final class AmbientCursorGlow {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("zenith-client", "textures/gui/sprites/spark.png");
    private static final int TEXTURE_SIZE = 32;

    // Насколько большое само пятно света на экране.
    private static final int GLOW_SIZE = 150;

    // Насколько оно тусклое (из 255) — должно быть еле заметным, не перебивать след.
    private static final int GLOW_ALPHA = 18;

    // Скорость "подтягивания" к курсору: 0.0 — вообще не двигается, 1.0 — телепортация без отставания.
    // Маленькое значение даёт эффект плавного, немного инертного свечения.
    private static final double FOLLOW_SPEED = 0.30;

    private double x = Double.NaN;
    private double y = Double.NaN;

    void update(int mouseX, int mouseY) {
        if (Double.isNaN(x)) {
            // Первый кадр — сразу ставим пятно на курсор, чтобы оно не "прилетало" издалека при открытии экрана.
            x = mouseX;
            y = mouseY;
            return;
        }
        x += (mouseX - x) * FOLLOW_SPEED;
        y += (mouseY - y) * FOLLOW_SPEED;
    }

    void draw(GuiGraphics context) {
        int half = GLOW_SIZE / 2;
        int argbColor = (GLOW_ALPHA << 24) | 0xFFFFFF;

        context.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                (int) Math.round(x) - half, (int) Math.round(y) - half,
                0f, 0f,
                GLOW_SIZE, GLOW_SIZE,
                TEXTURE_SIZE, TEXTURE_SIZE,
                TEXTURE_SIZE, TEXTURE_SIZE,
                argbColor
        );
    }
}