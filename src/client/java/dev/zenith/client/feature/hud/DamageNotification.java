package dev.zenith.client.feature.hud;

import net.minecraft.network.chat.Component;

/**
 * Уведомление о нанесённом уроне: сколько сердец снято и по кому.
 * Переиспользует FadeTimer для тайминга — сам отвечает только за данные удара.
 */
public class DamageNotification {

    private final Component targetName;
    private final float damageInHealthPoints;
    private final FadeTimer timer = new FadeTimer();

    public DamageNotification(Component targetName, float damageInHealthPoints) {
        this.targetName = targetName;
        this.damageInHealthPoints = damageInHealthPoints;
    }

    public Component getTargetName() {
        return targetName;
    }

    /**
     * В Minecraft 1 сердце = 2 единицы здоровья (HP), поэтому делим на 2.
     */
    public float getHearts() {
        return damageInHealthPoints / 2f;
    }

    public float getAlpha() {
        return timer.getAlpha();
    }

    public boolean isFinished() {
        return timer.isFinished();
    }
}