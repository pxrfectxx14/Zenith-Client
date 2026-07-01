package dev.zenith.client.feature.hud;

import dev.zenith.client.feature.ClientFeature;
import net.minecraft.client.Minecraft;

/**
 * Показывает, сколько игрового времени осталось до ночи или до рассвета.
 */
public class GameClockFeature extends ClientFeature {

    private static final long TICKS_PER_DAY = 24000;
    private static final long NIGHT_START_TICK = 13000;

    public GameClockFeature() {
        super("game_clock", true); // включена по умолчанию
    }

    /**
     * Текст, который нужно нарисовать на экране.
     * Возвращает пустую строку, если игрок не в мире (например, в меню).
     */
    public String getStatusText() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return "";
        }

        long timeOfDay = client.level.getDayTime() % TICKS_PER_DAY;
        boolean isNight = timeOfDay >= NIGHT_START_TICK;

        long ticksLeft = isNight
                ? TICKS_PER_DAY - timeOfDay      // сколько осталось до рассвета (полночь = 0)
                : NIGHT_START_TICK - timeOfDay;  // сколько осталось до ночи

        String label = isNight ? "До рассвета: " : "До ночи: ";
        return label + formatTime(ticksLeft);
    }

    private String formatTime(long ticks) {
        long totalSeconds = ticks / 20; // в Minecraft 20 тиков = 1 реальная секунда
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
