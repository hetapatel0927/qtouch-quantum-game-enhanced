package qtouch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * View – handles the full GUI layout and menus.
 * Works with QTouchController and QTouchModel.
 */
public class QTouchView extends JFrame {

    private static final long serialVersionUID = 1L;

    // Shared objects for controller access
    public JPanel leftPanel, rightPanel;
    public JLabel player1Title, player2Title;
    public JLabel player1Score, player2Score;
    public JLabel currentPlayerLabel, timeLabel, diceLabel, drawCardLabel1, drawCardLabel2;
    public JButton rollButton, startPauseButton;
    public JMenuItem exitItem;
    public ImageIcon startIcon, pauseIcon;
    public JLabel centerCard;
    public javax.swing.Timer turnTimer;
    public JLabel boardLabel; // ✅ used by controller to update the board
    

    private final QTouchModel model;
    private ResourceBundle messages;
    private Locale currentLocale;

    public QTouchView(QTouchModel model) {
        this.model = model;
        this.currentLocale = Locale.ENGLISH;
        loadMessages();

        setTitle("Qubit Touchdown - Heta Patel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setupMenuBar();
        setupMainContent();
        add(makeFooter(), BorderLayout.SOUTH);

        setSize(1000, 700);
        setLocationRelativeTo(null);
    }

    /* ------------------------------------------------ */
    /* ---------------- Menu Bar Section -------------- */
    /* ------------------------------------------------ */
    private JMenuBar menuBar;
    private JMenu gameMenu, settingsMenu, languageMenu, lookFeelMenu, colorMenu, helpMenu;
    public JMenuItem newGameItem, aboutItem;
    

    private void setupMenuBar() {
        menuBar = new JMenuBar();

        //  Game Menu
        gameMenu = new JMenu(messages.getString("game"));
        newGameItem = new JMenuItem(messages.getString("newgame"));
        exitItem = new JMenuItem(messages.getString("exit"));
        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        //  Settings Menu
        settingsMenu = new JMenu(messages.getString("settings"));

        //  Language Menu
        languageMenu = new JMenu(messages.getString("language"));
        JMenuItem englishItem = new JMenuItem("English");
        JMenuItem frenchItem = new JMenuItem("Français");
        englishItem.addActionListener(e -> changeLanguage(Locale.ENGLISH));
        frenchItem.addActionListener(e -> changeLanguage(Locale.FRENCH));
        languageMenu.add(englishItem);
        languageMenu.add(frenchItem);

        //  Look & Feel Menu
        lookFeelMenu = new JMenu(messages.getString("lookfeel"));
        UIManager.LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo info : lafs) {
            JMenuItem lafItem = new JMenuItem(info.getName());
            lafItem.addActionListener(e -> changeLookAndFeel(info.getClassName()));
            lookFeelMenu.add(lafItem);
        }

        //  Color Menu
        colorMenu = new JMenu(messages.getString("colors"));
        String[] colorNames = {"Light Gray", "Light Blue", "Light Green", "Light Pink", "Yellow"};
        Color[] colors = {Color.LIGHT_GRAY, new Color(173,216,230),
                new Color(144,238,144), new Color(255,182,193), Color.YELLOW};
        for (int i = 0; i < colorNames.length; i++) {
            JMenuItem colorItem = new JMenuItem(colorNames[i]);
            final Color c = colors[i];
            colorItem.addActionListener(e -> changeBackgroundColor(c));
            colorMenu.add(colorItem);
        }
        colorMenu.addSeparator();
        JMenuItem customColor = new JMenuItem("Custom...");
        customColor.addActionListener(e -> {
            Color sel = JColorChooser.showDialog(this,
                    "Choose a Color", getContentPane().getBackground());
            if (sel != null) changeBackgroundColor(sel);
        });
        colorMenu.add(customColor);

        // Add submenus to Settings
        settingsMenu.add(languageMenu);
        settingsMenu.add(lookFeelMenu);
        settingsMenu.add(colorMenu);

        //  Help Menu
        helpMenu = new JMenu(messages.getString("help"));
        aboutItem = new JMenuItem(messages.getString("about"));
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                messages.getString("about_message"),
                messages.getString("about"),
                JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        // Add menus to bar
        menuBar.add(gameMenu);
        menuBar.add(settingsMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }
    /* ------------------- Main Layout ---------------- */
    /* ------------------------------------------------ */
    private void setupMainContent() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 10, 0, 10);

        // LEFT Player 1
        gbc.gridx = 0;
        gbc.weightx = 0.1;
        leftPanel = makePlayerPanel("Tyler Amin", Color.BLUE, true);
        mainPanel.add(leftPanel, gbc);

        // CENTER board
        gbc.gridx = 1;
        gbc.weightx = 0.9;
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2), "Board"));

        // ✅ Create main board label for dynamic updates
        boardLabel = new JLabel(loadScaledImage(model.IMAGE_PATH + "qtboard.jpg", 900, 600), JLabel.CENTER);
        centerPanel.add(boardLabel, BorderLayout.CENTER);

        // Start/Pause button
        startIcon = new ImageIcon(loadScaledImage(model.IMAGE_PATH + "start_logo.png", 110, 40).getImage());
        pauseIcon = new ImageIcon(loadScaledImage(model.IMAGE_PATH + "pause_logo.png", 110, 40).getImage());
        startPauseButton = new JButton(startIcon);
        startPauseButton.setFocusPainted(false);
        startPauseButton.setContentAreaFilled(false);
        startPauseButton.setBorder(BorderFactory.createEmptyBorder());
        centerPanel.add(startPauseButton, BorderLayout.SOUTH);
        mainPanel.add(centerPanel, gbc);

        // RIGHT Player 2
        gbc.gridx = 2;
        gbc.weightx = 0.1;
        rightPanel = makePlayerPanel("Ryan Reinalds", Color.RED, false);
        mainPanel.add(rightPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel makePlayerPanel(String name, Color color, boolean left) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 2),
                left ? "Player 1" : "Player 2"));

        JLabel title = new JLabel(name, JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(color);
        if (left) player1Title = title; else player2Title = title;
        panel.add(title, BorderLayout.NORTH);

        JPanel imagePanel = new JPanel(new BorderLayout());
        String file = left ? "RF.jpg" : "R.jpg";
        JLabel playerLabel = new JLabel(loadScaledImage(model.IMAGE_PATH + file, 200, 250));
        playerLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        imagePanel.add(playerLabel, BorderLayout.CENTER);

        JPanel cardPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        String[] cards = {"B.jpg","Z.jpg","S.jpg","Y.jpg"};
        for (String c : cards) {
            JLabel card = new JLabel(loadScaledImage(model.IMAGE_PATH + c, 100,150));
            card.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            cardPanel.add(card);
        }
        JLabel score = new JLabel("Touchdowns: 0", JLabel.CENTER);
        score.setFont(new Font("Arial", Font.BOLD, 12));
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(cardPanel, BorderLayout.CENTER);
        bottom.add(score, BorderLayout.SOUTH);
        imagePanel.add(bottom, BorderLayout.SOUTH);

        if (left) player1Score = score; else player2Score = score;
        panel.add(imagePanel, BorderLayout.CENTER);
        return panel;
    }

    /* ------------------------------------------------ */
    /* ------------------- Footer --------------------- */
    /* ------------------------------------------------ */
    private JPanel makeFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 0));
        footer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2), "Status"));

        JPanel drawPanel = new JPanel();
        drawPanel.setLayout(new BoxLayout(drawPanel, BoxLayout.Y_AXIS));
        JLabel drawImg = new JLabel(loadScaledImage(model.IMAGE_PATH + "0.jpg", 90, 140));
        drawImg.setAlignmentX(CENTER_ALIGNMENT);
        drawPanel.add(drawImg);
        drawCardLabel1 = new JLabel("Draw Card (24)", JLabel.CENTER);
        drawCardLabel1.setAlignmentX(CENTER_ALIGNMENT);
        drawPanel.add(drawCardLabel1);
        footer.add(drawPanel);

        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridy = 0;
        JPanel status = new JPanel(new GridLayout(1, 2));
        status.setBorder(BorderFactory.createTitledBorder("Team"));
        currentPlayerLabel = new JLabel("Current Player:", JLabel.CENTER);
        timeLabel = new JLabel("Time left:", JLabel.CENTER);
        timeLabel.setForeground(Color.RED);
        status.add(currentPlayerLabel);
        status.add(timeLabel);
        center.add(status, gbc);

        gbc.gridy = 1;
        JPanel dicePanel = new JPanel(new BorderLayout());
        dicePanel.setBorder(BorderFactory.createTitledBorder("Dice"));
        diceLabel = new JLabel("0", JLabel.CENTER);
        diceLabel.setFont(new Font("Arial", Font.BOLD, 28));
        rollButton = new JButton("Roll Dice");
        dicePanel.add(diceLabel, BorderLayout.CENTER);
        dicePanel.add(rollButton, BorderLayout.SOUTH);
        center.add(dicePanel, gbc);
        footer.add(center);

        JPanel discard = new JPanel();
        discard.setLayout(new BoxLayout(discard, BoxLayout.Y_AXIS));
        JLabel discardImg = new JLabel(loadScaledImage(model.IMAGE_PATH + "Y.jpg", 100, 150));
        discardImg.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        discard.add(discardImg);
        drawCardLabel2 = new JLabel("Discard Pile", JLabel.CENTER);
        discard.add(drawCardLabel2);
        footer.add(discard);
        return footer;
    }

    /* ------------------------------------------------ */
    /* ------------------- Utility -------------------- */
    /* ------------------------------------------------ */
    public ImageIcon loadScaledImage(String path, int w, int h) {
        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    private void changeLanguage(Locale locale) {
        currentLocale = locale;
        loadMessages();

        // rebuild the entire menu bar with new text
        setupMenuBar();
        SwingUtilities.updateComponentTreeUI(this);

        JOptionPane.showMessageDialog(
            this,
            messages.getString("language_changed") + " " + currentLocale.getDisplayLanguage(),
            messages.getString("language"),
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void changeLookAndFeel(String className) {
        try {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to change look & feel: " + e.getMessage());
        }
    }

    private void changeBackgroundColor(Color c) {
        getContentPane().setBackground(c);
        for (Component comp : getContentPane().getComponents())
            if (comp instanceof JPanel) ((JPanel) comp).setBackground(c);
        repaint();
    }
    
    private void loadMessages() {
        try {
        	messages = ResourceBundle.getBundle("qtouch.QGameMessages", currentLocale);
        }  catch (MissingResourceException e) {
            System.err.println(" Missing language file: " + e.getMessage());
            messages = null;
        }
    }
}
