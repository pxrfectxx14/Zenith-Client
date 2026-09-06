package dev.zenith.client.feature;

/**
 * Базовый класс для всех фич мода.
 * Каждая фича (HUD-элемент, поиск в сундуках и т.д.) наследуется от него.
 */
public abstract class ClientFeature implements Toggleable {

    private final String name;
    private boolean enabled;

    protected ClientFeature(String name, boolean enabledByDefault) {
        this.name = name;
        this.enabled = enabledByDefault;
    }

    public String getName() {
        return name;
    }

    /** Отображаемое название фичи для UI, на текущем выбранном языке (см. {@link Lang}). */
    public String getDisplayName() {
        return FeatureTranslations.get(name, Lang.current());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return; // ничего не изменилось — не дёргаем события
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    /** Вызывается, когда фичу включили. По умолчанию ничего не делает — переопределяй при необходимости. */
    protected void onEnable() {}

    /** Вызывается, когда фичу выключили. */
    protected void onDisable() {}
}