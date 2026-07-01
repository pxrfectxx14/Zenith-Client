package dev.zenith.client.feature.hud;

/**
 * Текстовое уведомление с анимацией появления/исчезновения.
 * Сам текст и логика "что показывать" здесь, тайминг — в FadeTimer.
 */
public class FadingNotification {

    private final String text;
    private final FadeTimer timer = new FadeTimer();

    public FadingNotification(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public float getAlpha() {
        return timer.getAlpha();
    }

    public boolean isFinished() {
        return timer.isFinished();
    }
}