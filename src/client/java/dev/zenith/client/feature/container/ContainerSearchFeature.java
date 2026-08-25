package dev.zenith.client.feature.container;

import dev.zenith.client.feature.ClientFeature;
import net.minecraft.world.item.ItemStack;

/**
 * Хранит текущий поисковый запрос, режим отображения результата,
 * и решает, подходит ли под запрос предмет.
 * Ничего не знает об экранах, слотах и отрисовке — это отдельная ответственность,
 * которую берёт на себя код связи с экраном.
 */
public class ContainerSearchFeature extends ClientFeature {

    /**
     * HIGHLIGHT — совпадения обводятся рамкой, остальные предметы не трогаются.
     * FILTER — совпадения остаются чёткими, все остальные предметы затемняются,
     * создавая эффект "отфильтрованного" списка (сами слоты не двигаются —
     * физически убрать предмет из чужой позиции в контейнере нельзя).
     */
    public enum SearchMode {
        HIGHLIGHT,
        FILTER
    }

    private String query = "";
    private SearchMode mode = SearchMode.HIGHLIGHT;

    public ContainerSearchFeature() {
        super("container_search", true);
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public SearchMode getMode() {
        return mode;
    }

    /** Переключает режим по кругу: HIGHLIGHT -> FILTER -> HIGHLIGHT -> ... */
    public void cycleMode() {
        mode = (mode == SearchMode.HIGHLIGHT) ? SearchMode.FILTER : SearchMode.HIGHLIGHT;
    }

    /**
     * true, если предмет нужно подсветить как результат поиска.
     * Пустой запрос намеренно не подсвечивает ничего — иначе при открытии
     * сундука все слоты сразу окрашивались бы без всякого поиска.
     */
    public boolean matches(ItemStack stack) {
        if (query.isBlank() || stack.isEmpty()) {
            return false;
        }
        String itemName = stack.getHoverName().getString().toLowerCase();
        return itemName.contains(query.toLowerCase());
    }
}