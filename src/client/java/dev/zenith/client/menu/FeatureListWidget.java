package dev.zenith.client.menu;

import dev.zenith.client.feature.Toggleable;
import dev.zenith.client.feature.Lang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Прокручиваемый список фич с переключателем вкл/выкл в каждой строке.
 * Сама логика хранения и переключения фич уже есть в ClientFeature/FeatureManager —
 * этот класс только отображает список и дёргает setEnabled() по клику на кнопку.
 */
final class FeatureListWidget extends ObjectSelectionList<FeatureListWidget.FeatureEntry> {

    private static final int ROW_HEIGHT = 24;

    FeatureListWidget(Minecraft minecraft, int width, int height, int top) {
        super(minecraft, width, height, top, ROW_HEIGHT);
    }

    void addFeatureEntry(Toggleable feature) {
        this.addEntry(new FeatureEntry(feature));
    }

    static final class FeatureEntry extends ObjectSelectionList.Entry<FeatureEntry> {

        private static final ResourceLocation BUTTON_SPRITE =
                ResourceLocation.withDefaultNamespace("widget/button");
        private static final ResourceLocation BUTTON_HIGHLIGHTED_SPRITE =
                ResourceLocation.withDefaultNamespace("widget/button_highlighted");

        private static final int BUTTON_HEIGHT = 20;
        private static final int BUTTON_HORIZONTAL_PADDING = 16;

        private final Toggleable feature;

        private int buttonX;
        private int buttonY;
        private int buttonWidth;

        FeatureEntry(Toggleable feature) {
            this.feature = feature;
        }

        private String stateText() {
            if (Lang.current() == Lang.RU) {
                return feature.isEnabled() ? "ВКЛ" : "ВЫКЛ";
            }
            return feature.isEnabled() ? "ON" : "OFF";
        }

        @Override
        public void render(GuiGraphics context, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovering, float partialTick) {
            Font font = Minecraft.getInstance().font;

            context.drawString(font, feature.getDisplayName(), left + 6, top + height / 2 - 4, 0xFFFFFFFF);

            String stateText = stateText();
            this.buttonWidth = font.width(stateText) + BUTTON_HORIZONTAL_PADDING;
            this.buttonX = left + width - buttonWidth - 6;
            this.buttonY = top + (height - BUTTON_HEIGHT) / 2;

            boolean buttonHovered = mouseX >= buttonX && mouseX < buttonX + buttonWidth
                    && mouseY >= buttonY && mouseY < buttonY + BUTTON_HEIGHT;

            ResourceLocation sprite = buttonHovered ? BUTTON_HIGHLIGHTED_SPRITE : BUTTON_SPRITE;
            context.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, buttonX, buttonY, buttonWidth, BUTTON_HEIGHT);

            int textColor = feature.isEnabled() ? 0xFF55FF55 : 0xFFFF5555;
            context.drawCenteredString(font, stateText, buttonX + buttonWidth / 2, buttonY + 6, textColor);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            boolean insideButton = mouseX >= buttonX && mouseX < buttonX + buttonWidth
                    && mouseY >= buttonY && mouseY < buttonY + BUTTON_HEIGHT;
            if (!insideButton) {
                return false;
            }
            feature.setEnabled(!feature.isEnabled());
            return true;
        }

        @Override
        public Component getNarration() {
            return Component.literal(feature.getDisplayName());
        }
    }
}