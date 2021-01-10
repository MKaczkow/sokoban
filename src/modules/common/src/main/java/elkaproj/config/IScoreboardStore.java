package elkaproj.config;

/**
 * Manages loading and storing scoreboards and entries.
 */
public interface IScoreboardStore {

    /**
     * Loads the complete scoreboard.
     * @return Loaded scoreboard.
     */
    IScoreboard loadScoreboard();

    /**
     * Stores a scoreboard entry for a single level.
     * @param scoreboard Scoreboard to store the entry in.
     * @param level Level to store the entry for.
     * @param playerName Name of the player to store the score for.
     * @param score Score achieved by the player.
     */
    void putEntry(IScoreboard scoreboard, ILevel level, String playerName, int score);
}
