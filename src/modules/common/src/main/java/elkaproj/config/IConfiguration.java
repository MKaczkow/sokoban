package elkaproj.config;

import java.util.EnumSet;

/**
 * Provides configuration data for the game. A configuration object provides all of the following:
 * <ul>
 *     <li>Level set - provides information about the levels the player will play</li>
 *     <li>
 *         Gameplay settings
 *         <ul>
 *             <li>Maximum lives - up to how many lives a player can have</li>
 *             <li>Start lives - how many lives the player starts with</li>
 *             <li>
 *                 Active bonuses - A comma-separated list of STRENGTH, PULL, GHOST - decides which bonuses are active
 *                 in-game
 *              </li>
 *             <li>
 *                 Life recovery level count - How many levels in a row must a player complete without a reset to
 *                 regain a life
 *             </li>
 *             <li>
 *                 Life recovery count - How many lives a player regains for completing n levels in a row without a
 *                 reset
 *             </li>
 *             <li>Timers active - Whether level timers are active</li>
 *         </ul>
 *     </li>
 * </ul>
 */
public interface IConfiguration extends IXmlSerializable {
    /**
     * Gets the ID of the currently-configured level pack the player will be playing.
     *
     * @return ID of the level pack.
     * @see ILevelPack
     * @see ILevel
     */
    String getLevelPackId();

    /**
     * Gets the maximum number of lives a player can have. This number has to be greater than 0.
     *
     * @return Maximum number of lives a player can have.
     */
    int getMaxLives();

    /**
     * Gets the number of lives the player starts with. This number has to be greater than 0 and less than or equal to
     * the number returned by {@link #getMaxLives()}.
     *
     * @return Number of lives the player starts with.
     */
    int getStartingLives();

    /**
     * Gets the number of levels that the player has to successfully complete in a row without resetting to trigger
     * life regain. This number has to be greater than 1.
     *
     * @return The number of levels the player has to successfully complete in a row to trigger life regain.
     */
    int getLifeRecoveryThreshold();

    /**
     * <p>
     * Gets the number of lives the player regains after successfully completing several levels in a row (the exact
     * number is determined by {@link #getLifeRecoveryThreshold()}). This number has to be greater than 0 and less
     * than or equal to the number returned by {@link #getMaxLives()}.
     * </p>
     *
     * <p>
     * If the number of lives the player currently has plus the number of lives they recover is greater than
     * maximum number of lives, the number is clamped to the maximum number.
     * </p>
     *
     * @return The number of lives recovered after successfully completing several levels in a row.
     */
    int getLifeRecoveryCount();

    /**
     * Gets whether level timers are active. Level timers define 3 time thresholds exceeding or completing under which
     * can reward or penalize the player.
     *
     * @return Whether level timers are active.
     * @see ILevel
     */
    boolean areTimersActive();

    /**
     * Gets the powerups that are active in-game.
     *
     * @return Set of powerup items that are enabled to appear in-game.
     * @see GamePowerup
     */
    EnumSet<GamePowerup> getActivePowerups();
}
