package dev.zenith.client.feature.hud;

/**
 * Уведомление с анимацией "плавно появилось → подержалось → плавно исчезло".
 * Ничего не знает о том, ЧТО отображается (биом, достижение и т.д.) —
 * только о том, как меняется прозрачность текста со временем.
 */
public class FadingNotification {

    private static final long FADE_IN_MS = 300;
    private static final long HOLD_MS = 2000;
    private static final long FADE_OUT_MS = 500;
    private static final long TOTAL_DURATION_MS = FADE_IN_MS + HOLD_MS + FADE_OUT_MS;

    private final String text;
    private final long startTimeMs;

    public FadingNotification(String text) {
        this.text = text;
        this.startTimeMs = System.currentTimeMillis();
    }

    public String getText() {
        return text;
    }

    /**
     * Прозрачность от 0.0 (невидимо) до 1.0 (полностью видно) в текущий момент.
     */
    public float getAlpha() {
        long elapsed = System.currentTimeMillis() - startTimeMs;

        if (elapsed < FADE_IN_MS) {
            return elapsed / (float) FADE_IN_MS;
        }
        if (elapsed < FADE_IN_MS + HOLD_MS) {
            return 1.0f;
        }
        long fadeOutElapsed = elapsed - FADE_IN_MS - HOLD_MS;
        if (fadeOutElapsed < FADE_OUT_MS) {
            return 1.0f - (fadeOutElapsed / (float) FADE_OUT_MS);
        }
        return 0.0f;
    }

    /**
     * true, когда анимация полностью прошла и уведомление больше не нужно показывать.
     */
    public boolean isFinished() {
        return System.currentTimeMillis() - startTimeMs >= TOTAL_DURATION_MS;
    }
}
