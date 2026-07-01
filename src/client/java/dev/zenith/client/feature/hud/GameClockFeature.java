package dev.zenith.client.feature.hud;

import dev.zenith.client.feature.ClientFeature;
import net.minecraft.client.Minecraft;

/**
 * Отслеживает игровое время и готовит данные о том, сколько осталось
 * до ночи или до рассвета. Ничего не знает о том, как это будет нарисовано —
 * одна строка, две строки, какой шрифт и т.д. Это решает код отрисовки.
 */
public class GameClockFeature extends ClientFeature {

    private static final long TICKS_PER_DAY = 24000;
    private static final long NIGHT_START_TICK = 13000;

    public GameClockFeature() {
        super("game_clock", true);
    }

    /**
     * Текст метки: "До рассвета" ночью или "До ночи" днём.
     * Пустая строка, если игрок не в мире (например, в меню).
     */
    public String getLabel() {
        ClockState state = computeState();
        if (state == null) {
            return "";
        }
        return state.isNight() ? "До рассвета" : "До ночи";
    }

    /**
     * Оставшееся время в формате мм:сс.
     */
    public String getTimeRemaining() {
        ClockState state = computeState();
        if (state == null) {
            return "";
        }
        return formatTime(state.ticksLeft());
    }

    private ClockState computeState() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return null;
        }

        long timeOfDay = client.level.getDayTime() % TICKS_PER_DAY;
        boolean isNight = timeOfDay >= NIGHT_START_TICK;
        long ticksLeft = isNight
                ? TICKS_PER_DAY - timeOfDay
                : NIGHT_START_TICK - timeOfDay;

        return new ClockState(isNight, ticksLeft);
    }

    private String formatTime(long ticks) {
        long totalSeconds = ticks / 20;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Небольшой неизменяемый контейнер для результата расчёта.
     * Нужен только чтобы не дублировать арифметику выше в двух методах.
     */
    private record ClockState(boolean isNight, long ticksLeft) {}
}