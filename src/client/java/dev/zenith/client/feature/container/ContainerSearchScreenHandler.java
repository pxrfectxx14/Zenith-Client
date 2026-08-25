package dev.zenith.client.feature.container;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import dev.zenith.client.mixin.AbstractContainerScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

/**
 * Связывает ContainerSearchFeature с реальными игровыми экранами.
 * Отслеживает открытие экрана сундука/эндер-сундука (оба используют
 * один и тот же класс меню — ChestMenu), добавляет поле поиска,
 * кнопку переключения режима и подсветку/фильтрацию найденных предметов.
 */
public final class ContainerSearchScreenHandler {

    private static final int SEARCH_BOX_WIDTH = 90;
    private static final int SEARCH_BOX_HEIGHT = 14;
    private static final int SEARCH_BOX_GAP_ABOVE_SCREEN = 18;

    private static final int MODE_BUTTON_WIDTH = 60;
    private static final int MODE_BUTTON_GAP = 4;

    // Размер одного слота в пикселях — стандарт для всех контейнеров в Minecraft.
    private static final int SLOT_SIZE = 16;

    // Насколько сильно затемняются не подходящие под запрос предметы в режиме FILTER.
    private static final int FILTER_DIM_ALPHA = 200;
    private static final int FILTER_DIM_COLOR = 0x8B8B8B; // серый, под цвет фона слотов инвентаря

    private ContainerSearchScreenHandler() {
    }

    public static void register(ContainerSearchFeature feature) {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!feature.isEnabled() || !(screen instanceof AbstractContainerScreen<?> containerScreen)) {
                return;
            }
            if (!(containerScreen.getMenu() instanceof ChestMenu)) {
                return;
            }

            EditBox searchBox = createSearchBox(client, containerScreen);
            searchBox.setResponder(feature::setQuery);
            Screens.getButtons(screen).add(searchBox);

            Button modeButton = createModeButton(client, containerScreen, feature, searchBox);
            Screens.getButtons(screen).add(modeButton);

            // Поле — не кнопка, поэтому Fabric не рисует его сам, рисуем вручную каждый кадр.
            // Кнопку переключения режима Fabric рисует сам — она не требует ручного render().
            ScreenEvents.afterRender(screen).register((s, context, mouseX, mouseY, tickDelta) -> {
                searchBox.render(context, mouseX, mouseY, tickDelta);
                drawSlotEffects(context, containerScreen, feature);
            });

            // Пока поле поиска в фокусе, блокируем клавишу открытия/закрытия инвентаря (обычно "E").
            ScreenKeyboardEvents.allowKeyPress(screen).register((s, key, scancode, modifiers) -> {
                if (!searchBox.isFocused()) {
                    return true;
                }

                boolean isEnter = key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER;
                if (isEnter) {
                    searchBox.setFocused(false);
                    return false;
                }

                if (client.options.keyInventory.matches(key, scancode)) {
                    return false;
                }

                return true;
            });

            // Когда экран закрывают — сбрасываем запрос, чтобы при следующем открытии
            // сундука поле поиска было пустым, а не хранило старый текст.
            ScreenEvents.remove(screen).register(s -> feature.setQuery(""));
        });
    }

    private static EditBox createSearchBox(Minecraft client, AbstractContainerScreen<?> containerScreen) {
        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) containerScreen;
        int x = accessor.getLeftPos();
        int y = accessor.getTopPos() - SEARCH_BOX_GAP_ABOVE_SCREEN;

        return new EditBox(
                client.font,
                x, y,
                SEARCH_BOX_WIDTH, SEARCH_BOX_HEIGHT,
                Component.literal("Поиск")
        );
    }

    /**
     * Кнопка справа от поля поиска, переключающая режим отображения результата.
     * Текст на кнопке всегда отражает текущий активный режим.
     */
    private static Button createModeButton(
            Minecraft client,
            AbstractContainerScreen<?> containerScreen,
            ContainerSearchFeature feature,
            EditBox searchBox
    ) {
        int x = searchBox.getX() + SEARCH_BOX_WIDTH + MODE_BUTTON_GAP;
        int y = searchBox.getY();

        Button button = Button.builder(
                modeButtonLabel(feature),
                b -> {
                    feature.cycleMode();
                    b.setMessage(modeButtonLabel(feature));
                }
        ).bounds(x, y, MODE_BUTTON_WIDTH, SEARCH_BOX_HEIGHT).build();

        return button;
    }

    private static Component modeButtonLabel(ContainerSearchFeature feature) {
        return switch (feature.getMode()) {
            case HIGHLIGHT -> Component.literal("Подсветка");
            case FILTER -> Component.literal("Фильтр");
        };
    }

    /**
     * В зависимости от текущего режима — рисует либо мигающую рамку вокруг
     * найденных предметов (HIGHLIGHT), либо затемняет все НЕ найденные предметы (FILTER).
     */
    private static void drawSlotEffects(GuiGraphics context, AbstractContainerScreen<?> containerScreen, ContainerSearchFeature feature) {
        if (feature.getQuery().isBlank()) {
            return;
        }

        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) containerScreen;
        int left = accessor.getLeftPos();
        int top = accessor.getTopPos();

        switch (feature.getMode()) {
            case HIGHLIGHT -> drawHighlightMode(context, containerScreen, feature, left, top);
            case FILTER -> drawFilterMode(context, containerScreen, feature, left, top);
        }
    }

    private static void drawHighlightMode(GuiGraphics context, AbstractContainerScreen<?> containerScreen, ContainerSearchFeature feature, int left, int top) {
        int color = computePulsingColor();

        for (Slot slot : containerScreen.getMenu().slots) {
            if (!feature.matches(slot.getItem())) {
                continue;
            }

            int x = left + slot.x;
            int y = top + slot.y;
            context.renderOutline(x, y, SLOT_SIZE, SLOT_SIZE, color);
        }
    }

    private static void drawFilterMode(GuiGraphics context, AbstractContainerScreen<?> containerScreen, ContainerSearchFeature feature, int left, int top) {
        int dimColor = (FILTER_DIM_ALPHA << 24) | FILTER_DIM_COLOR;

        for (Slot slot : containerScreen.getMenu().slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty() || feature.matches(stack)) {
                continue; // совпадения и пустые слоты не трогаем
            }

            int x = left + slot.x;
            int y = top + slot.y;
            // Затемняем только сам предмет, рисуя полупрозрачный слой поверх него —
            // слот и его рамка остаются видны, "выцветает" только содержимое.
            context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, dimColor);
        }
    }

    /**
     * Плавно меняющаяся прозрачность (эффект "дыхания"/мигания) на основе синусоиды от текущего времени.
     */
    private static int computePulsingColor() {
        float pulse = (float) (Math.sin(System.currentTimeMillis() / 150.0) + 1) / 2f; // 0.0–1.0
        int alpha = (int) (100 + pulse * 120); // колеблется примерно от 100 до 220 из 255
        return (alpha << 24) | 0xFFFFFF; // белая мигающая рамка
    }
}