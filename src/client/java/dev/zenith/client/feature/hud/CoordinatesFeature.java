package dev.zenith.client.feature.hud;

import dev.zenith.client.feature.ClientFeature;
import net.minecraft.client.Minecraft;

/**
 * Показывает текущие координаты игрока — то же самое, что на клавише F3,
 * но всегда на экране, маленьким полупрозрачным текстом.
 */
public class CoordinatesFeature extends ClientFeature {

    public CoordinatesFeature() {
        super("coordinates", true);
    }

    public String getFormattedCoordinates() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return "";
        }

        int x = (int) Math.floor(client.player.getX());
        int y = (int) Math.floor(client.player.getY());
        int z = (int) Math.floor(client.player.getZ());

        return String.format("X: %d Y: %d Z: %d", x, y, z);
    }
}