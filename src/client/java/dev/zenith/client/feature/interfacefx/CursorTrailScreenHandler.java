package dev.zenith.client.feature.interfacefx;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.GuiGraphics;

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
    private static final double MIN_DISTANCE_BETWEEN_POINTS = 7.0;

    private static final int MAX_DOT_SIZE = 5;
    private static final int MIN_DOT_SIZE = 1;

    // Сколько маленьких частиц-спутников создаётся вокруг каждой "основной" точки.
    // Уменьшено с 3 до 1 — облако стало легче и не забивает экран.
    private static final int SATELLITES_PER_POINT = 1;
    private static final double SATELLITE_SPREAD = 4.0;

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

    private static final Random RANDOM = new Random();

    private CursorTrailScreenHandler() {
    }

    public static void register(CursorTrailFeature feature) {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!feature.isEnabled()) {
                return;
            }

            // Каждый открытый экран получает собственное, независимое состояние следа —
            // хранится в замыкании, а не в поле класса, чтобы разные экраны не путались.
            TrailState state = new TrailState();

            ScreenEvents.afterRender(screen).register((s, context, mouseX, mouseY, tickDelta) -> {
                updateIdleTimer(state, mouseX, mouseY);
                addPointIfMoved(state, mouseX, mouseY);
                removeExpiredPoints(state.points);
                drawTrail(context, state);
            });
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
            double dx = mouseX - last.x();
            double dy = mouseY - last.y();
            if (dx * dx + dy * dy < MIN_DISTANCE_BETWEEN_POINTS * MIN_DISTANCE_BETWEEN_POINTS) {
                return; // мышь недостаточно сдвинулась — новую точку не добавляем
            }
        }

        long now = System.currentTimeMillis();

        // Основная точка — точно на курсоре.
        points.addLast(new TrailPoint(mouseX, mouseY, now));

        // Спутники вокруг неё со случайным смещением — лёгкое "облачко", а не голая точка.
        for (int i = 0; i < SATELLITES_PER_POINT; i++) {
            double offsetX = (RANDOM.nextDouble() - 0.5) * 2 * SATELLITE_SPREAD;
            double offsetY = (RANDOM.nextDouble() - 0.5) * 2 * SATELLITE_SPREAD;
            points.addLast(new TrailPoint(
                    (int) Math.round(mouseX + offsetX),
                    (int) Math.round(mouseY + offsetY),
                    now
            ));
        }
    }

    private static void removeExpiredPoints(Deque<TrailPoint> points) {
        long now = System.currentTimeMillis();
        while (!points.isEmpty() && now - points.peekFirst().spawnTimeMs() >= POINT_LIFETIME_MS) {
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
            float age = (now - point.spawnTimeMs()) / (float) POINT_LIFETIME_MS; // 0.0 (новая) .. 1.0 (истекла)
            float lifeLeft = 1.0f - age;

            // Квадратичное угасание — точка почти всё время жизни едва заметна
            // и по-настоящему гаснет только в последний момент, а не тает линейно.
            float fade = lifeLeft * lifeLeft * idleFactor;

            int alpha = (int) (fade * PEAK_ALPHA);
            if (alpha <= 0) {
                continue;
            }

            int size = MIN_DOT_SIZE + (int) (fade * (MAX_DOT_SIZE - MIN_DOT_SIZE));
            drawSoftDot(context, point.x(), point.y(), size, alpha);
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
        int color = (alpha << 24) | 0xFFFFFF;
        int half = size / 2;
        context.fill(x - half, y - half, x + half + 1, y + half + 1, color);
    }

    private record TrailPoint(int x, int y, long spawnTimeMs) {}

    /** Хранит точки следа, а также сырую позицию курсора и время его последнего движения. */
    private static final class TrailState {
        final Deque<TrailPoint> points = new ArrayDeque<>();
        long lastMoveTimeMs = System.currentTimeMillis();
        double lastRawX = Double.NaN;
        double lastRawY = Double.NaN;
    }
}