package dev.zenith.client.feature;

import java.util.HashMap;
import java.util.Map;

/**
 * Отображаемые названия фич на английском и русском.
 * Ключ — тот самый snake_case id, который передаётся в конструктор ClientFeature
 * (например "game_clock"). Сам id при этом нигде не меняется, меняется только
 * то, что видит пользователь в меню.
 */
final class FeatureTranslations {

    private static final Map<String, String[]> TRANSLATIONS = new HashMap<>();

    static {
        // id                        -> { EN,                      RU }
        put("game_clock",               "Game Clock",              "Внутриигровые часы");
        put("real_time_clock",          "Real-Time Clock",         "Реальное время");
        put("biome_notifier",           "Biome Notifier",          "Оповещение о биоме");
        put("pvp_hits_counter",         "PvP Hits Counter",        "Счётчик ударов PvP");
        put("cursor_trail",             "Cursor Trail",            "След курсора");
        put("atmospheric_particles",    "Atmospheric Particles",   "Атмосферные частицы");
        put("damage_indicator",         "Damage Indicator",        "Индикатор урона");
        put("coordinates",              "Coordinates",             "Координаты");
        put("container_search",         "Container Search",        "Поиск в сундуках");
    }

    private static void put(String id, String en, String ru) {
        TRANSLATIONS.put(id, new String[]{en, ru});
    }

    private FeatureTranslations() {
    }

    /** Возвращает отображаемое имя для данного id и языка. Если перевода нет — отдаёт сырой id как есть. */
    static String get(String id, Lang lang) {
        String[] names = TRANSLATIONS.get(id);
        if (names == null) {
            return id; // на случай новой фичи, для которой перевод ещё не добавили
        }
        return lang == Lang.RU ? names[1] : names[0];
    }
}