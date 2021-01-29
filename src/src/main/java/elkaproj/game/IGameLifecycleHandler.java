package elkaproj.game;

import elkaproj.config.GamePowerup;
import elkaproj.config.ILevel;

import java.util.EnumSet;

/**
 * Implementations of this interface handle events about game lifecycle.
 */
public interface IGameLifecycleHandler {

    /**
     * Triggered whenever a new game begins.
     *
     * @param currentLevel The level currently being played.
     * @param currentLives The number of lives the player has.
     */
    default void onGameStarted(ILevel currentLevel, int currentLives) {
    }

    /**
     * Triggered whenever a game is finished.
     *
     * @param totalScore Player's final total score.
     * @param completed  Whether the game was completed.
     */
    default void onGameStopped(int totalScore, boolean completed) {
    }

    /**
     * Triggered whenever a new level is started.
     *
     * @param previousLevel      Previously-played level.
     * @param previousLevelScore Score achieved during previous level.
     * @param currentLevel       The level currently being played.
     * @param totalScore         Player's current total score.
     */
    default void onNextLevel(ILevel previousLevel, int previousLevelScore, ILevel currentLevel, int totalScore) {
    }

    /**
     * Triggered whenever the number of lives the player has changes.
     *
     * @param currentLives Player's current life count.
     * @param maxLives     Player's maximum life count.
     */
    default void onLivesUpdated(int currentLives, int maxLives) {
    }

    /**
     * Triggered whenever score is updated.
     *
     * @param currentScore Player's current level score.
     * @param totalScore   Player's total score.
     */
    default void onScoreUpdated(int currentScore, int totalScore) {
    }

    /**
     * Triggered whenever powerups are updated.
     *
     * @param activePowerups Player's currently-active powerups.
     */
    default void onPowerupsUpdated(EnumSet<GamePowerup> activePowerups) {
    }

    /**
     * Triggered whenever the game is paused.
     */
    default void onGamePaused() {
    }

    /**
     * Triggered whenever the game is unpaused.
     */
    default void onGameResumed() {
    }
}
