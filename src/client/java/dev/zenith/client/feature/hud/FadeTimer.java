package dev.zenith.client.feature.hud;

/**
 * Отвечает только за тайминг анимации "появилось → подержалось → исчезло".
 * Не знает, ЧТО показывается — только КОГДА и с какой прозрачностью.
 * Используется любым уведомлением, которому нужна плавная анимация.
 */
public class FadeTimer {

    private static final long FADE_IN_MS = 300;
    private static final long HOLD_MS = 2000;
    private static final long FADE_OUT_MS = 500;
    private static final long TOTAL_DURATION_MS = FADE_IN_MS + HOLD_MS + FADE_OUT_MS;

    private final long startTimeMs = System.currentTimeMillis();

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

    public boolean isFinished() {
        return System.currentTimeMillis() - startTimeMs >= TOTAL_DURATION_MS;
    }
}