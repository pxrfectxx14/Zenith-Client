package dev.zenith.client.feature;

/**
 * Что-то, что можно показать в меню строкой с переключателем —
 * не только фичи (ClientFeature), но и, например, ресурспак.
 * Список в меню работает только с этим интерфейсом и не обязан знать,
 * что перед ним на самом деле.
 */
public interface Toggleable {
    String getDisplayName();
    boolean isEnabled();
    void setEnabled(boolean enabled);
}