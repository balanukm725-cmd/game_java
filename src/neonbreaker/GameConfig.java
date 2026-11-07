package neonbreaker;

import java.awt.Color;
import java.awt.event.KeyEvent;
public interface GameConfig {
    int WINDOW_WIDTH = 960, WINDOW_HEIGHT = 720;
    int LEFT = 28, RIGHT = WINDOW_WIDTH - LEFT, TOP = 112, BOTTOM = 638;
    int HALF = 2;
    int FPS = 120, FRAME_MS = 1000 / FPS, MAX_ELAPSED_MS = 40;
    int PADDLE_WIDTH = 116, WIDE_PADDLE_WIDTH = 174, PADDLE_HEIGHT = 14;
    int PADDLE_Y = BOTTOM - 32, BALL_RADIUS = 8, SERVE_GAP = 3;
    double BASE_SPEED = 360, LEVEL_SPEED_STEP = 35, MAX_SPEED = 550;
    double MAX_BOUNCE_ANGLE = Math.toRadians(65), SERVE_X_RATIO = 0.32;
    int PHYSICS_STEPS = 4, INITIAL_LIVES = 3, LEVEL_COUNT = 3;
    int BRICK_ROWS = 5, BRICK_COLS = 10, BRICK_GAP = 8;
    int BRICK_TOP = 154, BRICK_HEIGHT = 24;
    int BRICK_WIDTH = (RIGHT - LEFT - (BRICK_COLS - 1) * BRICK_GAP) / BRICK_COLS;
    int TOUGH_ROW_COUNT = 2, MAX_BRICK_HP = 2, POINTS_PER_HIT = 10;
    int BONUS_EVERY = 7, BONUS_SIZE = 22, BONUS_DURATION_MS = 10000;
    double BONUS_SPEED = 150;
    int KEY_NEW = KeyEvent.VK_N, KEY_SERVE = KeyEvent.VK_SPACE;
    int KEY_PAUSE = KeyEvent.VK_P, KEY_EXIT = KeyEvent.VK_ESCAPE;
    Color BACKGROUND = new Color(9, 15, 30), BACKGROUND_END = new Color(19, 32, 52);
    Color CARD = new Color(19, 32, 50), GRID = new Color(30, 46, 64);
    Color INK = new Color(235, 244, 250), MUTED = new Color(145, 166, 187);
    Color CYAN = new Color(84, 230, 218), GOLD = new Color(255, 205, 115);
    Color[] BRICK_COLORS = {new Color(250, 113, 132), new Color(248, 160, 119),
        new Color(244, 206, 113), new Color(113, 200, 191), new Color(122, 155, 231)};
    String FONT = "SansSerif";
    int TITLE_SIZE = 26, TEXT_SIZE = 14, HUD_SIZE = 22, SMALL_SIZE = 12;
    int HUD_Y = 27, HUD_HEIGHT = 66, TITLE_X = 48, TITLE_Y = 56;
    int HUD_SCORE_X = 460, HUD_LEVEL_X = 650, HUD_LIVES_X = 800;
    int LABEL_Y = 48, VALUE_Y = 75, GRID_STEP = 32;
    int CORNER = 16, SMALL_CORNER = 8, BORDER_STROKE = 2;
    int FOOTER_Y = 670, FOOTER_SECOND_Y = 694;
    int OVERLAY_WIDTH = 480, OVERLAY_HEIGHT = 128, OVERLAY_TITLE_Y = 48;
    int OVERLAY_TEXT_Y = 83, OVERLAY_ALPHA = 235, WASH_ALPHA = 100;
    int TRAIL_LENGTH = 10, TRAIL_ALPHA = 105, TRAIL_ALPHA_STEP = 9;
    int BRICK_INSET = 2, HP_MARK_SIZE = 4, HP_MARK_OFFSET = 10;
    int BONUS_TEXT_OFFSET_X = 5, BONUS_TEXT_OFFSET_Y = 16;
    int MILLIS_PER_SECOND = 1000, NANOS_PER_MILLI = 1_000_000;
    double MIN_STEP_SECONDS = 0.001;
}
