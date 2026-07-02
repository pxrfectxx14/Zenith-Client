package dev.zenith.client.feature.container;

import dev.zenith.client.feature.ClientFeature;
import net.minecraft.world.item.ItemStack;

/**
 * Хранит текущий поисковый запрос и решает, подходит ли под него предмет.
 * Ничего не знает об экранах, слотах и отрисовке — это отдельная ответственность,
 * которую возьмёт на себя код связи с экраном (следующий шаг).
 */
public class ContainerSearchFeature extends ClientFeature {

    private String query = "";

    public ContainerSearchFeature() {
        super("container_search", true);
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
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