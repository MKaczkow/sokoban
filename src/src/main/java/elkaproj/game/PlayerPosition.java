package elkaproj.game;

/**
 * Represents the player's position on the board.
 */
public class PlayerPosition {

    private int x, y;

    /**
     * Initializes player's board position.
     * @param x X coordinate of the position.
     * @param y Y coordinate of the position.
     */
    public PlayerPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets player's X position on the board.
     * @return Player's X position on the board.
     */
    public int getX() {
        return this.x;
    }

    /**
     * Sets player's X position on the board.
     * @param x Player's X position on the board.
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Gets player's Y position on the board.
     * @return Player's Y position on the board.
     */
    public int getY() {
        return this.y;
    }

    /**
     * Sets player's Yposition on the board.
     * @param y Player's Y position on the board.
     */
    public void setY(int y) {
        this.y = y;
    }
}
