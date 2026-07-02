package dev.zenith.client.feature.hud;

import dev.zenith.client.feature.ClientFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * Отслеживает урон, нанесённый игроком существам, и готовит уведомление
 * (сколько сердец снято и по кому) для отрисовки.
 *
 * Сам урон определяется не в момент удара (сервер его ещё не посчитал),
 * а по изменению здоровья цели на следующих тиках — см. onClientTick.
 */
public class DamageIndicatorFeature extends ClientFeature {

    // Сколько тиков ждём подтверждения урона, прежде чем "забыть" цель (например, был промах)
    private static final int TRACKING_TIMEOUT_TICKS = 10;

    private final Map<Integer, TrackedTarget> trackedTargets = new HashMap<>();
    private DamageNotification activeNotification;

    public DamageIndicatorFeature() {
        super("damage_indicator", true);
    }

    /**
     * Вызывается сразу после удара игрока по существу — начинаем следить за его здоровьем.
     */
    public void startTracking(LivingEntity target) {
        trackedTargets.put(target.getId(), new TrackedTarget(target.getHealth(), 0));
    }

    /**
     * Вызывается каждый игровой тик. Сравнивает текущее здоровье отслеживаемых
     * целей с запомненным и создаёт уведомление, если урон подтвердился.
     */
    public void onClientTick(Minecraft client) {
        if (client.level == null) {
            trackedTargets.clear();
            return;
        }

        Iterator<Map.Entry<Integer, TrackedTarget>> iterator = trackedTargets.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, TrackedTarget> entry = iterator.next();
            TrackedTarget tracked = entry.getValue();

            var entity = client.level.getEntity(entry.getKey());

            if (!(entity instanceof LivingEntity livingEntity) || !livingEntity.isAlive()) {
                // Существо погибло или пропало из вида — если у нас было известное
                // здоровье больше нуля, засчитываем это как смертельный удар.
                if (entity != null && tracked.lastKnownHealth() > 0) {
                    activeNotification = new DamageNotification(entity.getName(), tracked.lastKnownHealth());
                }
                iterator.remove();
                continue;
            }

            float currentHealth = livingEntity.getHealth();
            if (currentHealth < tracked.lastKnownHealth()) {
                float damage = tracked.lastKnownHealth() - currentHealth;
                activeNotification = new DamageNotification(livingEntity.getName(), damage);
                iterator.remove();
                continue;
            }

            int ticksWaited = tracked.ticksWaited() + 1;
            if (ticksWaited >= TRACKING_TIMEOUT_TICKS) {
                iterator.remove(); // урон не подтвердился (например, промах) — забываем цель
            } else {
                entry.setValue(new TrackedTarget(tracked.lastKnownHealth(), ticksWaited));
            }
        }
    }

    /**
     * Возвращает активное уведомление для отрисовки, если оно есть и ещё не истекло.
     * Тот же паттерн, что уже используется в BiomeNotifierFeature.getActiveNotification().
     */
    public Optional<DamageNotification> getActiveNotification() {
        if (activeNotification != null && activeNotification.isFinished()) {
            activeNotification = null;
        }
        return Optional.ofNullable(activeNotification);
    }

    /**
     * Последнее известное здоровье цели и сколько тиков мы его ждём.
     * Деталь реализации именно этого класса — наружу не нужна.
     */
    private record TrackedTarget(float lastKnownHealth, int ticksWaited) {}
}