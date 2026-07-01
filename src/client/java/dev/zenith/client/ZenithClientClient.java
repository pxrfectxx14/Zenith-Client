package dev.zenith.client;

import dev.zenith.client.feature.FeatureManager;
import dev.zenith.client.feature.hud.GameClockFeature;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class ZenithClientClient implements ClientModInitializer {

    public static final String MOD_ID = "zenith-client";

    @Override
    public void onInitializeClient() {
        GameClockFeature gameClock = new GameClockFeature();
        FeatureManager.register(gameClock);

        HudElementRegistry.addLast(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "game_clock"),
                (context, tickCounter) -> {
                    if (!gameClock.isEnabled()) {
                        return;
                    }
                    context.drawString(
                            Minecraft.getInstance().font,
                            gameClock.getStatusText(),
                            10, 10,
                            0xFFFFFFFF // ARGB: с 1.21.6 альфа-канал обязателен, 0 в альфе = невидимый текст
                    );
                }
        );
    }
}
