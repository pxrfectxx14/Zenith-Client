package dev.zenith.client.feature.interfacefx;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

/**
 * Рисует плавно затухающий призрачный след из частиц за курсором мыши
 * на любом GUI-экране (главное меню, инвентарь, настройки и т.д.).
 */
public final class CursorTrailScreenHandler {

    // Сколько миллисекунд живёт одна точка следа — увеличено для более длинного "хвоста".
    private static final long POINT_LIFETIME_MS = 750;

    // Минимальное расстояние между соседними точками — увеличено, чтобы точек
    // на одинаковом отрезке пути было заметно меньше (менее перегруженно).
    private static final double MIN_DISTANCE_BETWEEN_POINTS = 3.0;

    // Основной хвост — крупнее и ярче, это "тело" следа.
    private static final int CORE_MAX_SIZE = 9;
    private static final int CORE_MIN_SIZE = 2;
    private static final int CORE_PEAK_ALPHA = 90;

    // Искры-спутники — маленькие и тусклее, просто мерцающая пыль вокруг хвоста.
    private static final int SPARK_MAX_SIZE = 4;
    private static final int SPARK_MIN_SIZE = 1;
    private static final int SPARK_PEAK_ALPHA = 40;

    // Сколько маленьких частиц-спутников создаётся вокруг каждой "основной" точки.
    // Уменьшено с 3 до 1 — облако стало легче и не забивает экран.
    private static final int SATELLITES_PER_POINT = 4;
    private static final double SATELLITE_SPREAD = 3.0;

    // Максимальная прозрачность в момент появления (из 255) — понижена для более лёгкого следа.
    private static final int PEAK_ALPHA = 45;

    // Порог (в пикселях), при превышении которого курсор за один кадр считается "двинувшимся".
    // Не связан с MIN_DISTANCE_BETWEEN_POINTS — так минимальное дрожание руки
    // не будет постоянно сбрасывать таймер простоя.
    private static final double IDLE_MOVEMENT_EPSILON = 0.5;

    // Через сколько миллисекунд неподвижности курсора след начинает гаснуть принудительно.
    private static final long IDLE_GRACE_MS = 40;
    // За сколько миллисекунд после этого след гаснет полностью.
    private static final long IDLE_FADE_OUT_MS = 150;

    // Насколько сильно спутники разлетаются в стороны при рождении (пикселей в кадр).
    private static final double DRIFT_SPEED = 1.4;

    // Коэффициент торможения дрейфа за кадр (0..1). Чем ближе к 1 — тем дольше искра "плывёт".
    private static final double DRIFT_DRAG = 0.92;

    // Скорость мерцания размера искры (циклов в секунду).
    private static final double TWINKLE_SPEED_HZ = 3.0;

    // Насколько сильно мерцание меняет итоговый размер (0.3 = ±30%).
    private static final double TWINKLE_STRENGTH = 0.3;

    private static final ResourceLocation SPARK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("zenith-client", "textures/gui/sprites/spark.png");
    private static final int SPARK_TEXTURE_SIZE = 32;

    private static final Random RANDOM = new Random();

    private CursorTrailScreenHandler() {
    }

    public static void register(CursorTrailFeature feature) {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!feature.isEnabled()) {
                return;
            }

            TrailState state = new TrailState();
            SystemCursorHider cursorHider = new SystemCursorHider();
            GlowCursorRenderer cursorRenderer = new GlowCursorRenderer();
            AmbientCursorGlow ambientGlow = new AmbientCursorGlow();

            cursorHider.hide();

            ScreenEvents.afterRender(screen).register((s, context, mouseX, mouseY, tickDelta) -> {
                updateIdleTimer(state, mouseX, mouseY);
                addPointIfMoved(state, mouseX, mouseY);
                applyDrift(state.points);
                removeExpiredPoints(state.points);

                ambientGlow.update(mouseX, mouseY);
                ambientGlow.draw(context);   // рисуется первым — фоновое сияние
                drawTrail(context, state);   // затем след
                cursorRenderer.draw(context, mouseX, mouseY); // и сама стрелка поверх всего
            });

            ScreenEvents.remove(screen).register(s -> cursorHider.restore());
        });
    }

    /**
     * Проверяет, сдвинулся ли курсор хоть немного по сравнению с прошлым кадром,
     * независимо от порога спавна новых точек. Это единственный источник истины
     * для "мышь сейчас в покое" — специально отделён от логики добавления точек следа.
     */
    private static void updateIdleTimer(TrailState state, int mouseX, int mouseY) {
        double dx = mouseX - state.lastRawX;
        double dy = mouseY - state.lastRawY;

        if (Math.abs(dx) > IDLE_MOVEMENT_EPSILON || Math.abs(dy) > IDLE_MOVEMENT_EPSILON) {
            state.lastMoveTimeMs = System.currentTimeMillis();
        }

        state.lastRawX = mouseX;
        state.lastRawY = mouseY;
    }

    private static void addPointIfMoved(TrailState state, int mouseX, int mouseY) {
        Deque<TrailPoint> points = state.points;
        TrailPoint last = points.peekLast();

        if (last != null) {
            double dx = mouseX - last.x;
            double dy = mouseY - last.y;
            if (dx * dx + dy * dy < MIN_DISTANCE_BETWEEN_POINTS * MIN_DISTANCE_BETWEEN_POINTS) {
                return; // мышь недостаточно сдвинулась — новую точку не добавляем
            }
        }

        long now = System.currentTimeMillis();

        // Основная точка — точно на курсоре, без дрейфа (она держит форму следа).
        points.addLast(new TrailPoint(mouseX, mouseY, 0, 0, now, RANDOM.nextDouble() * Math.PI * 2, true));

        // Спутники — рождаются рядом и разлетаются в случайную сторону, постепенно тормозя.
        for (int i = 0; i < SATELLITES_PER_POINT; i++) {
            double offsetX = (RANDOM.nextDouble() - 0.5) * 2 * SATELLITE_SPREAD;
            double offsetY = (RANDOM.nextDouble() - 0.5) * 2 * SATELLITE_SPREAD;
            double angle = RANDOM.nextDouble() * Math.PI * 2;
            double speed = RANDOM.nextDouble() * DRIFT_SPEED;

            points.addLast(new TrailPoint(
                    mouseX + offsetX,
                    mouseY + offsetY,
                    Math.cos(angle) * speed,
                    Math.sin(angle) * speed,
                    now,
                    RANDOM.nextDouble() * Math.PI * 2,
                    false
            ));
        }
    }

    private static void applyDrift(Deque<TrailPoint> points) {
        for (TrailPoint point : points) {
            point.x += point.velocityX;
            point.y += point.velocityY;
            point.velocityX *= DRIFT_DRAG;
            point.velocityY *= DRIFT_DRAG;
        }
    }

    private static void removeExpiredPoints(Deque<TrailPoint> points) {
        long now = System.currentTimeMillis();
        while (!points.isEmpty() && now - points.peekFirst().spawnTimeMs >= POINT_LIFETIME_MS) {
            points.pollFirst();
        }
    }

    private static void drawTrail(GuiGraphics context, TrailState state) {
        long now = System.currentTimeMillis();

        long idleTime = now - state.lastMoveTimeMs;
        float idleFactor = 1.0f;
        if (idleTime > IDLE_GRACE_MS) {
            float pastGrace = idleTime - IDLE_GRACE_MS;
            idleFactor = Math.max(0f, 1.0f - pastGrace / IDLE_FADE_OUT_MS);
        }

        if (idleFactor <= 0f) {
            return; // курсор в покое дольше порога — след полностью невидим
        }

        for (TrailPoint point : state.points) {
            float age = (now - point.spawnTimeMs) / (float) POINT_LIFETIME_MS;
            float lifeLeft = 1.0f - age;

            // Квадратичное угасание — точка почти всё время жизни едва заметна
            // и по-настоящему гаснет только в последний момент, а не тает линейно.
            float fade = lifeLeft * lifeLeft * idleFactor;

            int peakAlpha = point.isCore ? CORE_PEAK_ALPHA : SPARK_PEAK_ALPHA;
            int minSize = point.isCore ? CORE_MIN_SIZE : SPARK_MIN_SIZE;
            int maxSize = point.isCore ? CORE_MAX_SIZE : SPARK_MAX_SIZE;

            int alpha = (int) (fade * peakAlpha);
            if (alpha <= 0) {
                continue;
            }

// У основного хвоста мерцание отключаем — он должен выглядеть как ровный
// плотный след, а не мигать. Мерцают только искры-спутники.
            double twinkle = point.isCore ? 1.0 : 1.0 + TWINKLE_STRENGTH * Math.sin(
                    now / 1000.0 * TWINKLE_SPEED_HZ * Math.PI * 2 + point.twinklePhase
            );
            int size = (int) Math.max(1, (minSize + fade * (maxSize - minSize)) * twinkle);
            drawSoftDot(context, (int) Math.round(point.x), (int) Math.round(point.y), size, alpha);
        }
    }

    /**
     * Рисует "мягкую" частицу двумя вложенными слоями убывающей прозрачности —
     * приближение размытого свечения без встроенного блюра в GUI Minecraft.
     */
    private static void drawSoftDot(GuiGraphics context, int x, int y, int size, int peakAlpha) {
        drawLayer(context, x, y, (int) (size * 1.8f), (int) (peakAlpha * 0.35f)); // мягкое гало
        drawLayer(context, x, y, size, peakAlpha);                                 // ядро
    }

    private static void drawLayer(GuiGraphics context, int x, int y, int size, int alpha) {
        if (alpha <= 0 || size <= 0) {
            return;
        }
        int argbColor = (alpha << 24) | 0xFFFFFF;
        int half = size / 2;
        context.blit(
                RenderPipelines.GUI_TEXTURED,
                SPARK_TEXTURE,
                x - half, y - half,
                0f, 0f,
                size, size,
                SPARK_TEXTURE_SIZE, SPARK_TEXTURE_SIZE,
                SPARK_TEXTURE_SIZE, SPARK_TEXTURE_SIZE,
                argbColor
        );
    }

    /**
     * Частица следа. В отличие от record — не неизменяемая, потому что
     * x/y и скорость дрейфа меняются каждый кадр (частица "отлетает" и тормозит).
     */
    private static final class TrailPoint {
        double x;
        double y;
        double velocityX;
        double velocityY;
        final long spawnTimeMs;
        final double twinklePhase;
        final boolean isCore; // true — основная точка хвоста, false — искра-спутник

        TrailPoint(double x, double y, double velocityX, double velocityY, long spawnTimeMs, double twinklePhase, boolean isCore) {
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.spawnTimeMs = spawnTimeMs;
            this.twinklePhase = twinklePhase;
            this.isCore = isCore;
        }
    }

    /** Хранит точки следа, а также сырую позицию курсора и время его последнего движения. */
    private static final class TrailState {
        final Deque<TrailPoint> points = new ArrayDeque<>();
        long lastMoveTimeMs = System.currentTimeMillis();
        double lastRawX = Double.NaN;
        double lastRawY = Double.NaN;
    }
}