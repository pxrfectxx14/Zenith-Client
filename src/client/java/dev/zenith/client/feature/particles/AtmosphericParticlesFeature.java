package dev.zenith.client.feature.particles;

import dev.zenith.client.feature.ClientFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.Optional;
import java.util.Random;

/**
 * Спавнит атмосферные частицы вокруг игрока в зависимости от текущего биома.
 * Использует встроенные частицы игры (снег, пепел, споры и т.д.) — без своих текстур.
 */
public class AtmosphericParticlesFeature extends ClientFeature {

    // Радиус зоны спавна вокруг игрока по X/Z и по высоте.
    private static final double HORIZONTAL_RADIUS = 8.0;
    private static final double VERTICAL_RADIUS = 4.0;

    // Шанс спавна одной частицы за тик (0.0–1.0). Меньше — реже, разреженнее.
    private static final double SPAWN_CHANCE_PER_TICK = 0.5;

    private final Random random = new Random();

    public AtmosphericParticlesFeature() {
        super("atmospheric_particles", true);
    }

    public void onClientTick(Minecraft client) {
        if (client.level == null || client.player == null) {
            return;
        }

        Optional<ResourceKey<Biome>> currentBiome = client.level
                .getBiome(client.player.blockPosition())
                .unwrapKey();

        if (currentBiome.isEmpty()) {
            return;
        }

        ParticleOptions particle = BiomeParticles.get(currentBiome.get());
        if (particle == null) {
            return; // для этого биома частицы не заданы
        }

        if (random.nextDouble() > SPAWN_CHANCE_PER_TICK) {
            return;
        }

        double x = client.player.getX() + (random.nextDouble() * 2 - 1) * HORIZONTAL_RADIUS;
        double y = client.player.getY() + random.nextDouble() * VERTICAL_RADIUS;
        double z = client.player.getZ() + (random.nextDouble() * 2 - 1) * HORIZONTAL_RADIUS;

        // Небольшой случайный снос — частицы не стоят на месте, а слегка "плывут".
        double dx = (random.nextDouble() - 0.5) * 0.02;
        double dy = -0.02; // лёгкое оседание вниз, как у настоящего снега/пепла
        double dz = (random.nextDouble() - 0.5) * 0.02;

        client.level.addParticle(particle, x, y, z, dx, dy, dz);
    }
}