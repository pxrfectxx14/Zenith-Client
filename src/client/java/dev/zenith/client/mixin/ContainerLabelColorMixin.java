package dev.zenith.client.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Затемняет цвет заголовков в контейнерных экранах (верстак, сундук и т.д.).
 * Цвет в vanilla-коде не вынесен в отдельное поле, а передан как аргумент
 * прямо в вызов отрисовки текста — поэтому перехватываем именно аргумент,
 * а не поле, как я предполагал раньше.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class ContainerLabelColorMixin {

    private static final int LABEL_COLOR = 0xFF221D1C;

    @ModifyArg(
            method = "renderLabels",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V"
            ),
            index = 4
    )
    private int zenith$overrideLabelColor(int originalColor) {
        return dev.zenith.client.ZenithResourcePacks.isEnabled() ? LABEL_COLOR : originalColor;
    }
}