package elkaproj.game;

import elkaproj.Dimensions;
import elkaproj.config.ILevel;
import elkaproj.config.LevelTile;

import java.util.Set;

public interface IGameEventHandler {

    /**
     * Triggered whenever the board is updated.
     *
     * @param currentLevel   Level on which the game occurs.
     * @param board          Layout of the board.
     * @param crates         Crate locations.
     * @param playerPosition Player's current position.
     * @param deltas         Crate deltas.
     */
    void onBoardUpdated(ILevel currentLevel, LevelTile[][] board, boolean[][] crates, Dimensions playerPosition, Set<Dimensions.Delta> deltas);
}
