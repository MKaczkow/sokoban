package elkaproj.config;

import java.util.List;

/**
 * Represents a total game scoreboard entry.
 */
public interface IScoreboardTotalEntry {

    /**
     * Gets the name of the player this entry is for.
     *
     * @return Name of the player this entry is for.
     */
    String getPlayerName();

    /**
     * Gets the player's total score.
     *
     * @return Player's total score.
     */
    int getScore();

    /**
     * Gets whether the player has completed all levels.
     *
     * @return Whether the player has completed all levels.
     */
    boolean hasCompletedAllLevels();

    /**
     * Gets entries for all levels this player has completed.
     *
     * @return Entries for all levels this player has completed.
     */
    List<IScoreboardEntry> getLevelEntries();
}
