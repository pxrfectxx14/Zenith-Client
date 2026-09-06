package dev.zenith.client.mixin;

import dev.zenith.client.ZenithResourcePacks;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Затемняет ванильный серый цвет подписей (0x404040) во ВСЕХ экранах —
 * верстак, сундук, печь, творческий инвентарь и т.д. — но только пока
 * включён наш ресурспак. Перехватывается не конкретный экран, а сам
 * GuiGraphics.drawString(...), через который проходит отрисовка текста
 * в любом меню — поэтому не нужно писать по Mixin'у на каждый экран.
 */
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsLabelColorMixin {

    // Стандартный серый, которым Minecraft красит заголовки меню (0x404040 = 4210752),
    // с добавленной альфой FF — начиная с 1.21.6 цвет обязан явно нести альфа-канал.
    private static final int VANILLA_LABEL_GRAY = 0xFF404040;
    private static final int DARK_LABEL_COLOR = 0xFF221D1C;

    @ModifyVariable(
            method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 2 // среди int-параметров этой сигнатуры (x, y, color) — третий по счёту, начиная с 0
    )
    private int zenith$overrideLabelColor(int color) {
        if (ZenithResourcePacks.isEnabled() && color == VANILLA_LABEL_GRAY) {
            return DARK_LABEL_COLOR;
        }
        return color;
    }
}