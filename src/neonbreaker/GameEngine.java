package neonbreaker;

import java.util.ArrayList;
import java.util.List;
public final class GameEngine implements Runnable, GameConfig {
    public enum State { READY, PLAYING, PAUSED, WON, LOST }
    public record BrickView(int x, int y, int row, int hp) { }
    public record Point(double x, double y) { }
    public record Snapshot(State state, int score, int lives, int level,
            double paddleX, int paddleWidth, double ballX, double ballY,
            List<BrickView> bricks, List<Point> trail, boolean bonusActive,
            double bonusX, double bonusY, int wideSeconds) { }

    private final GamePanel panel;
    private final List<Brick> bricks = new ArrayList<>();
    private final List<Point> trail = new ArrayList<>();
    private Thread loopThread;
    private volatile boolean running;
    private State state;
    private int score, lives, level, brokenBricks;
    private double paddleX, ballX, ballY, velocityX, velocityY;
    private double bonusX, bonusY, wideRemaining;
    private boolean bonusActive;

    public GameEngine(GamePanel panel) {
        this.panel = panel;
        newGame();
    }

    public synchronized void newGame() {
        score = 0;
        lives = INITIAL_LIVES;
        level = 1;
        brokenBricks = 0;
        paddleX = (LEFT + RIGHT) / (double) HALF;
        prepareLevel();
    }

    private void prepareLevel() {
        bricks.clear();
        for (int row = 0; row < BRICK_ROWS; row++) {
            for (int col = 0; col < BRICK_COLS; col++) {
                int hp = level > 1 && row < TOUGH_ROW_COUNT ? MAX_BRICK_HP : 1;
                bricks.add(new Brick(LEFT + col * (BRICK_WIDTH + BRICK_GAP),
                        BRICK_TOP + row * (BRICK_HEIGHT + BRICK_GAP), row, hp));
            }
        }
        resetServe();
    }

    private void resetServe() {
        state = State.READY;
        wideRemaining = 0;
        bonusActive = false;
        movePaddle(paddleX);
        attachBall();
        trail.clear();
    }

    private void attachBall() {
        ballX = paddleX;
        ballY = PADDLE_Y - BALL_RADIUS - SERVE_GAP;
    }

    public synchronized void movePaddle(double x) {
        if (state == State.PAUSED || state == State.LOST || state == State.WON) { return; }
        double halfWidth = paddleWidth() / (double) HALF;
        paddleX = clamp(x, LEFT + halfWidth, RIGHT - halfWidth);
        if (state == State.READY) { attachBall(); }
    }

    public synchronized void serve() {
        if (state != State.READY) { return; }
        double speed = speed();
        velocityX = speed * SERVE_X_RATIO;
        velocityY = -Math.sqrt(speed * speed - velocityX * velocityX);
        state = State.PLAYING;
    }

    public synchronized void togglePause() {
        if (state == State.PLAYING) { state = State.PAUSED; }
        else if (state == State.PAUSED) { state = State.PLAYING; }
    }

    public synchronized void start() {
        if (loopThread != null) { return; }
        running = true;
        loopThread = new Thread(this, "neon-breaker-loop");
        loopThread.setDaemon(true);
        loopThread.start();
    }

    public synchronized void stop() {
        running = false;
        if (loopThread != null) { loopThread.interrupt(); }
    }

    @Override public void run() {
        long previous = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            double elapsedMs = Math.min((now - previous) / (double) NANOS_PER_MILLI,
                    MAX_ELAPSED_MS);
            previous = now;
            update(elapsedMs / MILLIS_PER_SECOND);
            panel.repaint();
            try { Thread.sleep(FRAME_MS); }
            catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        running = false;
    }

    synchronized void update(double seconds) {
        if (state != State.PLAYING) { return; }
        double dt = clamp(seconds, 0, MAX_ELAPSED_MS / (double) MILLIS_PER_SECOND);
        wideRemaining = Math.max(0, wideRemaining - dt);
        movePaddle(paddleX);
        trail.add(0, new Point(ballX, ballY));
        if (trail.size() > TRAIL_LENGTH) { trail.remove(trail.size() - 1); }
        for (int step = 0; step < PHYSICS_STEPS && state == State.PLAYING; step++) {
            stepBall(dt / PHYSICS_STEPS);
        }
        if (state == State.PLAYING) { updateBonus(dt); }
    }

    private void stepBall(double dt) {
        double oldX = ballX, oldY = ballY;
        ballX += velocityX * dt;
        ballY += velocityY * dt;
        if (ballX - BALL_RADIUS < LEFT) {
            ballX = LEFT + BALL_RADIUS;
            velocityX = Math.abs(velocityX);
        } else if (ballX + BALL_RADIUS > RIGHT) {
            ballX = RIGHT - BALL_RADIUS;
            velocityX = -Math.abs(velocityX);
        }
        if (ballY - BALL_RADIUS < TOP) {
            ballY = TOP + BALL_RADIUS;
            velocityY = Math.abs(velocityY);
        }
        double halfWidth = paddleWidth() / (double) HALF;
        if (velocityY > 0 && oldY + BALL_RADIUS <= PADDLE_Y
                && ballY + BALL_RADIUS >= PADDLE_Y
                && ballX + BALL_RADIUS >= paddleX - halfWidth
                && ballX - BALL_RADIUS <= paddleX + halfWidth) {
            double relative = clamp((ballX - paddleX) / halfWidth, -1, 1);
            double angle = relative * MAX_BOUNCE_ANGLE;
            velocityX = speed() * Math.sin(angle);
            velocityY = -speed() * Math.cos(angle);
            ballY = PADDLE_Y - BALL_RADIUS;
        }
        hitBrick(oldX, oldY);
        if (ballY - BALL_RADIUS > BOTTOM) {
            lives--;
            resetServe();
            if (lives == 0) { state = State.LOST; }
        }
    }

    private void hitBrick(double oldX, double oldY) {
        for (Brick brick : bricks) {
            if (!brick.isAlive() || !intersects(ballX, ballY, BALL_RADIUS,
                    brick.getX(), brick.getY(), BRICK_WIDTH, BRICK_HEIGHT)) { continue; }
            if (oldY + BALL_RADIUS <= brick.getY()) {
                ballY = brick.getY() - BALL_RADIUS;
                velocityY = -Math.abs(velocityY);
            } else if (oldY - BALL_RADIUS >= brick.getY() + BRICK_HEIGHT) {
                ballY = brick.getY() + BRICK_HEIGHT + BALL_RADIUS;
                velocityY = Math.abs(velocityY);
            } else if (oldX < brick.getX()) {
                ballX = brick.getX() - BALL_RADIUS;
                velocityX = -Math.abs(velocityX);
            } else {
                ballX = brick.getX() + BRICK_WIDTH + BALL_RADIUS;
                velocityX = Math.abs(velocityX);
            }
            brick.hit();
            score += POINTS_PER_HIT;
            if (!brick.isAlive()) {
                brokenBricks++;
                if (brokenBricks % BONUS_EVERY == 0 && !bonusActive) {
                    bonusX = brick.getX() + BRICK_WIDTH / (double) HALF;
                    bonusY = brick.getY();
                    bonusActive = true;
                }
            }
            if (bricks.stream().noneMatch(Brick::isAlive)) {
                if (level == LEVEL_COUNT) { state = State.WON; }
                else { level++; prepareLevel(); }
            }
            break;
        }
    }

    private void updateBonus(double dt) {
        if (!bonusActive) { return; }
        bonusY += BONUS_SPEED * dt;
        if (bonusY + BONUS_SIZE / (double) HALF >= PADDLE_Y
                && bonusY - BONUS_SIZE / (double) HALF <= PADDLE_Y + PADDLE_HEIGHT
                && Math.abs(bonusX - paddleX) <= (paddleWidth() + BONUS_SIZE) / (double) HALF) {
            wideRemaining = BONUS_DURATION_MS / (double) MILLIS_PER_SECOND;
            bonusActive = false;
            movePaddle(paddleX);
        } else if (bonusY - BONUS_SIZE > BOTTOM) { bonusActive = false; }
    }

    private boolean intersects(double x, double y, double radius,
            int rx, int ry, int width, int height) {
        double dx = x - clamp(x, rx, rx + width);
        double dy = y - clamp(y, ry, ry + height);
        return dx * dx + dy * dy <= radius * radius;
    }

    private double speed() { return Math.min(MAX_SPEED, BASE_SPEED + (level - 1) * LEVEL_SPEED_STEP); }
    private int paddleWidth() { return wideRemaining > 0 ? WIDE_PADDLE_WIDTH : PADDLE_WIDTH; }
    private double clamp(double value, double min, double max) { return Math.max(min, Math.min(max, value)); }

    public synchronized Snapshot snapshot() {
        List<BrickView> view = new ArrayList<>();
        for (Brick brick : bricks) {
            if (brick.isAlive()) {
                view.add(new BrickView(brick.getX(), brick.getY(), brick.getRow(), brick.getHitPoints()));
            }
        }
        return new Snapshot(state, score, lives, level, paddleX, paddleWidth(), ballX, ballY,
                List.copyOf(view), List.copyOf(trail), bonusActive, bonusX, bonusY,
                (int) Math.ceil(wideRemaining));
    }
}
