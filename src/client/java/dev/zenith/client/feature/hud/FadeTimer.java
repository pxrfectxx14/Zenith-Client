package dev.zenith.client.feature.hud;

/**
 * Отвечает только за тайминг анимации "появилось → подержалось → исчезло".
 * Длительности можно задать через конструктор — так один и тот же класс
 * подходит и для короткого уведомления (биом), и для долгого (урон).
 */
public class FadeTimer {

    private final long fadeInMs;
    private final long holdMs;
    private final long fadeOutMs;
    private final long totalDurationMs;

    private final long startTimeMs = System.currentTimeMillis();

    /** Значения по умолчанию — как было раньше (0.3с появление, 2с держим, 0.5с исчезновение). */
    public FadeTimer() {
        this(300, 2000, 500);
    }

    public FadeTimer(long fadeInMs, long holdMs, long fadeOutMs) {
        this.fadeInMs = fadeInMs;
        this.holdMs = holdMs;
        this.fadeOutMs = fadeOutMs;
        this.totalDurationMs = fadeInMs + holdMs + fadeOutMs;
    }

    public float getAlpha() {
        long elapsed = System.currentTimeMillis() - startTimeMs;

        if (elapsed < fadeInMs) {
            return elapsed / (float) fadeInMs;
        }
        if (elapsed < fadeInMs + holdMs) {
            return 1.0f;
        }
        long fadeOutElapsed = elapsed - fadeInMs - holdMs;
        if (fadeOutElapsed < fadeOutMs) {
            return 1.0f - (fadeOutElapsed / (float) fadeOutMs);
        }
        return 0.0f;
    }

    public boolean isFinished() {
        return System.currentTimeMillis() - startTimeMs >= totalDurationMs;
    }
}