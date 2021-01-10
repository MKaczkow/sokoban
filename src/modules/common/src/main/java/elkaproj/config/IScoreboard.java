package elkaproj.config;

import java.util.List;

/**
 * Represents a scoreboard.
 */
public interface IScoreboard {

    /**
     * Gets the level pack this scoreboard is for.
     * @return The level pack associated with this scoreboard.
     */
    ILevelPack getLevelPack();

    /**
     * Gets the full list of all total entries in this scoreboard.
     * @return Full list of all total entries in this scoreboard.
     */
    List<IScoreboardTotalEntry> getAllTotalEntries();

    /**
     * Gets the full list of all entries for a given level.
     * @param level Level to get entries for.
     * @return Full list of all entries for a given level.
     */
    List<IScoreboardEntry> getLevelEntries(ILevel level);
}
