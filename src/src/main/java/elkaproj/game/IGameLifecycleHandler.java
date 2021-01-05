package elkaproj.game;

import elkaproj.config.ILevel;

/**
 * Implementations of this interface handle events about game lifecycle.
 */
public interface IGameLifecycleHandler {

    /**
     * Triggered whenever a new game begins.
     * @param currentLevel The level currently being played.
     * @param currentLives The number of lives the player has.
     */
    void onGameStarted(ILevel currentLevel, int currentLives);

    /**
     * Triggered whenever a game is finished.
     * @param totalScore Player's final total score.
     */
    void onGameStopped(int totalScore);

    /**
     * Triggered whenever a new level is started.
     * @param currentLevel The level currently being played.
     * @param totalScore Player's current total score.
     */
    void onNextLevel(ILevel currentLevel, int totalScore);

    /**
     * Triggered whenever the number of lives the player has changes.
     * @param currentLives Player's current life count.
     * @param maxLives Player's maximum life count.
     */
    void onLivesUpdated(int currentLives, int maxLives);

    /**
     * Triggered whenever score is updated.
     * @param currentScore Player's current level score.
     * @param totalScore Player's total score.
     */
    void onScoreUpdated(int currentScore, int totalScore);
}
