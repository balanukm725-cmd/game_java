package neonbreaker;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public final class Main {
    private Main() { }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::openWindow);
    }
    private static void openWindow() {
        GamePanel panel = new GamePanel();
        GameEngine engine = new GameEngine(panel);
        panel.setEngine(engine);
        JFrame window = new JFrame("Neon Breaker | Java Swing");
        GameController controller = new GameController(engine, window, panel);
        panel.addMouseMotionListener(controller);
        panel.addMouseListener(controller);
        panel.addKeyListener(controller);
        window.addWindowListener(controller);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.setContentPane(panel);
        window.setResizable(false);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        engine.start();
    }
}
