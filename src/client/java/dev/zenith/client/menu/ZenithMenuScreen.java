package dev.zenith.client.menu;

import dev.zenith.client.feature.ClientFeature;
import dev.zenith.client.feature.FeatureManager;
import dev.zenith.client.feature.Lang;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ZenithMenuScreen extends Screen {

    private static final int LIST_WIDTH = 220;
    private static final int LANG_BUTTON_WIDTH = 50;
    private static final int LANG_BUTTON_HEIGHT = 20;
    private static final int LANG_BUTTON_TOP = 36;
    private static final int LIST_TOP = LANG_BUTTON_TOP + LANG_BUTTON_HEIGHT + 6;
    private static final int LIST_BOTTOM_MARGIN = 20;

    public ZenithMenuScreen() {
        super(Component.literal("Zenith Client"));
    }

    @Override
    protected void init() {
        int listX = (this.width - LIST_WIDTH) / 2;
        int listBottom = this.height - LIST_BOTTOM_MARGIN;

        // Кнопка переключения языка — над списком, прижата к правому краю списка
        int langButtonX = listX + LIST_WIDTH - LANG_BUTTON_WIDTH;
        this.addRenderableWidget(Button.builder(
                        Component.literal(Lang.current().name()),
                        button -> {
                            Lang.toggle();
                            // Пересоздаём экран, чтобы все тексты (названия фич, ON/OFF) перерисовались на новом языке
                            this.minecraft.setScreen(new ZenithMenuScreen());
                        })
                .bounds(langButtonX, LANG_BUTTON_TOP, LANG_BUTTON_WIDTH, LANG_BUTTON_HEIGHT)
                .build());

        FeatureListWidget featureList = new FeatureListWidget(
                this.minecraft, LIST_WIDTH, listBottom - LIST_TOP, LIST_TOP
        );
        for (ClientFeature feature : FeatureManager.getAll()) {
            featureList.addFeatureEntry(feature);
        }
        featureList.setX(listX);

        featureList.addFeatureEntry(new ResourcePackToggle());

        this.addRenderableWidget(featureList);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        context.fill(0, 0, this.width, this.height, 0xC0101010); // тёмная полупрозрачная подложка, без блюра
        super.render(context, mouseX, mouseY, partialTick);
        context.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false; // чтобы игра не ставилась на паузу в одиночной игре при открытии меню
    }
}