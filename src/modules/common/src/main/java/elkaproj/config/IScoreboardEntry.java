package elkaproj.config;

/**
 * Represents a singular scoreboard entry.
 */
public interface IScoreboardEntry {

    /**
     * Gets the name of the player this entry is for.
     *
     * @return Name of the player this entry is for.
     */
    String getPlayerName();

    /**
     * Gets the level this entry is for.
     *
     * @return Level this entry is for.
     */
    ILevel getLevel();

    /**
     * Gets the score achieved by the player.
     *
     * @return Score achieved by the player.
     */
    int getScore();
}
