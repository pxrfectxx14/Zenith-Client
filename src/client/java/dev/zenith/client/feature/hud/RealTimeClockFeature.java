package dev.zenith.client.feature.hud;

import dev.zenith.client.feature.ClientFeature;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Показывает текущее реальное (не игровое) время в углу экрана.
 */
public class RealTimeClockFeature extends ClientFeature {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public RealTimeClockFeature() {
        super("real_time_clock", true);
    }

    public String getFormattedTime() {
        return LocalTime.now().format(TIME_FORMAT);
    }
}
