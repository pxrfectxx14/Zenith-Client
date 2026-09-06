package dev.zenith.client.feature;

/**
 * Текущий язык интерфейса мода (меню фич).
 * Не влияет на язык самого Minecraft — это отдельный, свой переключатель.
 */
public enum Lang {
    EN,
    RU;

    private static Lang current = EN;

    public static Lang current() {
        return current;
    }

    public static void toggle() {
        current = (current == EN) ? RU : EN;
    }
}