package qtouch;

import javax.swing.*;
import java.awt.*;

/**
 * Splash screen for QTouch game (3 seconds).
 */
public class QTouchSplash extends JWindow {
    private final QTouchModel model;

    public QTouchSplash(QTouchModel model) {
        this.model = model;
        buildUI();
    }

    private void buildUI() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);

        JLabel img = new JLabel(new ImageIcon(model.IMAGE_PATH + "splash.png"), JLabel.CENTER);
        JLabel info = new JLabel("<html><center><font color='white'><b>QTouch - Qubit Touchdown</b><br>Developed by Heta Patel</font></center></html>", JLabel.CENTER);
        info.setFont(new Font("Arial", Font.BOLD, 16));

        panel.add(img, BorderLayout.CENTER);
        panel.add(info, BorderLayout.SOUTH);

        getContentPane().add(panel);
        setSize(600, 400);
        setLocationRelativeTo(null);
    }

    public void showSplash() {
        setVisible(true);
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
        setVisible(false);
    }
}
