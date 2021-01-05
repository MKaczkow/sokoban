package elkaproj.game;

import elkaproj.config.ILevel;
import elkaproj.config.LevelTile;

public interface IGameEventHandler {

    /**
     * Triggered whenever the board is updated.
     * @param currentLevel Level on which the game occurs.
     * @param board Layout of the board.
     * @param crates Crate locations.
     * @param playerPosition Player's current position.
     */
    void onBoardUpdated(ILevel currentLevel, LevelTile[][] board, boolean[][] crates, PlayerPosition playerPosition);
}
