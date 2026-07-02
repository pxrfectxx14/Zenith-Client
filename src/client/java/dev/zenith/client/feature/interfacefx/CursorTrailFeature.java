package dev.zenith.client.feature.interfacefx;

import dev.zenith.client.feature.ClientFeature;

/**
 * Флаг включения/выключения следа за курсором.
 * Вся логика хранения и отрисовки точек следа — в CursorTrailScreenHandler,
 * этот класс участвует только в общей системе фич (вкл/выкл, конфиг).
 */
public class CursorTrailFeature extends ClientFeature {

    public CursorTrailFeature() {
        super("cursor_trail", true);
    }
}