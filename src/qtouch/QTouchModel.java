package qtouch;

/**
 * Model class – holds all constants, variables, and game states.
 * Defines configuration and data shared across the app.
 */
public class QTouchModel {
    // ----------- IMAGE PATH -----------
    public static final String IMAGE_PATH = "C:\\CST8132 Homework\\PA\\img\\";

    // ----------- GAME STATE VARIABLES -----------
    private int remainingCards = 24;
    private boolean isPlayer1Turn = false;
    private boolean isGameStarted = false;
    private boolean isPaused = false;
    private int timeLeft = 12;
    private String currentPosition = "0";
    private int cardRepetition = 2;
    private final int totalCardTypes = 9; // B,Z,S,Y,M,X,H,I,cardP


    // ----------- GETTERS AND SETTERS -----------
    public int getRemainingCards() { return remainingCards; }
    public void setRemainingCards(int remainingCards) { this.remainingCards = remainingCards; }

    public boolean isPlayer1Turn() { return isPlayer1Turn; }
    public void setPlayer1Turn(boolean player1Turn) { isPlayer1Turn = player1Turn; }

    public boolean isGameStarted() { return isGameStarted; }
    public void setGameStarted(boolean gameStarted) { isGameStarted = gameStarted; }

    public boolean isPaused() { return isPaused; }
    public void setPaused(boolean paused) { isPaused = paused; }
    public void switchTurn() {
        isPlayer1Turn = !isPlayer1Turn;
    }
    public String getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(String currentPosition) {
        this.currentPosition = currentPosition;
    }
    public void setCardRepetition(int r) {
        if (r <= 0) r = 2; // default
        cardRepetition = r;
    }

    public int getCardRepetition() {
        return cardRepetition;
    }



    public int getTimeLeft() { return timeLeft; }
    public void setTimeLeft(int timeLeft) { this.timeLeft = timeLeft; }
}
