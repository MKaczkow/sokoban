package elkaproj.game;

import elkaproj.config.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Implements game logic.
 */
public class GameController {

    private final IConfiguration configuration;
    private final ILevelPack levelPack;

    private final ArrayList<IGameLifecycleHandler> lifecycleHandlers = new ArrayList<>();
    private final ArrayList<IGameEventHandler> gameEventHandlers = new ArrayList<>();

    private int currentLives = 0;
    private int currentStreak = 0;
    private int currentLevelNumber = -1;
    private ILevel currentLevel = null;
    private int currentScore = 0;
    private int totalScore = 0;

    private boolean gamePaused = false;
    private LevelTile[][] board = null;
    private boolean[][] crates = null;
    private int numCrates = 0, numMatched = 0;
    private Dimensions playerPosition = null;
    private boolean acceptsInput = true;

    /**
     * Initializes the controller.
     * @param configuration Configuration to use for this game.
     * @param levelPack Level pack the player will play through.
     */
    public GameController(IConfiguration configuration, ILevelPack levelPack) {
        this.configuration = configuration;
        this.levelPack = levelPack;
    }

    /**
     * Gets the configuration for this controller.
     * @return Configuration for the controller.
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * Gets the number of remaining lives.
     * @return Number of remaining lives.
     */
    public int getCurrentLives() {
        return this.currentLives;
    }

    /**
     * Gets the max number of lives the player can have.
     * @return Maximum number of lives a player can have.
     */
    public int getMaxLives() {
        return this.configuration.getMaxLives();
    }

    /**
     * Gets the current level score.
     * @return Current level score.
     */
    public int getCurrentScore() {
        return this.currentScore;
    }

    /**
     * Gets the current level.
     * @return Current level.
     */
    public ILevel getCurrentLevel() {
        return this.currentLevel;
    }

    /**
     * Gets the total score across all levels.
     * @return Total score across all level.
     */
    public int getTotalScore() {
        return this.totalScore;
    }

    /**
     * Gets whether a game is active.
     * @return Whether a game is active.
     */
    public boolean isGameRunning() {
        return this.currentLevelNumber >= 0;
    }

    /**
     * Adds a game lifecycle event handler.
     * @param lifecycleHandler Game lifecycle event handler.
     */
    public void addLifecycleHandler(IGameLifecycleHandler lifecycleHandler) {
        this.lifecycleHandlers.add(lifecycleHandler);
    }

    /**
     * Removes a game lifecycle event handler.
     * @param lifecycleHandler Game lifecycle event handler.
     */
    public void removeLifecycleHandler(IGameLifecycleHandler lifecycleHandler) {
        this.lifecycleHandlers.remove(lifecycleHandler);
    }

    /**
     * Adds a game event handler.
     * @param gameEventHandler Game event handler.
     */

    public void addGameEventHandler(IGameEventHandler gameEventHandler) {
        this.gameEventHandlers.add(gameEventHandler);
    }

    /**
     * Removes a game event handler.
     * @param gameEventHandler Game event handler.
     */
    public void removeGameEventHandler(IGameEventHandler gameEventHandler) {
        this.gameEventHandlers.remove(gameEventHandler);
    }

    /**
     * Moves to the next level.
     * @return Whether a new level was loaded. If false, it means there are no more levels available.
     */
    public boolean nextLevel() {
        boolean success = this.nextLevelInternal();
        if (success)
            this.onNextLevel(this.currentLevel, this.totalScore);

        return success;
    }

    private boolean nextLevelInternal() {
        if (++this.currentLevelNumber >= this.levelPack.getCount())
            return false;

        this.currentLevel = this.levelPack.getLevel(this.currentLevelNumber);
        this.totalScore += this.currentScore;
        this.currentScore = 0;
        this.acceptsInput = true;

        this.prepareLevel();

        this.onScoreUpdated(this.currentScore, this.totalScore);
        this.onBoardUpdated(this.currentLevel, this.board, this.crates, this.playerPosition, null);

        return true;
    }

    private void prepareLevel() {
        this.numCrates = 0;
        this.numMatched = 0;
        this.acceptsInput = true;

        Dimensions levelSize = this.currentLevel.getSize();
        this.board = new LevelTile[levelSize.getHeight()][];
        this.crates = new boolean[levelSize.getHeight()][];
        LevelTile[][] levelTiles = this.currentLevel.getTiles();

        for (int y = 0; y < levelSize.getHeight(); y++) {
            this.board[y] = new LevelTile[levelSize.getWidth()];
            this.crates[y] = new boolean[levelSize.getWidth()];

            for (int x = 0; x < levelSize.getWidth(); x++) {
                this.board[y][x] = levelTiles[y][x];

                if (this.board[y][x] == LevelTile.CRATE) {
                    this.crates[y][x] = true;
                    this.numCrates++;
                }

                if (this.board[y][x] == LevelTile.PLAYER) {
                    this.playerPosition = new Dimensions(x, y);
                    this.board[y][x] = LevelTile.FLOOR;
                } else if (this.board[y][x] == LevelTile.CRATE) {
                    this.board[y][x] = LevelTile.FLOOR;
                }
            }
        }
    }

    /**
     * Resets and starts the game.
     */
    public void startGame() {
        if (!this.nextLevelInternal())
            throw new IllegalStateException("Failed to start the game, are any levels defined?");

        this.currentLives = this.configuration.getStartingLives();
        this.currentStreak = 0;
        this.currentScore = 0;
        this.totalScore = 0;

        this.onGameStarted(this.currentLevel, this.currentLives);
        this.onLivesUpdated(this.currentLives, this.configuration.getMaxLives());
    }

    /**
     * Stops the game, if applicable.
     * @param completed Whether the stop happened as a result of game being completed. False indicates the game was interrupted manually.
     */
    public void stopGame(boolean completed) {
        if (this.currentLevelNumber < 0)
            return;

        this.totalScore += this.currentScore;
        this.onGameStopped(this.totalScore, completed);

        this.currentLives = 0;
        this.currentStreak = 0;
        this.currentLevelNumber = -1;
        this.currentLevel = null;
        this.currentScore = 0;
        this.totalScore = 0;
    }

    /**
     * Pauses or resumes game.
     */
    public void setGamePaused() {
        if (gamePaused = false) {
            this.onGamePaused();
            gamePaused = true;
        }
        else {
            this.onGameResumed();
            gamePaused = false;
        }
    }

    /**
     * Resets level.
     */
    public void resetLevel() {
        this.prepareLevel();
        if (currentLives >= 1) { currentLives--; }
        else { stopGame(false); }

        // fire lives and board events

    }

    /**
     * Sets whether player inputs are accepted.
     * @param enable Whether inputs are to be accepted.
     */
    public void enableInput(boolean enable) {
        this.acceptsInput = enable;
    }

    /**
     * Move the player in a direction.
     * @param direction Direction to move the player in.
     */
    public void move(GameMovementDirection direction) {
        if (this.currentLevelNumber < 0 || !this.acceptsInput)
            return;

        Dimensions pos = this.playerPosition;
        int xm = 0, ym = 0;
        switch (direction) {
            case RIGHT:
                xm = 1;
                break;

            case LEFT:
                xm = -1;
                break;

            case UP:
                ym = -1;
                break;

            case DOWN:
                ym = 1;
                break;
        }

        Dimensions newPos = new Dimensions(playerPosition.getWidth() + xm, playerPosition.getHeight() + ym);
        int nx = newPos.getWidth(), ny = newPos.getHeight();

        // check if out of bounds
        if (newPos.getWidth() < 0 || newPos.getWidth() > this.currentLevel.getSize().getWidth() || newPos.getHeight() < 0 || newPos.getHeight() > this.currentLevel.getSize().getHeight())
            return;

        // check if wall
        if (this.board[ny][nx] == LevelTile.WALL)
            return;
        //TODO: implement GHOST power-up logic here

        // check if crate
        HashSet<Dimensions.Delta> deltas = null;
        if (this.crates[ny][nx]) {
            // check if stacked crate or stacked wall
            if (this.crates[ny + ym][nx + xm] || this.board[ny + ym][nx + xm] == LevelTile.WALL)
                return;
            //TODO: implement STRENGTH and PULL  power-ups logic here
            this.crates[ny + ym][nx + xm] = true;
            this.crates[ny][nx] = false;

            deltas = new HashSet<>(1);
            deltas.add(new Dimensions.Delta(new Dimensions(nx, ny), new Dimensions(nx + xm, ny + ym)));

            if (this.board[ny + ym][nx + xm] == LevelTile.TARGET_SPOT && this.board[ny][nx] != LevelTile.TARGET_SPOT)
                this.numMatched++;
            else if (this.board[ny + ym][nx + xm] != LevelTile.TARGET_SPOT && this.board[ny][nx] == LevelTile.TARGET_SPOT)
                this.numMatched--;
        }

        this.playerPosition = newPos;
        this.currentScore++;

        this.onScoreUpdated(this.currentScore, this.totalScore);
        this.onBoardUpdated(this.currentLevel, this.board, this.crates, this.playerPosition, deltas);

        if (this.numMatched == this.numCrates) {
            if (!this.nextLevel())
                this.stopGame(true);
        }
    }

    // event dispatchers
    private void onGameStarted(ILevel currentLevel, int currentLives) {
        for (IGameLifecycleHandler handler : this.lifecycleHandlers) {
            handler.onGameStarted(currentLevel, currentLives);
        }
    }

    private void onGameStopped(int totalScore, boolean completed) {
        for (IGameLifecycleHandler handler : this.lifecycleHandlers) {
            handler.onGameStopped(this.totalScore, completed);
        }
    }

    private void onNextLevel(ILevel currentLevel, int totalScore) {
        for (IGameLifecycleHandler handler : this.lifecycleHandlers) {
            handler.onNextLevel(currentLevel, totalScore);
        }
    }

    private void onLivesUpdated(int currentLives, int maxLives) {
        for (IGameLifecycleHandler handler : this.lifecycleHandlers) {
            handler.onLivesUpdated(currentLives, maxLives);
        }
    }

    private void onScoreUpdated(int currentScore, int totalScore) {
        for (IGameLifecycleHandler handler : this.lifecycleHandlers) {
            handler.onScoreUpdated(currentScore, totalScore);
        }
    }

    private void onGamePaused() {
        for (IGameLifecycleHandler handler : this.lifecycleHandlers) {
            handler.onGamePaused();
        }
    }

    private void onGameResumed() {
        for (IGameLifecycleHandler handler : this.lifecycleHandlers) {
            handler.onGameResumed();
        }
    }

    private void onBoardUpdated(ILevel currentLevel, LevelTile[][] board, boolean[][] crates, Dimensions playerPosition, Set<Dimensions.Delta> deltas) {
        for (IGameEventHandler handler : this.gameEventHandlers) {
            handler.onBoardUpdated(currentLevel, board, crates, playerPosition, deltas);
        }
    }
}
