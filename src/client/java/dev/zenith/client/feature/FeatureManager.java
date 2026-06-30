package dev.zenith.client.feature;

import java.util.ArrayList;
import java.util.List;

/**
 * Хранит все зарегистрированные фичи мода и даёт к ним доступ.
 * Это единственное место, которое "знает" обо всех фичах сразу —
 * остальной код работает с ними через этот класс, а не напрямую.
 */
public final class FeatureManager {

    private static final List<ClientFeature> FEATURES = new ArrayList<>();

    private FeatureManager() {
        // утилитный класс — создавать экземпляры не нужно
    }

    public static void register(ClientFeature feature) {
        FEATURES.add(feature);
    }

    public static List<ClientFeature> getAll() {
        return FEATURES;
    }

    public static ClientFeature getByName(String name) {
        for (ClientFeature feature : FEATURES) {
            if (feature.getName().equals(name)) {
                return feature;
            }
        }
        return null;
    }
}