package neonbreaker;

import java.awt.*;
import javax.swing.JPanel;
public final class GamePanel extends JPanel implements GameConfig {
    private static final long serialVersionUID = 1L;
    private GameEngine engine;

    public GamePanel() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(BACKGROUND);
        setFocusable(true);
    }
    public void setEngine(GameEngine engine) { this.engine = engine; }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0, 0, BACKGROUND, WINDOW_WIDTH, WINDOW_HEIGHT, BACKGROUND_END));
            g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
            if (engine == null) { return; }
            GameEngine.Snapshot s = engine.snapshot();
            drawField(g);
            drawHud(g, s);
            drawObjects(g, s);
            drawHelp(g, s);
            if (s.state() != GameEngine.State.PLAYING) { drawOverlay(g, s.state()); }
        } finally { g.dispose(); }
    }

    private void drawField(Graphics2D g) {
        g.setColor(GRID);
        for (int x = LEFT; x <= RIGHT; x += GRID_STEP) { g.drawLine(x, TOP, x, BOTTOM); }
        for (int y = TOP; y <= BOTTOM; y += GRID_STEP) { g.drawLine(LEFT, y, RIGHT, y); }
        g.setColor(MUTED);
        g.drawRoundRect(LEFT - 1, TOP - 1, RIGHT - LEFT + 2, BOTTOM - TOP + 2, CORNER, CORNER);
    }

    private void drawHud(Graphics2D g, GameEngine.Snapshot s) {
        g.setColor(CARD);
        g.fillRoundRect(LEFT, HUD_Y, RIGHT - LEFT, HUD_HEIGHT, CORNER, CORNER);
        text(g, "NEON / BREAKER", TITLE_X, TITLE_Y, TITLE_SIZE, CYAN, Font.BOLD);
        text(g, "АРКАНОИД · ЛОВИ РИТМ", TITLE_X, VALUE_Y, SMALL_SIZE, MUTED, Font.PLAIN);
        hudValue(g, "СЧЁТ", Integer.toString(s.score()), HUD_SCORE_X);
        hudValue(g, "СЕКТОР", s.level() + " / " + LEVEL_COUNT, HUD_LEVEL_X);
        hudValue(g, "ЖИЗНИ", Integer.toString(s.lives()), HUD_LIVES_X);
    }

    private void hudValue(Graphics2D g, String label, String value, int x) {
        text(g, label, x, LABEL_Y, SMALL_SIZE, MUTED, Font.PLAIN);
        text(g, value, x, VALUE_Y, HUD_SIZE, INK, Font.BOLD);
    }

    private void drawObjects(Graphics2D g, GameEngine.Snapshot s) {
        for (GameEngine.BrickView brick : s.bricks()) {
            g.setColor(BRICK_COLORS[brick.row()]);
            g.fillRoundRect(brick.x(), brick.y(), BRICK_WIDTH, BRICK_HEIGHT, SMALL_CORNER, SMALL_CORNER);
            if (brick.hp() > 1) {
                g.setColor(INK);
                g.drawRoundRect(brick.x() + BRICK_INSET, brick.y() + BRICK_INSET,
                        BRICK_WIDTH - HALF * BRICK_INSET, BRICK_HEIGHT - HALF * BRICK_INSET,
                        SMALL_CORNER, SMALL_CORNER);
                g.fillOval(brick.x() + BRICK_WIDTH - HP_MARK_OFFSET,
                        brick.y() + BRICK_HEIGHT / HALF - HP_MARK_SIZE / HALF, HP_MARK_SIZE, HP_MARK_SIZE);
            }
        }
        int alpha = TRAIL_ALPHA;
        for (GameEngine.Point point : s.trail()) {
            g.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), Math.max(0, alpha)));
            g.fillOval((int) point.x() - BALL_RADIUS, (int) point.y() - BALL_RADIUS,
                    HALF * BALL_RADIUS, HALF * BALL_RADIUS);
            alpha -= TRAIL_ALPHA_STEP;
        }
        g.setColor(INK);
        g.fillOval((int) s.ballX() - BALL_RADIUS, (int) s.ballY() - BALL_RADIUS,
                HALF * BALL_RADIUS, HALF * BALL_RADIUS);
        g.setColor(s.wideSeconds() > 0 ? GOLD : CYAN);
        g.fillRoundRect((int) s.paddleX() - s.paddleWidth() / HALF, PADDLE_Y,
                s.paddleWidth(), PADDLE_HEIGHT, PADDLE_HEIGHT, PADDLE_HEIGHT);
        if (s.bonusActive()) {
            int x = (int) s.bonusX() - BONUS_SIZE / HALF;
            int y = (int) s.bonusY() - BONUS_SIZE / HALF;
            g.setColor(GOLD);
            g.fillRoundRect(x, y, BONUS_SIZE, BONUS_SIZE, SMALL_CORNER, SMALL_CORNER);
            text(g, "+", x + BONUS_TEXT_OFFSET_X, y + BONUS_TEXT_OFFSET_Y, TEXT_SIZE, BACKGROUND, Font.BOLD);
        }
    }

    private void drawHelp(Graphics2D g, GameEngine.Snapshot s) {
        text(g, "МЫШЬ — платформа     SPACE / ЩЕЛЧОК — подача     P — пауза     N — новая игра     ESC — выход",
                LEFT, FOOTER_Y, TEXT_SIZE, INK, Font.PLAIN);
        String info = s.wideSeconds() > 0 ? "ШИРОКАЯ ПЛАТФОРМА: " + s.wideSeconds() + " с"
                : "Золотой + расширяет платформу на 10 с. Угол отскока зависит от точки удара.";
        text(g, info, LEFT, FOOTER_SECOND_Y, SMALL_SIZE, s.wideSeconds() > 0 ? GOLD : MUTED, Font.PLAIN);
    }

    private void drawOverlay(Graphics2D g, GameEngine.State state) {
        String title, help;
        switch (state) {
            case READY -> { title = "СЕКТОР ГОТОВ"; help = "Нажми SPACE или щёлкни, чтобы подать мяч"; }
            case PAUSED -> { title = "ПАУЗА"; help = "Нажми P, чтобы продолжить"; }
            case WON -> { title = "ВСЕ СЕКТОРЫ ПРОЙДЕНЫ"; help = "Победа! Нажми N для новой игры"; }
            case LOST -> { title = "ИГРА ОКОНЧЕНА"; help = "Нажми N и попробуй ещё раз"; }
            default -> { return; }
        }
        g.setColor(new Color(0, 0, 0, WASH_ALPHA));
        g.fillRect(LEFT, TOP, RIGHT - LEFT, BOTTOM - TOP);
        int x = (WINDOW_WIDTH - OVERLAY_WIDTH) / HALF;
        int y = (TOP + BOTTOM - OVERLAY_HEIGHT) / HALF;
        g.setColor(new Color(CARD.getRed(), CARD.getGreen(), CARD.getBlue(), OVERLAY_ALPHA));
        g.fillRoundRect(x, y, OVERLAY_WIDTH, OVERLAY_HEIGHT, CORNER, CORNER);
        g.setColor(CYAN);
        g.drawRoundRect(x, y, OVERLAY_WIDTH, OVERLAY_HEIGHT, CORNER, CORNER);
        centered(g, title, y + OVERLAY_TITLE_Y, HUD_SIZE, CYAN);
        centered(g, help, y + OVERLAY_TEXT_Y, TEXT_SIZE, INK);
    }

    private void centered(Graphics2D g, String value, int y, int size, Color color) {
        g.setFont(new Font(FONT, Font.BOLD, size));
        text(g, value, (WINDOW_WIDTH - g.getFontMetrics().stringWidth(value)) / HALF, y, size, color, Font.BOLD);
    }
    private void text(Graphics2D g, String value, int x, int y, int size, Color color, int style) {
        g.setFont(new Font(FONT, style, size));
        g.setColor(color);
        g.drawString(value, x, y);
    }
}
