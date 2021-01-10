package elkaproj.config;

import elkaproj.Dimensions;

/**
 * Provides information about a singular game level. This is an abstraction for level objects meant to establish a
 * common base.
 */
public interface ILevel {

    /**
     * Gets which level in the collection is this.
     *
     * @return Level's ordinal in the collection.
     */
    int getOrdinal();

    /**
     * Gets the name of the level.
     *
     * @return Name of the level.
     */
    String getName();

    /**
     * Gets the number of seconds which constitutes a bonus timer for the level. Completing the level below this time
     * will result in a score bonus being applied.
     *
     * @return Number of seconds the player has to complete the level for scoring bonus to be applied.
     */
    int getBonusTimeThreshold();

    /**
     * Gets the number of seconds which constitutes a penalty timer for the level. Completing the level above that
     * time will result in a score penalty being applied.
     *
     * @return Number of seconds the player has to complete the level before scoring penalty is applied.
     */
    int getPenaltyTimeThreshold();

    /**
     * Gets the number of seconds which constitutes a fail timer for the level. Reaching this time will automatically
     * fail the level.
     *
     * @return Number of seconds the player has to complete the level before they are automatically failed.
     */
    int getFailTimeThreshold();

    /**
     * Gets the size of the level, in tiles.
     *
     * @return Size of the level in tiles.
     */
    Dimensions getSize();

    /**
     * Gets the tiles comprising the level.
     *
     * @return Tiles comprising the level.
     */
    LevelTile[][] getTiles();
}
