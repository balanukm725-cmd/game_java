package neonbreaker;

import java.awt.event.*;
import javax.swing.JFrame;
public final class GameController implements MouseMotionListener, MouseListener,
        KeyListener, WindowListener, GameConfig {
    private final GameEngine engine;
    private final JFrame window;
    private final GamePanel panel;

    public GameController(GameEngine engine, JFrame window, GamePanel panel) {
        this.engine = engine;
        this.window = window;
        this.panel = panel;
    }
    @Override public void mouseMoved(MouseEvent e) { engine.movePaddle(e.getX()); }
    @Override public void mouseDragged(MouseEvent e) { mouseMoved(e); }
    @Override public void mousePressed(MouseEvent e) {
        panel.requestFocusInWindow();
        if (e.getButton() == MouseEvent.BUTTON1) { engine.serve(); }
    }
    @Override public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KEY_NEW) { engine.newGame(); }
        else if (e.getKeyCode() == KEY_SERVE) { engine.serve(); }
        else if (e.getKeyCode() == KEY_PAUSE) { engine.togglePause(); }
        else if (e.getKeyCode() == KEY_EXIT) { window.dispose(); }
    }
    @Override public void windowOpened(WindowEvent e) { panel.requestFocusInWindow(); }
    @Override public void windowClosing(WindowEvent e) { engine.stop(); }
    @Override public void windowClosed(WindowEvent e) { engine.stop(); }
    @Override public void windowDeactivated(WindowEvent e) {
        if (engine.snapshot().state() == GameEngine.State.PLAYING) { engine.togglePause(); }
    }
    @Override public void mouseClicked(MouseEvent e) { }
    @Override public void mouseReleased(MouseEvent e) { }
    @Override public void mouseEntered(MouseEvent e) { }
    @Override public void mouseExited(MouseEvent e) { }
    @Override public void keyReleased(KeyEvent e) { }
    @Override public void keyTyped(KeyEvent e) { }
    @Override public void windowActivated(WindowEvent e) { }
    @Override public void windowIconified(WindowEvent e) { }
    @Override public void windowDeiconified(WindowEvent e) { }
}
