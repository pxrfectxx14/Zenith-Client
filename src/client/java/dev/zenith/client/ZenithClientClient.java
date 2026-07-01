package dev.zenith.client;

import dev.zenith.client.feature.FeatureManager;
import dev.zenith.client.feature.hud.BiomeNotifierFeature;
import dev.zenith.client.feature.hud.FadingNotification;
import dev.zenith.client.feature.hud.GameClockFeature;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class ZenithClientClient implements ClientModInitializer {

    public static final String MOD_ID = "zenith-client";

    @Override
    public void onInitializeClient() {
        GameClockFeature gameClock = new GameClockFeature();
        FeatureManager.register(gameClock);

        BiomeNotifierFeature biomeNotifier = new BiomeNotifierFeature();
        FeatureManager.register(biomeNotifier);

        // Каждый тик даём фичам обновить своё внутреннее состояние
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (biomeNotifier.isEnabled()) {
                biomeNotifier.onClientTick(client);
            }
        });

        registerGameClockHud(gameClock);
        registerBiomeNotificationHud(biomeNotifier);
    }

    private void registerGameClockHud(GameClockFeature gameClock) {
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
                            0xFFFFFFFF
                    );
                }
        );
    }

    private void registerBiomeNotificationHud(BiomeNotifierFeature biomeNotifier) {
        HudElementRegistry.addLast(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "biome_notification"),
                (context, tickCounter) -> {
                    if (!biomeNotifier.isEnabled()) {
                        return;
                    }

                    Optional<FadingNotification> notification = biomeNotifier.getActiveNotification();
                    if (notification.isEmpty()) {
                        return;
                    }

                    drawFadingNotification(context, notification.get());
                }
        );
    }

    private void drawFadingNotification(net.minecraft.client.gui.GuiGraphics context, FadingNotification notification) {
        Minecraft client = Minecraft.getInstance();
        String text = notification.getText();

        int alpha = (int) (notification.getAlpha() * 255);
        int color = (alpha << 24) | 0xFFFFFF; // белый текст с переменной прозрачностью

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int textWidth = client.font.width(text);
        int x = (screenWidth - textWidth) / 2;
        int y = 40; // чуть ниже верхнего края экрана

        context.drawString(client.font, text, x, y, color);
    }
}
