package elkaproj.game;

import elkaproj.config.*;

import java.util.ArrayList;

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

    private LevelTile[][] board = null;
    private boolean[][] crates = null;
    private PlayerPosition playerPosition = null;

    /**
     * Initializes the controller.
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

        Dimensions levelSize = this.currentLevel.getSize();
        this.board = new LevelTile[levelSize.getHeight()][];
        this.crates = new boolean[levelSize.getHeight()][];
        LevelTile[][] levelTiles = this.currentLevel.getTiles();

        for (int y = 0; y < levelSize.getHeight(); y++) {
            this.board[y] = new LevelTile[levelSize.getWidth()];
            this.crates[y] = new boolean[levelSize.getWidth()];

            for (int x = 0; x < levelSize.getWidth(); x++) {
                this.board[y][x] = levelTiles[y][x];
                this.crates[y][x] = this.board[y][x] == LevelTile.CRATE;

                if (this.board[y][x] == LevelTile.PLAYER) {
                    this.playerPosition = new PlayerPosition(x, y);
                    this.board[y][x] = LevelTile.FLOOR;
                } else if (this.board[y][x] == LevelTile.CRATE) {
                    this.board[y][x] = LevelTile.FLOOR;
                }
            }
        }

        this.onScoreUpdated(this.currentScore, this.totalScore);
        this.onBoardUpdated(this.currentLevel, this.board, this.crates, this.playerPosition);

        return true;
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
     */
    public void stopGame() {
        if (this.currentLevelNumber < 0)
            return;

        this.totalScore += this.currentScore;
        this.onGameStopped(this.totalScore);

        this.currentLives = 0;
        this.currentStreak = 0;
        this.currentLevelNumber = -1;
        this.currentLevel = null;
        this.currentScore = 0;
        this.totalScore = 0;
    }

    // event dispatchers
    private void onGameStarted(ILevel currentLevel, int currentLives) {
        for (IGameLifecycleHandler handler : this.lifecycleHandlers) {
            handler.onGameStarted(currentLevel, currentLives);
        }
    }

    private void onGameStopped(int totalScore) {
        for (IGameLifecycleHandler handler : this.lifecycleHandlers) {
            handler.onGameStopped(this.totalScore);
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

    private void onBoardUpdated(ILevel currentLevel, LevelTile[][] board, boolean[][] crates, PlayerPosition playerPosition) {
        for (IGameEventHandler handler : this.gameEventHandlers) {
            handler.onBoardUpdated(currentLevel, board, crates, playerPosition);
        }
    }
}
