package dev.zenith.client.feature.hud;

/**
 * Текстовое уведомление с анимацией появления/исчезновения.
 * Сам текст, цвет и логика "что показывать" здесь, тайминг — в FadeTimer.
 */
public class FadingNotification {

    private final String text;
    private final int color;
    private final FadeTimer timer = new FadeTimer();

    /** Без указания цвета — обычный белый, как было раньше. */
    public FadingNotification(String text) {
        this(text, 0xFFFFFF);
    }

    public FadingNotification(String text, int color) {
        this.text = text;
        this.color = color;
    }

    public String getText() {
        return text;
    }

    /** RGB без альфа-канала — альфа применяется отдельно, на основе getAlpha(). */
    public int getColor() {
        return color;
    }

    public float getAlpha() {
        return timer.getAlpha();
    }

    public boolean isFinished() {
        return timer.isFinished();
    }
}