package dev.zenith.client.feature.particles;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.HashMap;
import java.util.Map;

/**
 * Сопоставление "биом → частица", которая будет летать вокруг игрока.
 * Используются встроенные частицы игры — без кастомных текстур.
 * Ключ — технический id биома (как в BiomeColors).
 */
public final class BiomeParticles {

    private static final Map<String, ParticleOptions> PARTICLES = new HashMap<>();

    static {
        // Снежные биомы — снежинки
        PARTICLES.put("snowy_plains", ParticleTypes.SNOWFLAKE);
        PARTICLES.put("ice_spikes", ParticleTypes.SNOWFLAKE);
        PARTICLES.put("snowy_taiga", ParticleTypes.SNOWFLAKE);
        PARTICLES.put("grove", ParticleTypes.SNOWFLAKE);
        PARTICLES.put("snowy_slopes", ParticleTypes.SNOWFLAKE);
        PARTICLES.put("frozen_peaks", ParticleTypes.SNOWFLAKE);
        PARTICLES.put("jagged_peaks", ParticleTypes.SNOWFLAKE);

        // Пустыни и бэдленды — пыль/пепел
        PARTICLES.put("desert", ParticleTypes.WHITE_ASH);
        PARTICLES.put("badlands", ParticleTypes.WHITE_ASH);
        PARTICLES.put("eroded_badlands", ParticleTypes.WHITE_ASH);
        PARTICLES.put("wooded_badlands", ParticleTypes.WHITE_ASH);

        // Болото — споры (замена "пыльцы")
        PARTICLES.put("swamp", ParticleTypes.SPORE_BLOSSOM_AIR);
        PARTICLES.put("mangrove_swamp", ParticleTypes.SPORE_BLOSSOM_AIR);

        // Пещерные биомы с цветами — тоже споры
        PARTICLES.put("lush_caves", ParticleTypes.SPORE_BLOSSOM_AIR);

        // Незер — пепел или споры, в зависимости от подтипа
        PARTICLES.put("nether_wastes", ParticleTypes.ASH);
        PARTICLES.put("soul_sand_valley", ParticleTypes.ASH);
        PARTICLES.put("basalt_deltas", ParticleTypes.ASH);
        PARTICLES.put("crimson_forest", ParticleTypes.CRIMSON_SPORE);
        PARTICLES.put("warped_forest", ParticleTypes.WARPED_SPORE);

        // Энд — тоже подходят споры (мистический эффект)
        PARTICLES.put("the_end", ParticleTypes.SPORE_BLOSSOM_AIR);
        PARTICLES.put("end_highlands", ParticleTypes.SPORE_BLOSSOM_AIR);
        PARTICLES.put("end_midlands", ParticleTypes.SPORE_BLOSSOM_AIR);
    }

    private BiomeParticles() {
    }

    /** null означает "для этого биома атмосферных частиц не предусмотрено". */
    public static ParticleOptions get(ResourceKey<Biome> biome) {
        return PARTICLES.get(biome.location().getPath());
    }
}