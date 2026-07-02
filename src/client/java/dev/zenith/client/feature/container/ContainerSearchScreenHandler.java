package dev.zenith.client.feature.container;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import org.lwjgl.glfw.GLFW;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import dev.zenith.client.mixin.AbstractContainerScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;

/**
 * Связывает ContainerSearchFeature с реальными игровыми экранами.
 * Отслеживает открытие экрана сундука/эндер-сундука (оба используют
 * один и тот же класс меню — ChestMenu) и добавляет поле поиска,
 * а также подсветку найденных предметов.
 */
public final class ContainerSearchScreenHandler {

    private static final int SEARCH_BOX_WIDTH = 90;
    private static final int SEARCH_BOX_HEIGHT = 14;
    private static final int SEARCH_BOX_GAP_ABOVE_SCREEN = 18;

    // Размер одного слота в пикселях — стандарт для всех контейнеров в Minecraft.
    private static final int SLOT_SIZE = 16;

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

            // Поле — не кнопка, поэтому Fabric не рисует его сам, рисуем вручную каждый кадр.
            // Заодно рисуем подсветку слотов с найденными предметами.
            ScreenEvents.afterRender(screen).register((s, context, mouseX, mouseY, tickDelta) -> {
                searchBox.render(context, mouseX, mouseY, tickDelta);
                drawSlotHighlights(context, containerScreen, feature);
            });

            // Пока поле поиска в фокусе, блокируем клавишу открытия/закрытия инвентаря (обычно "E").
            // Без этого Minecraft закрывает контейнер на уровне ниже, чем печать текста в поле,
            // не дожидаясь, что символ вообще дойдёт до поля ввода.
            //
            // Enter (или Numpad Enter) — особый случай: он специально снимает фокус с поля,
            // чтобы после подтверждения поиска игрок снова мог закрывать сундук клавишей "E",
            // не кликая мимо поля вручную.
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
     * Рисует мигающую рамку вокруг слотов, содержимое которых подходит под поисковый запрос.
     * Рамка, а не заливка — специально, чтобы не закрывать сам предмет и его количество.
     */
    private static void drawSlotHighlights(GuiGraphics context, AbstractContainerScreen<?> containerScreen, ContainerSearchFeature feature) {
        if (feature.getQuery().isBlank()) {
            return;
        }

        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) containerScreen;
        int left = accessor.getLeftPos();
        int top = accessor.getTopPos();

        int color = computePulsingColor();

        for (Slot slot : containerScreen.getMenu().slots) {
            if (!feature.matches(slot.getItem())) {
                continue;
            }

            int x = left + slot.x;
            int y = top + slot.y;

            // Встроенный метод сам рисует ровную рамку по всем четырём сторонам —
            // никаких ручных смещений и риска асимметрии, как было с четырьмя fill().
            context.renderOutline(x, y, SLOT_SIZE, SLOT_SIZE, color);
        }
    }

    /**
     * Плавно меняющаяся прозрачность (эффект "дыхания"/мигания) на основе синусоиды от текущего времени.
     * Тот же принцип, что и в FadeTimer — просто периодическая, а не одноразовая анимация.
     */
    private static int computePulsingColor() {
        float pulse = (float) (Math.sin(System.currentTimeMillis() / 150.0) + 1) / 2f; // 0.0–1.0
        int alpha = (int) (100 + pulse * 120); // колеблется примерно от 100 до 220 из 255
        return (alpha << 24) | 0xFFFFFF; // белая мигающая рамка, как на референсе
    }
}