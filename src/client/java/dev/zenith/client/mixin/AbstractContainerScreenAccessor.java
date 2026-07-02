package dev.zenith.client.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Открывает доступ к protected-полям leftPos/topPos у AbstractContainerScreen,
 * не изменяя поведение игры — просто добавляет два "геттера" снаружи.
 */
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {

    @Accessor("leftPos")
    int getLeftPos();

    @Accessor("topPos")
    int getTopPos();
}