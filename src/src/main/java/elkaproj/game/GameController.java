package elkaproj.game;

import elkaproj.DebugWriter;
import elkaproj.Dimensions;
import elkaproj.config.*;

import java.util.ArrayList;
import java.util.EnumSet;
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
    private EnumSet<GamePowerup> powerUps;
    private GameClock gameClock;

    private boolean gamePaused = false;
    private LevelTile[][] board = null;
    private LevelTile[][] powerupTiles = null;
    private boolean[][] crates = null;
    private int numCrates = 0, numMatched = 0;
    private Dimensions playerPosition = null;
    private boolean acceptsInput = true;

    /**
     * Initializes the controller.
     *
     * @param configuration Configuration to use for this game.
     * @param levelPack     Level pack the player will play through.
     */
    public GameController(IConfiguration configuration, ILevelPack levelPack) {
        this.configuration = configuration;
        this.levelPack = levelPack;
    }

    /**
     * Gets the configuration for this controller.
     *
     * @return Configuration for the controller.
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * Gets the number of remaining lives.
     *
     * @return Number of remaining lives.
     */
    public int getCurrentLives() {
        return this.currentLives;
    }

    /**
     * Gets active power-ups (out of the list of 3).
     *
     * @return Active power-ups.
     */
    public EnumSet<GamePowerup> getPowerUps() {
        return this.powerUps;
    }

    /**
     * Gets the max number of lives the player can have.
     *
     * @return Maximum number of lives a player can have.
     */
    public int getMaxLives() {
        return this.configuration.getMaxLives();
    }

    /**
     * Gets the current level score.
     *
     * @return Current level score.
     */
    public int getCurrentScore() {
        return this.currentScore;
    }

    /**
     * Gets the current level.
     *
     * @return Current level.
     */
    public ILevel getCurrentLevel() {
        return this.currentLevel;
    }

    /**
     * Gets the total score across all levels.
     *
     * @return Total score across all level.
     */
    public int getTotalScore() {
        return this.totalScore;
    }

    /**
     * Gets the current active powerups.
     *
     * @return Active powerups.
     */
    public EnumSet<GamePowerup> getActivePowerups() {
        return this.powerUps.clone();
    }

    /**
     * Gets whether a game is active.
     *
     * @return Whether a game is active.
     */
    public boolean isGameRunning() {
        return this.currentLevelNumber >= 0;
    }

    /**
     * Gets whether the game is paused.
     *
     * @return Whether the game is paused.
     */
    public boolean isPaused() {
        return this.gamePaused;
    }

    /**
     * Adds a game lifecycle event handler.
     *
     * @param lifecycleHandler Game lifecycle event handler.
     */
    public void addLifecycleHandler(IGameLifecycleHandler lifecycleHandler) {
        this.lifecycleHandlers.add(lifecycleHandler);
    }

    /**
     * Removes a game lifecycle event handler.
     *
     * @param lifecycleHandler Game lifecycle event handler.
     */
    public void removeLifecycleHandler(IGameLifecycleHandler lifecycleHandler) {
        this.lifecycleHandlers.remove(lifecycleHandler);
    }

    /**
     * Adds a game event handler.
     *
     * @param gameEventHandler Game event handler.
     */

    public void addGameEventHandler(IGameEventHandler gameEventHandler) {
        this.gameEventHandlers.add(gameEventHandler);
    }

    /**
     * Removes a game event handler.
     *
     * @param gameEventHandler Game event handler.
     */
    public void removeGameEventHandler(IGameEventHandler gameEventHandler) {
        this.gameEventHandlers.remove(gameEventHandler);
    }

    /**
     * Moves to the next level.
     *
     * @return Whether a new level was loaded. If false, it means there are no more levels available.
     */
    public boolean nextLevel() {
        ILevel previousLevel = this.currentLevel;
        int previousScore = this.currentScore;

        boolean success = this.nextLevelInternal();
        if (success)
            this.onNextLevel(previousLevel, previousScore, this.currentLevel, this.totalScore);

        if (++this.currentStreak >= this.configuration.getLifeRecoveryThreshold()) {
            this.currentLives += this.configuration.getLifeRecoveryCount();
            this.currentLives = Math.min(this.currentLives, this.configuration.getMaxLives());
            this.currentStreak = 0;
            this.powerUps.clear();
            this.onLivesUpdated(this.currentLives, this.configuration.getMaxLives());
            this.onPowerupsUpdated(this.getActivePowerups());
        }

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
        this.onBoardUpdated(this.currentLevel, this.board, this.powerupTiles, this.crates, this.playerPosition, null);

        return true;
    }

    private void prepareLevel() {
        this.numCrates = 0;
        this.numMatched = 0;
        this.acceptsInput = true;
        this.powerUps = EnumSet.noneOf(GamePowerup.class);

        Dimensions levelSize = this.currentLevel.getSize();
        this.board = new LevelTile[levelSize.getHeight()][];
        this.powerupTiles = new LevelTile[levelSize.getHeight()][];
        this.crates = new boolean[levelSize.getHeight()][];
        LevelTile[][] levelTiles = this.currentLevel.getTiles();
        EnumSet<GamePowerup> enabledPowerups = this.configuration.getActivePowerups();

        for (int y = 0; y < levelSize.getHeight(); y++) {
            this.board[y] = new LevelTile[levelSize.getWidth()];
            this.powerupTiles[y] = new LevelTile[levelSize.getWidth()];
            this.crates[y] = new boolean[levelSize.getWidth()];

            for (int x = 0; x < levelSize.getWidth(); x++) {
                this.board[y][x] = levelTiles[y][x];

                if (this.board[y][x] == LevelTile.CRATE) {
                    this.crates[y][x] = true;
                    this.numCrates++;
                }

                switch (this.board[y][x]) {
                    case GHOST:
                    case STRENGTH:
                    case PULL:
                        if (enabledPowerups.contains(GamePowerup.fromTile(this.board[y][x])))
                            this.powerupTiles[y][x] = levelTiles[y][x];
                        else
                            this.powerupTiles[y][x] = LevelTile.NONE;

                        this.board[y][x] = LevelTile.FLOOR;
                        break;

                    default:
                        this.powerupTiles[y][x] = LevelTile.NONE;
                        break;
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
        this.powerUps = EnumSet.noneOf(GamePowerup.class);
        this.onGameStarted(this.currentLevel, this.currentLives);
        this.onLivesUpdated(this.currentLives, this.configuration.getMaxLives());
    }

    /**
     * Stops the game, if applicable.
     *
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
    public void togglePause() {
        this.gamePaused = !this.gamePaused;
        this.enableInput(!this.gamePaused);

        DebugWriter.INSTANCE.logMessage("GAME", "Pause status: %b", this.gamePaused);
        if (this.gamePaused) {
            this.onGamePaused();
        } else {
            this.onGameResumed();
        }
    }

    /**
     * Resets level.
     */
    public void resetLevel() {
        this.currentStreak = 0;
        this.prepareLevel();

        this.powerUps.clear();
        this.onPowerupsUpdated(this.getActivePowerups());

        if (currentLives > 0) {
            this.currentLives--;
            this.onLivesUpdated(this.getCurrentLives(), this.getMaxLives());

            this.onBoardUpdated(this.currentLevel, this.board, this.powerupTiles, this.crates, this.playerPosition, null);
        } else {
            this.stopGame(false);
        }
    }

    /**
     * Sets whether player inputs are accepted.
     *
     * @param enable Whether inputs are to be accepted.
     */
    public void enableInput(boolean enable) {
        this.acceptsInput = enable;
    }

    /**
     * Move the player in a direction.
     *
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

        int ox = pos.getWidth(), oy = pos.getHeight();
        Dimensions newPos = new Dimensions(ox + xm, oy + ym);
        int nx = newPos.getWidth(), ny = newPos.getHeight();

        // check if out of bounds
        if (newPos.getWidth() < 0 || newPos.getWidth() > this.currentLevel.getSize().getWidth() || newPos.getHeight() < 0 || newPos.getHeight() > this.currentLevel.getSize().getHeight())
            return;

        // check if wall
        if (this.board[ny][nx] == LevelTile.WALL && !(this.powerUps.contains(GamePowerup.GHOST)))
            return;
        this.powerUps.remove(GamePowerup.GHOST);

        // check if crate
        HashSet<Dimensions.Delta> deltas = null;
        if (this.crates[ny][nx]) {
            // check if stacked wall
            if (this.board[ny + ym][nx + xm] == LevelTile.WALL)
                return;

            // check if stacked crate
            if (this.crates[ny + ym][nx + xm] && !(this.powerUps.contains(GamePowerup.STRENGTH)))
                return;

            // check if STRENGTH needed
            if (this.crates[ny + ym][nx + xm]) {
                this.crates[ny + 2 * ym][nx + 2 * xm] = true;
                this.powerUps.remove(GamePowerup.STRENGTH);
            }

            // push crate
            this.crates[ny][nx] = false;
            this.crates[ny + ym][nx + xm] = true;

            deltas = new HashSet<>(1);
            deltas.add(new Dimensions.Delta(new Dimensions(nx, ny), new Dimensions(nx + xm, ny + ym)));

            if (this.board[ny + ym][nx + xm] == LevelTile.TARGET_SPOT && this.board[ny][nx] != LevelTile.TARGET_SPOT)
                this.numMatched++;
            else if (this.board[ny + ym][nx + xm] != LevelTile.TARGET_SPOT && this.board[ny][nx] == LevelTile.TARGET_SPOT)
                this.numMatched--;
        }

        // check if PULL is used
        if (this.powerUps.contains(GamePowerup.PULL)) {
            // check if crate behind
            if (this.crates[oy - ym][ox - xm]) {
                this.powerUps.remove(GamePowerup.PULL);
                // pull crate
                this.crates[oy - ym][ox - xm] = false;
                this.crates[oy][ox] = true;

                deltas = deltas == null ? new HashSet<>(1) : deltas;
                deltas.add(new Dimensions.Delta(new Dimensions(ox - xm, oy - ym), new Dimensions(ox, oy)));

                if (this.board[oy][ox] == LevelTile.TARGET_SPOT && this.board[oy - ym][ox - xm] != LevelTile.TARGET_SPOT)
                    this.numMatched++;
                else if (this.board[oy][ox] != LevelTile.TARGET_SPOT && this.board[oy - ym][ox - xm] == LevelTile.TARGET_SPOT)
                    this.numMatched--;
            }
        }
        this.playerPosition = newPos;
        this.currentScore++;

        // check if newPos is power-up activator
        switch (this.powerupTiles[ny][nx]) {
            case GHOST:
            case STRENGTH:
            case PULL:
                this.powerUps.add(GamePowerup.fromTile(this.powerupTiles[ny][nx]));
                this.powerupTiles[ny][nx] = LevelTile.NONE;
                this.onPowerupsUpdated(this.getActivePowerups());
                break;
        }

        this.onScoreUpdated(this.currentScore, this.totalScore);
        this.onBoardUpdated(this.currentLevel, this.board, this.powerupTiles, this.crates, this.playerPosition, deltas);

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

    private void onNextLevel(ILevel previousLevel, int previousLevelScore, ILevel currentLevel, int totalScore) {
        for (IGameLifecycleHandler handler : this.lifecycleHandlers) {
            handler.onNextLevel(previousLevel, previousLevelScore, currentLevel, totalScore);
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

    private void onPowerupsUpdated(EnumSet<GamePowerup> activePowerups) {
        for (IGameLifecycleHandler handler : this.lifecycleHandlers) {
            handler.onPowerupsUpdated(activePowerups);
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

    private void onBoardUpdated(ILevel currentLevel, LevelTile[][] board, LevelTile[][] powerupTiles, boolean[][] crates, Dimensions playerPosition, Set<Dimensions.Delta> deltas) {
        for (IGameEventHandler handler : this.gameEventHandlers) {
            handler.onBoardUpdated(currentLevel, board, powerupTiles, crates, playerPosition, deltas);
        }
    }
}
