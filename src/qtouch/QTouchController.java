package qtouch;

import javax.swing.*;
import java.awt.Container;
import java.awt.Font;
import java.awt.Image;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Color;

import java.awt.event.*;
import java.util.*;

public class QTouchController {
    private final QTouchModel model;
    private final QTouchView view;
    private javax.swing.Timer turnTimer;
    private final Random rng = new Random();
    private int player1Score = 0;
    private int player2Score = 0;
    private static final int WIN_SCORE = 3;
    private int touchdownTarget = 3; 

    private final String basePath = "C:\\CST8132 Homework\\PA\\img\\";
    private final List<String> deck = new ArrayList<>();

    private final Set<String> usedCards = new HashSet<>();

    public QTouchController(QTouchModel model, QTouchView view) {
        this.model = model;
        this.view = view;
    }

    // BUILD DECK WITH REPETITION
    private void rebuildDeck() {
        deck.clear();

        String[] allCards = {
            "B.jpg","Z.jpg","S.jpg","Y.jpg",
            "M.jpg","X.jpg","H.jpg","I.jpg",
            "cardP.jpg"
        };

        for (String c : allCards) {
            for (int i = 0; i < model.getCardRepetition(); i++) {
                deck.add(c);
            }
        }
        Collections.shuffle(deck);
    }

    public void initialize() {
        view.exitItem.addActionListener(e -> System.exit(0));
        view.startPauseButton.addActionListener(e -> toggleGame());
        view.rollButton.addActionListener(e -> rollDice());

        // Setup editing features
        setupNameEditing(view.player1Title, true);
        setupNameEditing(view.player2Title, false);
        setupImageChange(view.leftPanel, true);
        setupImageChange(view.rightPanel, false);

        // Attach card listeners
        attachCardListeners(view.leftPanel, true);
        attachCardListeners(view.rightPanel, false);

        // New Game menu item - FIXED: removed duplicate nested listener
        view.newGameItem.addActionListener(e -> {
            if (model.isGameStarted()) {
                int choice = JOptionPane.showConfirmDialog(
                        view,
                        "A game is currently in progress.\nDo you want to quit and start a new game?",
                        "Confirm New Game",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (choice != JOptionPane.YES_OPTION) return;
            }

            // RESET GAME STATE
            player1Score = 0;
            player2Score = 0;
            view.player1Score.setText("Score: 0");
            view.player2Score.setText("Score: 0");
            usedCards.clear();
            model.setPaused(false);
            model.setGameStarted(false);
            model.setCurrentPosition("0");

            // Get card repetition input
            String input = JOptionPane.showInputDialog(
                    view,
                    "Enter number of repetitions for each card type:",
                    "2"
            );

            int rep = 2;
            try { 
                rep = Integer.parseInt(input); 
            } catch (Exception ignored) {}
            model.setCardRepetition(rep);

            model.setRemainingCards(rep * 9);
            view.drawCardLabel1.setText("Draw Card (" + model.getRemainingCards() + ")");

            // Rebuild deck and assign cards
            rebuildDeck();
            usedCards.clear();
            assignUniqueRandomCards(view.leftPanel);
            assignUniqueRandomCards(view.rightPanel);

            // Reset board
            view.boardLabel.setIcon(view.loadScaledImage(model.IMAGE_PATH + "board.jpg", 900, 600));

            // Start game
            startGame();
        });

        // Initial setup
        rebuildDeck();
        view.boardLabel.setIcon(view.loadScaledImage(model.IMAGE_PATH + "board.jpg", 900, 600));
        view.setVisible(true);
    }



    private void setupNameEditing(JLabel label, boolean left) {
        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (!model.isGameStarted()) {
                    String newName = JOptionPane.showInputDialog(view,
                            "Enter new name for " + (left ? "Player 1" : "Player 2"),
                            label.getText());
                    if (newName != null && !newName.trim().isEmpty())
                        label.setText(newName.trim());
                } else msg("Cannot change name after game start!");
            }
        });
    }

    private void setupImageChange(Container sidePanel, boolean left) {
        for (Component comp : getAllComponents(sidePanel)) {
            if (comp instanceof JLabel lbl) {
                Icon icon = lbl.getIcon();
                if (icon instanceof ImageIcon img && img.getIconWidth() >= 190 && img.getIconHeight() >= 240) {
                    lbl.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) {
                            if (!model.isGameStarted()) {
                                JFileChooser fc = new JFileChooser();
                                fc.setDialogTitle("Select new image for " + (left ? "Player 1" : "Player 2"));
                                if (fc.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
                                    ImageIcon original = new ImageIcon(fc.getSelectedFile().getAbsolutePath());
                                    Image scaledImg = original.getImage()
                                            .getScaledInstance(200, 250, Image.SCALE_SMOOTH);
                                    ImageIcon scaledIcon = new ImageIcon(scaledImg);
                                    scaledIcon.setDescription(fc.getSelectedFile().getName());
                                    lbl.setIcon(scaledIcon);
                                }
                            } else msg("Cannot change image after game start!");
                        }
                    });
                    break;
                }
            }
        }
    }

    private JLabel findDiscardLabel1() {
        for (Component c : view.getContentPane().getComponents()) {
            if (c instanceof JPanel p) {
                for (Component sub : getAllComponents(p)) {
                    if (sub instanceof JLabel lbl) {
                        Icon ic = lbl.getIcon();
                        if (ic instanceof ImageIcon img &&
                                img.getIconWidth() >= 95 && img.getIconWidth() <= 110 &&
                                lbl.getText() == null) {
                            Container parent = lbl.getParent();
                            for (Component sib : parent.getComponents()) {
                                if (sib instanceof JLabel textLbl &&
                                        "Discard Pile".equals(textLbl.getText())) {
                                    return lbl;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void assignUniqueRandomCards(Container panel) {
        List<JLabel> cardLabels = getCardLabels(panel);
        Collections.shuffle(deck);
        for (JLabel card : cardLabels) {
            String nextCard = drawUniqueCard();

            ImageIcon original = new ImageIcon(model.IMAGE_PATH + nextCard);
            Image scaledImg = original.getImage()
                    .getScaledInstance(100, 150, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImg);
            scaledIcon.setDescription(nextCard);
            card.setIcon(scaledIcon);
        }
    }

    private String drawUniqueCard() {
        for (String c : deck) {
            if (!usedCards.contains(c)) {
                usedCards.add(c);
                return c;
            }
        }
        // IF EMPTY → FIX
        usedCards.clear();
        rebuildDeck();
        return deck.get(rng.nextInt(deck.size()));
    }

    private void attachCardListeners(Container panel, boolean leftSide) {
        for (JLabel card : getCardLabels(panel)) {
            card.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (!model.isGameStarted() || model.isPaused()) {
                        msg("Start the game first!");
                        return;
                    }
                    if ((leftSide && !model.isPlayer1Turn()) || (!leftSide && model.isPlayer1Turn()))
                        return;

                    JLabel discardImg = findDiscardLabel1();
                    if (discardImg != null) discardImg.setIcon(card.getIcon());

                    String cardName = null;
                    Icon icon = card.getIcon();
                    if (icon instanceof ImageIcon img) {
                        cardName = img.getDescription();
                    }
                    if (cardName == null || cardName.isEmpty()) {
                        msg("DEBUG: Card description missing for " + icon);
                        return;
                    }
                    cardName = cardName.toLowerCase();

                    // 🌟 MR P INSTANT WIN
                    if (cardName.equals("cardp.jpg")) {
                        String winner = model.isPlayer1Turn()
                                ? view.player1Title.getText()
                                : view.player2Title.getText();

                        JOptionPane.showMessageDialog(view,
                                "yay " + winner + " drew the Mr. P card and WINS instantly!");

                        int choice = JOptionPane.showConfirmDialog(
                                view,
                                "Would you like to start a new game?",
                                "Play Again?",
                                JOptionPane.YES_NO_OPTION
                        );

                        if (choice == JOptionPane.YES_OPTION) {
                            // reset scores
                            player1Score = 0;
                            player2Score = 0;
                            view.player1Score.setText("Score: 0");
                            view.player2Score.setText("Score: 0");

                            // reset deck
                            usedCards.clear();
                            rebuildDeck();
                            model.setRemainingCards(deck.size());

                            // update label
                            view.drawCardLabel1.setText("Draw Card (" + model.getRemainingCards() + ")");

                            // reassign random cards
                            assignUniqueRandomCards(view.leftPanel);
                            assignUniqueRandomCards(view.rightPanel);

                            // reset board
                            view.boardLabel.setIcon(view.loadScaledImage(model.IMAGE_PATH + "board.jpg", 900, 600));
                            model.setCurrentPosition("0");

                            // start new game cycle
                            startGame();
                        } else {
                            System.exit(0);
                        }

                        return;
                    }



                    usedCards.add(cardName);

                    String newCard = drawUniqueCard();
                    ImageIcon original = new ImageIcon(model.IMAGE_PATH + newCard);
                    Image scaledImg = original.getImage()
                            .getScaledInstance(100, 150, Image.SCALE_SMOOTH);
                    ImageIcon scaledIcon = new ImageIcon(scaledImg);
                    scaledIcon.setDescription(newCard);
                    card.setIcon(scaledIcon);

                    if (model.getRemainingCards() > 0) {
                        model.setRemainingCards(model.getRemainingCards() - 1);
                        view.drawCardLabel1.setText("Draw Card (" + model.getRemainingCards() + ")");
                    }
                    if (model.getRemainingCards() == 0) {
                        usedCards.clear();
                        rebuildDeck();
                        msg("Deck reshuffled!");
                    }

                    updateBoardForCard(model.getCurrentPosition(), cardName);
                    switchTurn();
                }
            });
        }
    }

    private java.util.List<JLabel> getCardLabels(Container panel) {
        java.util.List<JLabel> cards = new ArrayList<>();
        for (Component comp : getAllComponents(panel)) {
            if (comp instanceof JLabel lbl && lbl.getIcon() instanceof ImageIcon img) {
                if (img.getIconWidth() >= 95 && img.getIconWidth() <= 110) cards.add(lbl);
            }
        }
        return cards;
    }

    private Component[] getAllComponents(Container c) {
        java.util.List<Component> list = new ArrayList<>();
        for (Component comp : c.getComponents()) {
            list.add(comp);
            if (comp instanceof Container)
                Collections.addAll(list, getAllComponents((Container) comp));
        }
        return list.toArray(new Component[0]);
    }

    private void toggleGame() {
        if (!model.isGameStarted()) {
            model.setGameStarted(true);
            model.setPaused(false);
            view.startPauseButton.setIcon(view.pauseIcon);
            startGame();
        } else if (!model.isPaused()) {
            model.setPaused(true);
            view.startPauseButton.setIcon(view.startIcon);
            if (turnTimer != null) turnTimer.stop();
            msg("Game paused!");
        } else {
            model.setPaused(false);
            view.startPauseButton.setIcon(view.pauseIcon);
            startTimer();
        }
    }

    private void startGame() {
    	String repInput = JOptionPane.showInputDialog(
                view,
                "Enter the number of repetitions for each card type:",
                "2"
        );

        int rep = 2;
        try { rep = Integer.parseInt(repInput); } catch (Exception ignored) {}
        model.setCardRepetition(rep);

        model.setRemainingCards(rep * 9);
        view.drawCardLabel1.setText("Draw Card (" + model.getRemainingCards() + ")");

        rebuildDeck();
        usedCards.clear();
        assignUniqueRandomCards(view.leftPanel);
        assignUniqueRandomCards(view.rightPanel);
    
        int inputVal = 0;
        while (inputVal <= 0) {
            String input = JOptionPane.showInputDialog(
                    view,
                    "Enter total touchdowns needed to win (e.g., 1, 2, 3).\n" +
                    "Click Cancel or leave blank for default: 3",
                    "3"
            );

            if (input == null || input.trim().isEmpty()) {
                touchdownTarget = 3;
                break;
            }

            try {
                inputVal = Integer.parseInt(input.trim());
                if (inputVal > 0) touchdownTarget = inputVal;
                else msg("Please enter a number greater than 0.");
            } catch (NumberFormatException ex) {
                msg("Invalid input. Enter a number like 1, 2, or 3.");
            }
        }

        boolean startWithP1 = rng.nextBoolean();
        model.setCurrentPosition("0");
        model.setPlayer1Turn(startWithP1);
        view.boardLabel.setIcon(view.loadScaledImage(model.IMAGE_PATH + "board.jpg", 900, 600));
        updateTurnBorders();
        startTimer();

        msg("Game started! First to " + touchdownTarget + " touchdowns wins.\nRoll the dice!");
        countdownAndStart();

        // ensure deck is fresh for game start
        rebuildDeck();
    }

    private void countdownAndStart() {
        model.setPaused(true);
        if (turnTimer != null) turnTimer.stop();

        JDialog countdownDialog = new JDialog(view, "Get Ready!", false);
        countdownDialog.setSize(300, 200);
        countdownDialog.setLocationRelativeTo(view);
        countdownDialog.setUndecorated(true);
        countdownDialog.getContentPane().setBackground(Color.WHITE);

        JLabel countLabel = new JLabel("3", SwingConstants.CENTER);
        countLabel.setFont(new Font("Arial", Font.BOLD, 72));
        countLabel.setForeground(Color.BLUE);
        countdownDialog.add(countLabel, BorderLayout.CENTER);

        countdownDialog.setVisible(true);

        final int[] count = {3};
        javax.swing.Timer countdownTimer = new javax.swing.Timer(1000, e -> {
            count[0]--;
            if (count[0] > 0) {
                countLabel.setText(String.valueOf(count[0]));
            } else {
                ((javax.swing.Timer) e.getSource()).stop();
                countdownDialog.dispose();

                model.setPaused(false);
                view.startPauseButton.setIcon(view.pauseIcon);
                startTimer();
                msg("Go! The game has started!");
            }
        });

        countdownTimer.start();
    }


    private void startTimer() {
        if (turnTimer != null) turnTimer.stop();
        model.setTimeLeft(12);
        view.timeLabel.setText("Time left: " + model.getTimeLeft() + "s");

        turnTimer = new javax.swing.Timer(1000, e -> {
            if (!model.isPaused()) {
                model.setTimeLeft(model.getTimeLeft() - 1);
                view.timeLabel.setText("Time left: " + model.getTimeLeft() + "s");
                if (model.getTimeLeft() <= 0) switchTurn();
            }
        });
        turnTimer.start();
    }

    private void switchTurn() {
        model.setPlayer1Turn(!model.isPlayer1Turn());
        updateTurnBorders();
        startTimer();
    }

    private void updateTurnBorders() {
        view.leftPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(model.isPlayer1Turn() ? Color.RED : Color.BLACK, 3), "Player 1"));
        view.rightPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(!model.isPlayer1Turn() ? Color.RED : Color.BLACK, 3), "Player 2"));
        view.currentPlayerLabel.setText("Current Player: " +
                (model.isPlayer1Turn() ? view.player1Title.getText() : view.player2Title.getText()));
    }

    private void rollDice() {
        if (!model.isGameStarted() || model.isPaused()) {
            msg("Start the game first!");
            return;
        }

        int dice = rng.nextInt(2);
        view.diceLabel.setText(String.valueOf(dice));

        if (dice == 0) {
            view.boardLabel.setIcon(view.loadScaledImage(model.IMAGE_PATH + "qtboard0.jpg", 900, 600));
            model.setCurrentPosition("0");
        } else {
            view.boardLabel.setIcon(view.loadScaledImage(model.IMAGE_PATH + "qtboard1.jpg", 900, 600));
            model.setCurrentPosition("1");
        }
    }

    private void updateBoardForCard(String currentPos, String selectedCard) {
        // YOUR ORIGINAL LOGIC, unchanged
        if (selectedCard == null) {
            msg(" No card detected.");
            return;
        }

        String pos = (currentPos == null) ? "0" : currentPos.trim().toUpperCase();
        String card = selectedCard.trim().toLowerCase();
        String nextBoard = null;

        switch (pos) {
            case "0" -> {
                if (card.equals("h.jpg")) nextBoard = "qtboardP.jpg";
                else if (card.equals("b.jpg")) nextBoard = "qtboardJ.jpg";
                else if (card.equals("x.jpg") || card.equals("y.jpg")) nextBoard = "qtboard1.jpg";
            }
            case "J" -> {
                if (card.equals("b.jpg")) nextBoard = "qtboard1.jpg";
                else if (card.equals("x.jpg") || card.equals("z.jpg") || card.equals("h.jpg")) nextBoard = "qtboardI.jpg";
                if (card.equals("s.jpg")) nextBoard = "qtboardP.jpg";
            }
            case "1" -> {
                if (card.equals("h.jpg")) nextBoard = "qtboardM.jpg";
                else if (card.equals("b.jpg")) nextBoard = "qtboardI.jpg";
                else if (card.equals("x.jpg") || card.equals("y.jpg")) nextBoard = "qtboard0.jpg";
            }
            case "I" -> {
                if (card.equals("x.jpg") || card.equals("z.jpg") || card.equals("h.jpg")) nextBoard = "qtboardJ.jpg";
                else if (card.equals("s.jpg")) nextBoard = "qtboardM.jpg";
            }
            default -> msg(" Unknown position: " + pos);
        }

        if (nextBoard != null) {
            view.boardLabel.setIcon(view.loadScaledImage(model.IMAGE_PATH + nextBoard, 900, 600));
            String newPos = nextBoard.replace("qtboard", "").replace(".jpg", "").toUpperCase();
            model.setCurrentPosition(newPos);

            if (newPos.equals("P") || newPos.equals("M")) {
                if (model.isPlayer1Turn()) {
                    player1Score++;
                    view.player1Score.setText("Score: " + player1Score);
                    msg("yay " + view.player1Title.getText() + " SCORED A TOUCHDOWN!");
                } else {
                    player2Score++;
                    view.player2Score.setText("Score: " + player2Score);
                    msg("yay " + view.player2Title.getText() + " SCORED A TOUCHDOWN!");
                }

                if (player1Score >= touchdownTarget || player2Score >= touchdownTarget) {
                    String winner = player1Score >= touchdownTarget
                            ? view.player1Title.getText()
                            : view.player2Title.getText();

                    JLabel victoryLabel = new JLabel("yay " + winner.toUpperCase() + " WINS THE GAME! ", SwingConstants.CENTER);
                    victoryLabel.setFont(new Font("Ariel", Font.BOLD, 36));
                    victoryLabel.setForeground(Color.RED);

                    JOptionPane.showMessageDialog(
                            view,
                            victoryLabel,
                            "VICTORY!",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    int choice = JOptionPane.showConfirmDialog(
                            view,
                            "Would you like to start a new game?",
                            "Play Again?",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (choice == JOptionPane.YES_OPTION) {

                        player1Score = 0;
                        player2Score = 0;
                        view.player1Score.setText("Score: 0");
                        view.player2Score.setText("Score: 0");
                        usedCards.clear();

                        rebuildDeck();
                        model.setRemainingCards(deck.size());

                        view.drawCardLabel1.setText("Draw Card (" + model.getRemainingCards() + ")");

                        assignUniqueRandomCards(view.leftPanel);
                        assignUniqueRandomCards(view.rightPanel);

                        view.boardLabel.setIcon(view.loadScaledImage(model.IMAGE_PATH + "board.jpg", 900, 600));

                        startGame();

                    } else {
                        System.exit(0);
                    }
                }
            }
        }
    }

    private void msg(String s) {
        JOptionPane.showMessageDialog(view, s);
    }
}
