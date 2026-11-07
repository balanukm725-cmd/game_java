package neonbreaker;
public final class Brick {
    private final int x, y, row;
    private int hitPoints;

    public Brick(int x, int y, int row, int hitPoints) {
        this.x = x;
        this.y = y;
        this.row = row;
        this.hitPoints = hitPoints;
    }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getRow() { return row; }
    public int getHitPoints() { return hitPoints; }
    public boolean isAlive() { return hitPoints > 0; }
    public void hit() { if (isAlive()) { hitPoints--; } }
}
