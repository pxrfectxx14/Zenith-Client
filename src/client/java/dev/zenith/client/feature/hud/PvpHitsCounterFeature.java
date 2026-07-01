package dev.zenith.client.feature.hud;

import dev.zenith.client.feature.ClientFeature;

/**
 * Считает количество ударов игрока по существам (мобам, игрокам и т.д.)
 * Само подсчитывание событий (когда именно засчитывать удар) находится
 * снаружи — эта фича только хранит и отдаёт число.
 */
public class PvpHitsCounterFeature extends ClientFeature {

    private int hitCount;

    public PvpHitsCounterFeature() {
        super("pvp_hits_counter", true);
    }

    /**
     * Вызывается снаружи каждый раз, когда зафиксирован удар.
     */
    public void registerHit() {
        hitCount++;
    }

    public int getHitCount() {
        return hitCount;
    }

    /**
     * Обнуляет счётчик. Пригодится позже, например, для кнопки "сбросить"
     * в настройках или при заходе в новый мир.
     */
    public void reset() {
        hitCount = 0;
    }
}