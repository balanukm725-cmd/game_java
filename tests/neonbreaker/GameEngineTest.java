package neonbreaker;

import java.lang.reflect.Field;
import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
public final class GameEngineTest implements GameConfig {
    private static int checks;
    private GameEngineTest() { }
    public static void main(String[] args) throws Exception {
        GamePanel panel = new GamePanel();
        GameEngine game = new GameEngine(panel);
        panel.setEngine(game);
        check(game.snapshot().bricks().size() == BRICK_ROWS * BRICK_COLS, "initial bricks");
        check(game.snapshot().lives() == INITIAL_LIVES, "initial lives");
        game.movePaddle(-WINDOW_WIDTH);
        check(game.snapshot().paddleX() == LEFT + PADDLE_WIDTH / HALF, "left paddle clamp");
        game.movePaddle(WINDOW_WIDTH * HALF);
        check(game.snapshot().paddleX() == RIGHT - PADDLE_WIDTH / HALF, "right paddle clamp");
        check(game.snapshot().ballX() == game.snapshot().paddleX(), "ball attached before serve");
        game.serve();
        check(game.snapshot().state() == GameEngine.State.PLAYING, "serve");
        game.togglePause();
        double before = game.snapshot().ballY();
        game.update(1);
        check(game.snapshot().ballY() == before, "pause freezes physics");
        game.togglePause();
        game.update(0.01);
        check(game.snapshot().ballY() < before, "resume");

        ball(game, LEFT + BALL_RADIUS, 400, -BASE_SPEED, 0);
        game.update(0.01);
        check(number(game, "velocityX") > 0 && game.snapshot().ballX() >= LEFT + BALL_RADIUS, "left wall");
        ball(game, RIGHT - BALL_RADIUS, 400, BASE_SPEED, 0);
        game.update(0.01);
        check(number(game, "velocityX") < 0 && game.snapshot().ballX() <= RIGHT - BALL_RADIUS, "right wall");
        ball(game, WINDOW_WIDTH / HALF, TOP + BALL_RADIUS, 0, -BASE_SPEED);
        game.update(0.01);
        check(number(game, "velocityY") > 0, "top wall");

        game.movePaddle(WINDOW_WIDTH / HALF);
        ball(game, WINDOW_WIDTH / HALF, PADDLE_Y - BALL_RADIUS - 1, 0, BASE_SPEED);
        game.update(0.01);
        check(number(game, "velocityY") < 0 && Math.abs(number(game, "velocityX")) < 0.01, "center paddle bounce");
        ball(game, WINDOW_WIDTH / HALF + PADDLE_WIDTH / 3.0, PADDLE_Y - BALL_RADIUS - 1, 0, BASE_SPEED);
        game.update(0.01);
        check(number(game, "velocityY") < 0 && number(game, "velocityX") > 0, "edge changes bounce angle");

        game.newGame(); game.serve();
        GameEngine.BrickView first = game.snapshot().bricks().get(0);
        ball(game, first.x() + BRICK_WIDTH / HALF, first.y() - BALL_RADIUS - 1, 0, BASE_SPEED);
        game.update(0.01);
        check(game.snapshot().score() == POINTS_PER_HIT, "one contact scores once");
        check(game.snapshot().bricks().size() == BRICK_ROWS * BRICK_COLS - 1, "brick removed");
        game.newGame(); game.serve();
        bricks(game).clear();
        bricks(game).add(new Brick(WINDOW_WIDTH / HALF, BRICK_TOP, 0, MAX_BRICK_HP));
        first = game.snapshot().bricks().get(0);
        ball(game, first.x() - BALL_RADIUS - 1, first.y() + BRICK_HEIGHT / HALF, BASE_SPEED, 0);
        game.update(0.01);
        check(number(game, "velocityX") < 0, "side of brick reflects horizontally");

        game.newGame(); game.serve();
        for (int life = INITIAL_LIVES; life > 0; life--) {
            ball(game, LEFT + BALL_RADIUS, BOTTOM + BALL_RADIUS + 1, 0, BASE_SPEED);
            game.update(0.01);
            check(game.snapshot().lives() == life - 1, "life lost exactly once");
            game.serve();
        }
        check(game.snapshot().state() == GameEngine.State.LOST, "defeat");
        game.newGame();
        check(game.snapshot().score() == 0 && game.snapshot().lives() == INITIAL_LIVES, "restart resets");

        game.serve();
        set(game, "brokenBricks", BONUS_EVERY - 1);
        clearExceptFirst(game);
        bricks(game).add(new Brick(LEFT + BRICK_WIDTH + BRICK_GAP, BRICK_TOP, 0, 1));
        first = game.snapshot().bricks().get(0);
        ball(game, first.x() + BRICK_WIDTH / HALF, first.y() - BALL_RADIUS - 1, 0, BASE_SPEED);
        game.update(0.01);
        check(game.snapshot().bonusActive(), "seventh broken brick drops bonus");
        set(game, "bonusX", game.snapshot().paddleX());
        set(game, "bonusY", (double) PADDLE_Y - BONUS_SIZE / HALF);
        ball(game, WINDOW_WIDTH / HALF, 450, BASE_SPEED, 0);
        game.update(0.01);
        check(game.snapshot().paddleWidth() == WIDE_PADDLE_WIDTH, "catch expands paddle");
        set(game, "wideRemaining", 0.001);
        game.update(0.01);
        check(game.snapshot().paddleWidth() == PADDLE_WIDTH, "bonus expires");
        set(game, "bonusActive", true);
        set(game, "bonusY", (double) BOTTOM + BONUS_SIZE + 1);
        game.update(0.01);
        check(!game.snapshot().bonusActive(), "missed bonus removed");

        game.newGame(); game.serve();
        clearExceptFirst(game);
        strikeFirst(game);
        check(game.snapshot().level() == 2 && game.snapshot().state() == GameEngine.State.READY, "next level waits for serve");
        check(game.snapshot().bricks().get(0).hp() == MAX_BRICK_HP, "armored bricks on level two");
        game.serve();
        int count = game.snapshot().bricks().size();
        strikeFirst(game);
        check(game.snapshot().bricks().size() == count && game.snapshot().bricks().get(0).hp() == 1,
                "armor takes one hit without destruction");
        set(game, "level", LEVEL_COUNT);
        clearExceptFirst(game);
        strikeFirst(game);
        check(game.snapshot().state() == GameEngine.State.WON, "final victory");
        before = game.snapshot().ballY();
        game.update(1); game.serve();
        check(game.snapshot().ballY() == before && game.snapshot().state() == GameEngine.State.WON, "victory is stable");

        game.newGame(); game.start(); game.start();
        Thread thread = (Thread) field("loopThread").get(game);
        check(thread.isAlive(), "loop starts once");
        game.stop(); thread.join(1000);
        check(!thread.isAlive(), "loop stops on interrupt");

        if (args.length > 0) {
            game.newGame();
            save(panel, args[0] + "/preview-ready.png");
            game.serve();
            for (int i = 0; i < 100; i++) { game.update(0.008); }
            save(panel, args[0] + "/preview-playing.png");
        }
        System.out.println("PASS: " + checks + " checks");
    }
    private static void strikeFirst(GameEngine game) throws Exception {
        GameEngine.BrickView b = game.snapshot().bricks().get(0);
        ball(game, b.x() + BRICK_WIDTH / HALF, b.y() - BALL_RADIUS - 1, 0, BASE_SPEED);
        game.update(0.01);
    }
    private static void clearExceptFirst(GameEngine game) throws Exception {
        List<Brick> list = bricks(game);
        for (int i = 1; i < list.size(); i++) { while (list.get(i).isAlive()) { list.get(i).hit(); } }
    }
    @SuppressWarnings("unchecked")
    private static List<Brick> bricks(GameEngine game) throws Exception { return (List<Brick>) field("bricks").get(game); }
    private static void ball(GameEngine game, double x, double y, double vx, double vy) throws Exception {
        set(game, "ballX", x); set(game, "ballY", y);
        set(game, "velocityX", vx); set(game, "velocityY", vy);
    }
    private static Field field(String name) throws Exception {
        Field f = GameEngine.class.getDeclaredField(name); f.setAccessible(true); return f;
    }
    private static void set(GameEngine game, String name, Object value) throws Exception { field(name).set(game, value); }
    private static double number(GameEngine game, String name) throws Exception { return field(name).getDouble(game); }
    private static void check(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); } checks++;
    }
    private static void save(GamePanel panel, String path) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            panel.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
            BufferedImage image = new BufferedImage(WINDOW_WIDTH, WINDOW_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics(); panel.paint(g); g.dispose();
            try { ImageIO.write(image, "png", new File(path)); }
            catch (Exception e) { throw new RuntimeException(e); }
        });
    }
}
