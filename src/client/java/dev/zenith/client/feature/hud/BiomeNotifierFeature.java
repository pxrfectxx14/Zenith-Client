package dev.zenith.client.feature.hud;

import dev.zenith.client.feature.ClientFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import java.util.Optional;

/**
 * Отслеживает смену биома игрока и управляет анимированным уведомлением об этом.
 */
public class BiomeNotifierFeature extends ClientFeature {

    private ResourceKey<Biome> lastBiome;
    private FadingNotification activeNotification;

    public BiomeNotifierFeature() {
        super("biome_notifier", true);
    }

    /**
      Вызывается каждый игровой тик. Сравнивает текущий биом с предыдущим и,
      если он изменился, запускает новое уведомление.
     **/
    public void onClientTick(Minecraft client) {
        if (client.level == null || client.player == null) {
            return;
        }

        BlockPos playerPos = client.player.blockPosition();
        Optional<ResourceKey<Biome>> currentBiome = client.level.getBiome(playerPos).unwrapKey();

        if (currentBiome.isEmpty()) {
            return;
        }

        ResourceKey<Biome> biome = currentBiome.get();
        boolean biomeChanged = !biome.equals(lastBiome);

        if (biomeChanged) {
            if (lastBiome != null) {
                activeNotification = new FadingNotification(formatBiomeName(biome), BiomeColors.get(biome));
            }
            lastBiome = biome;
        }
    }

    private String formatBiomeName(ResourceKey<Biome> biome) {
        String technicalName = biome.location().getPath();
        String[] words = technicalName.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!result.isEmpty()) {
                result.append(" ");
            }
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }

    /**
      Возвращает активное уведомление для отрисовки, если оно есть и ещё не истекло.
      Автоматически "забывает" уведомление, когда его анимация полностью завершилась.
     **/
    public Optional<FadingNotification> getActiveNotification() {
        if (activeNotification != null && activeNotification.isFinished()) {
            activeNotification = null;
        }
        return Optional.ofNullable(activeNotification);
    }
}
