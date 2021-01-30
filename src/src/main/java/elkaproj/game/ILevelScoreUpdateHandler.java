package elkaproj.game;

import elkaproj.config.ILevel;

/**
 * Handles score updates.
 */
public interface ILevelScoreUpdateHandler {

    /**
     * Triggered whenever level score is updated.
     *
     * @param level Level for which the score was updated.
     * @param score Score for the level.
     */
    void onLevelScoreUpdated(ILevel level, int score);
}
