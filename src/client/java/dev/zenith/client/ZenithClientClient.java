package dev.zenith.client;

import dev.zenith.client.feature.FeatureManager;
import dev.zenith.client.feature.hud.BiomeNotifierFeature;
import dev.zenith.client.feature.hud.DamageIndicatorFeature;
import dev.zenith.client.feature.hud.DamageNotification;
import dev.zenith.client.feature.hud.FadingNotification;
import dev.zenith.client.feature.hud.GameClockFeature;
import dev.zenith.client.feature.particles.AtmosphericParticlesFeature;
import dev.zenith.client.feature.hud.PvpHitsCounterFeature;
import dev.zenith.client.feature.container.ContainerSearchFeature;
import dev.zenith.client.feature.interfacefx.CursorTrailFeature;
import dev.zenith.client.feature.interfacefx.CursorTrailScreenHandler;
import dev.zenith.client.feature.hud.CoordinatesFeature;
import dev.zenith.client.feature.container.ContainerSearchScreenHandler;
import dev.zenith.client.feature.hud.RealTimeClockFeature;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;

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

        PvpHitsCounterFeature pvpHitsCounter = new PvpHitsCounterFeature();
        FeatureManager.register(pvpHitsCounter);

        CursorTrailFeature cursorTrail = new CursorTrailFeature();
        FeatureManager.register(cursorTrail);
        CursorTrailScreenHandler.register(cursorTrail);

        AtmosphericParticlesFeature atmosphericParticles = new AtmosphericParticlesFeature();
        FeatureManager.register(atmosphericParticles);

        DamageIndicatorFeature damageIndicator = new DamageIndicatorFeature();
        FeatureManager.register(damageIndicator);

        CoordinatesFeature coordinates = new CoordinatesFeature();
        FeatureManager.register(coordinates);

        ContainerSearchFeature containerSearch = new ContainerSearchFeature();
        FeatureManager.register(containerSearch);
        ContainerSearchScreenHandler.register(containerSearch);

        // Каждый тик даём фичам обновить своё внутреннее состояние
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (atmosphericParticles.isEnabled()) {
                atmosphericParticles.onClientTick(client);
            }

            if (biomeNotifier.isEnabled()) {
                biomeNotifier.onClientTick(client);
            }
            if (damageIndicator.isEnabled()) {
                damageIndicator.onClientTick(client);
            }
        });

        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (pvpHitsCounter.isEnabled()) {
                pvpHitsCounter.registerHit();
            }
            if (damageIndicator.isEnabled() && entity instanceof LivingEntity livingEntity) {
                damageIndicator.startTracking(livingEntity);
            }
            return InteractionResult.PASS;
        });

        registerGameClockHud(gameClock, realTimeClock);
        registerRealTimeClockHud(realTimeClock);
        registerCoordinatesHud(coordinates);
        registerBiomeNotificationHud(biomeNotifier);
        registerDamageIndicatorHud(damageIndicator);
    }

    private void registerGameClockHud(GameClockFeature gameClock, RealTimeClockFeature realTimeClock) {
        HudElementRegistry.addLast(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "game_clock"),
                (context, tickCounter) -> {
                    if (!gameClock.isEnabled()) {
                        return;
                    }
                    drawGameClock(context, gameClock, realTimeClock);
                }
        );
    }

    private void registerCoordinatesHud(CoordinatesFeature coordinates) {
        HudElementRegistry.addLast(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "coordinates"),
                (context, tickCounter) -> {
                    if (!coordinates.isEnabled()) {
                        return;
                    }
                    drawCoordinates(context, coordinates);
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

    private void registerDamageIndicatorHud(DamageIndicatorFeature damageIndicator) {
        HudElementRegistry.addLast(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "damage_indicator"),
                (context, tickCounter) -> {
                    if (!damageIndicator.isEnabled()) {
                        return;
                    }

                    Optional<DamageNotification> notification = damageIndicator.getActiveNotification();
                    if (notification.isEmpty()) {
                        return;
                    }

                    drawDamageNotification(context, notification.get());
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

    private void drawGameClock(GuiGraphics context, GameClockFeature gameClock, RealTimeClockFeature realTimeClock) {
        Minecraft client = Minecraft.getInstance();

        float scale = 0.7f;
        int color = 0xFFFFFFFF;
        int lineSpacing = 10; // стандартный межстрочный интервал в Minecraft, до масштабирования
        int gapAfterRealTime = 10; // отступ между реальным временем и этим блоком

        int realTimeWidth = client.font.width(realTimeClock.getFormattedTime());
        float scaledRealTimeWidth = realTimeWidth * REAL_TIME_SCALE;
        float x = 10f + scaledRealTimeWidth + gapAfterRealTime;
        float y = 10f; // та же высота, что и у реального времени

        context.pose().pushMatrix();
        context.pose().translate(x, y);
        context.pose().scale(scale, scale);
        context.drawString(client.font, gameClock.getLabel(), 0, 0, color);
        context.drawString(client.font, gameClock.getTimeRemaining(), 0, lineSpacing, color);
        context.pose().popMatrix();
    }

    private void drawCoordinates(GuiGraphics context, CoordinatesFeature coordinates) {
        Minecraft client = Minecraft.getInstance();
        String text = coordinates.getFormattedCoordinates();

        float scale = 0.7f;
        int alpha = 150; // около 60% непрозрачности — заметно, но не отвлекает
        int margin = 10;

        int screenHeight = client.getWindow().getGuiScaledHeight();
        int textHeight = client.font.lineHeight; // обычно 9 пикселей, до масштабирования

        float x = margin;
        float y = screenHeight - margin - (textHeight * scale);

        context.pose().pushMatrix();
        context.pose().translate(x, y);
        context.pose().scale(scale, scale);
        context.drawString(client.font, text, 0, 0, (alpha << 24) | 0xFFFFFF);
        context.pose().popMatrix();
    }

    private static final float REAL_TIME_SCALE = 1.15f;

    private void drawRealTimeClock(GuiGraphics context, RealTimeClockFeature realTimeClock) {
        Minecraft client = Minecraft.getInstance();

        context.pose().pushMatrix();
        context.pose().translate(10f, 10f);
        context.pose().scale(REAL_TIME_SCALE, REAL_TIME_SCALE);
        context.drawString(client.font, realTimeClock.getFormattedTime(), 0, 0, 0xFFFFFFFF);
        context.pose().popMatrix();
    }

    private void drawFadingNotification(GuiGraphics context, FadingNotification notification) {
        Minecraft client = Minecraft.getInstance();
        String text = notification.getText();

        int alpha = (int) (notification.getAlpha() * 255 * 0.7f);
        int color = (alpha << 24) | notification.getColor(); // цвет свой для каждого биома

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int textWidth = client.font.width(text);
        int x = (screenWidth - textWidth) / 2;
        int y = 40; // чуть ниже верхнего края экрана

        context.drawString(client.font, text, x, y, color);
    }

    private void drawDamageNotification(GuiGraphics context, DamageNotification notification) {
        Minecraft client = Minecraft.getInstance();

        String text = String.format(
                "❤ -%.1f %s",
                notification.getHearts(),
                notification.getTargetName().getString()
        );

        float scale = 0.85f;
        int margin = 6;

        int alpha = (int) (notification.getAlpha() * 255 * 0.7f);
        int color = (alpha << 24) | 0xFF5555; // красноватый оттенок для урона

        int textWidth = client.font.width(text);
        float x = client.getWindow().getGuiScaledWidth() - (textWidth * scale) - margin;
        float y = margin;

        context.pose().pushMatrix();
        context.pose().translate(x, y);
        context.pose().scale(scale, scale);
        context.drawString(client.font, text, 0, 0, color);
        context.pose().popMatrix();
    }
}