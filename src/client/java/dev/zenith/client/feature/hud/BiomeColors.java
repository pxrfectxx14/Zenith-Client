package dev.zenith.client.feature.hud;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.HashMap;
import java.util.Map;

/**
 * Ручное сопоставление "биом → цвет текста уведомления".
 * Ключ — технический id биома без пространства имён (то, что видно
 * в /locate или в NBT, например "plains", "desert", "dark_forest").
 * Чтобы поменять или добавить цвет — просто впиши/измени строку ниже.
 * Формат цвета — RGB в hex, например 0xFF0000 — красный.
 */
public final class BiomeColors {

    private static final Map<String, Integer> COLORS = new HashMap<>();

    static {
        COLORS.put("plains", 0x9ACD32);
        COLORS.put("desert", 0xEDC9AF);
        COLORS.put("forest", 0x228B22);
        COLORS.put("dark_forest", 0x145214);
        COLORS.put("swamp", 0x6B8E23);
        COLORS.put("ocean", 0x1E90FF);
        COLORS.put("deep_ocean", 0x0000AA);
        COLORS.put("river", 0x4682B4);
        COLORS.put("snowy_plains", 0xE0FFFF);
        COLORS.put("ice_spikes", 0xADD8E6);
        COLORS.put("taiga", 0x2E8B57);
        COLORS.put("snowy_taiga", 0xB0E0E6);
        COLORS.put("jungle", 0x32CD32);
        COLORS.put("bamboo_jungle", 0x9ACD32);
        COLORS.put("savanna", 0xBDB76B);
        COLORS.put("badlands", 0xD2691E);
        COLORS.put("mushroom_fields", 0xFF69B4);
        COLORS.put("beach", 0xFFE4B5);
        COLORS.put("stony_shore", 0xA9A9A9);
        COLORS.put("windswept_hills", 0x808080);
        COLORS.put("nether_wastes", 0xFF4500);
        COLORS.put("crimson_forest", 0xDC143C);
        COLORS.put("warped_forest", 0x20B2AA);
        COLORS.put("soul_sand_valley", 0x483D8B);
        COLORS.put("basalt_deltas", 0x696969);
        COLORS.put("the_end", 0x9370DB);
        COLORS.put("end_highlands", 0xBA55D3);
        COLORS.put("end_midlands", 0x8A2BE2);
        COLORS.put("small_end_islands", 0x9932CC);
        COLORS.put("end_barrens", 0x800080);
    }

    // Цвет для любого биома, которого нет в списке выше.
    private static final int DEFAULT_COLOR = 0xFFFFFF;

    private BiomeColors() {
    }

    public static int get(ResourceKey<Biome> biome) {
        String path = biome.location().getPath();
        return COLORS.getOrDefault(path, DEFAULT_COLOR);
    }
}