package dev.zenith.client;

import dev.zenith.client.feature.FeatureManager;
import dev.zenith.client.feature.hud.BiomeNotifierFeature;
import dev.zenith.client.feature.hud.FadingNotification;
import dev.zenith.client.feature.hud.GameClockFeature;
import dev.zenith.client.feature.hud.PvpHitsCounterFeature;
import dev.zenith.client.feature.hud.RealTimeClockFeature;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;

import java.util.Optional;

public class ZenithClientClient implements ClientModInitializer {

    public static final String MOD_ID = "zenith-client";

    @Override
    public void onInitializeClient() {
        GameClockFeature gameClock = new GameClockFeature();
        FeatureManager.register(gameClock);

        RealTimeClockFeature realTimeClock = new RealTimeClockFeature();
        FeatureManager.register(realTimeClock);

        BiomeNotifierFeature biomeNotifier = new BiomeNotifierFeature();
        FeatureManager.register(biomeNotifier);

        // Каждый тик даём фичам обновить своё внутреннее состояние
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (biomeNotifier.isEnabled()) {
                biomeNotifier.onClientTick(client);
            }
        });

        PvpHitsCounterFeature pvpHitsCounter = new PvpHitsCounterFeature();
        FeatureManager.register(pvpHitsCounter);

        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (pvpHitsCounter.isEnabled()) {
                pvpHitsCounter.registerHit();
            }
            return InteractionResult.PASS;
        });

        registerGameClockHud(gameClock);
        registerRealTimeClockHud(realTimeClock);
        registerBiomeNotificationHud(biomeNotifier);

        registerGameClockHud(gameClock);
        registerRealTimeClockHud(realTimeClock);
        registerBiomeNotificationHud(biomeNotifier);
        registerPvpHitsCounterHud(pvpHitsCounter);
    }

    private void registerGameClockHud(GameClockFeature gameClock) {
        HudElementRegistry.addLast(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "game_clock"),
                (context, tickCounter) -> {
                    if (!gameClock.isEnabled()) {
                        return;
                    }
                    drawGameClock(context, gameClock);
                }
        );
    }

    private void registerRealTimeClockHud(RealTimeClockFeature realTimeClock) {
        HudElementRegistry.addLast(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "real_time_clock"),
                (context, tickCounter) -> {
                    if (!realTimeClock.isEnabled()) {
                        return;
                    }
                    drawRealTimeClock(context, realTimeClock);
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

    private void registerPvpHitsCounterHud(PvpHitsCounterFeature pvpHitsCounter) {
        HudElementRegistry.addLast(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "pvp_hits_counter"),
                (context, tickCounter) -> {
                    if (!pvpHitsCounter.isEnabled()) {
                        return;
                    }
                    drawPvpHitsCounter(context, pvpHitsCounter);
                }
        );
    }

    private void drawPvpHitsCounter(GuiGraphics context, PvpHitsCounterFeature pvpHitsCounter) {
        Minecraft client = Minecraft.getInstance();
        String text = "Hits: " + pvpHitsCounter.getHitCount();

        int margin = 6;
        int textWidth = client.font.width(text);
        int x = client.getWindow().getGuiScaledWidth() - textWidth - margin;
        int y = margin;

        context.drawString(client.font, text, x, y, 0xFFFFFFFF);
    }

    private void drawGameClock(GuiGraphics context, GameClockFeature gameClock) {
        Minecraft client = Minecraft.getInstance();

        float scale = 0.7f;
        int color = 0xFFFFFFFF;
        int lineSpacing = 10; // стандартный межстрочный интервал в Minecraft, до масштабирования

        context.pose().pushMatrix();
        context.pose().translate(10f, 26f);
        context.pose().scale(scale, scale);
        context.drawString(client.font, gameClock.getLabel(), 0, 0, color);
        context.drawString(client.font, gameClock.getTimeRemaining(), 0, lineSpacing, color);
        context.pose().popMatrix();
    }

    private void drawRealTimeClock(GuiGraphics context, RealTimeClockFeature realTimeClock) {
        Minecraft client = Minecraft.getInstance();
        context.drawString(
                client.font,
                realTimeClock.getFormattedTime(),
                10, 10,
                0xFFFFFFFF
        );
    }

    private void drawFadingNotification(GuiGraphics context, FadingNotification notification) {
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