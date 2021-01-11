package elkaproj.config;

import java.io.IOException;

/**
 * Manages loading and storing scoreboards and entries.
 */
public interface IScoreboardStore {

    /**
     * Loads the complete scoreboard.
     *
     * @param levelPack Level pack to load scoreboard for.
     * @return Loaded scoreboard.
     * @throws IOException Loading scoreboard failed.
     */
    IScoreboard loadScoreboard(ILevelPack levelPack) throws IOException;

    /**
     * Stores a scoreboard entry for a single level.
     *
     * @param scoreboard Scoreboard to store the entry in.
     * @param level      Level to store the entry for.
     * @param playerName Name of the player to store the score for.
     * @param score      Score achieved by the player.
     * @throws IOException Loading scoreboard failed.
     */
    void putEntry(IScoreboard scoreboard, ILevel level, String playerName, int score) throws IOException;
}
