package elkaproj.ui;

import elkaproj.DebugWriter;
import elkaproj.config.Dimensions;
import elkaproj.config.ILevel;
import elkaproj.config.LevelTile;
import elkaproj.game.GameController;
import elkaproj.game.GameMovementDirection;
import elkaproj.game.IGameEventHandler;
import elkaproj.game.IGameLifecycleHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Root canvas, on which the actual game will be drawn.
 */
public class GuiCanvas extends Canvas implements IGameEventHandler, IGameLifecycleHandler, KeyListener {

    private static final int ANIMATION_FRAME_COUNT = 10;
    private static final int ANIMATION_FRAME_TIME = 50;
    private static final int ANIMATION_DURATION = ANIMATION_FRAME_COUNT * ANIMATION_FRAME_TIME;

    private final GameController gameController;

    private Dimensions levelSize = null;

    private boolean isRunning = false;
    private boolean boardInvalid = true;
    private LevelTile[][] board = null;
    private boolean[][] crates = null;
    private Dimensions playerPosition = null;
    private Dimensions.Delta playerDelta = null;
    private Set<Dimensions.Delta> crateDeltas = null;
    private final ReentrantLock boardLock = new ReentrantLock();

    private final java.util.Timer timer;

    private long lastInputLockout = 0;

    private final Image tileFloor, tileWall, tileTarget, tileCrate, tilePlayer;

    private BufferStrategy bs;

    /**
     * Initializes the game canvas.
     * @param gameController Controller, which handles the gameplay component itself.
     * @throws IOException Loading tile graphics failed.
     */
    public GuiCanvas(GameController gameController) throws IOException {
        this.gameController = gameController;
        this.gameController.addGameEventHandler(this);
        this.addKeyListener(this);
        this.setFocusable(true);

        this.timer = new java.util.Timer();
        this.timer.scheduleAtFixedRate(new BoardTimer(this), ANIMATION_DURATION, ANIMATION_DURATION); // ~66.6FPS

        this.tileFloor = ImageIO.read(this.getClass().getResource("/tiles/floor.png"));
        this.tileWall = ImageIO.read(this.getClass().getResource("/tiles/wall.png"));
        this.tileTarget = ImageIO.read(this.getClass().getResource("/tiles/target.png"));
        this.tileCrate = ImageIO.read(this.getClass().getResource("/tiles/crate.png"));
        this.tilePlayer = ImageIO.read(this.getClass().getResource("/tiles/player.png"));
    }

    /**
     * Stops timers and performs necessary shutdown operations.
     */
    public void performShutdown() {
        this.timer.cancel();
        this.timer.purge();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        this.createBufferStrategy(3);
        this.bs = this.getBufferStrategy();
    }

    private void redrawGame(Graphics2D g) {
        try {
            this.boardLock.lock();

            long currentTime = System.currentTimeMillis();
            if (currentTime + ANIMATION_DURATION >= this.lastInputLockout)
                this.gameController.enableInput(true);

            long n1 = System.nanoTime();

            int tileSize = this.computeTileSize();
            Dimensions tileStart = this.computeTileStart(tileSize);

            float animationOffsetPercent = this.computeAnimationOffset(currentTime);
            int animationOffset = (int)(animationOffsetPercent * tileSize);

            Dimension size = this.getSize();

            if (this.boardInvalid) {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, size.width, size.height);

                this.drawBoardLayer(g, this.board, tileStart, tileSize, animationOffset);
                this.drawCrateLayer(g,
                        this.crates,
                        this.playerDelta != null ? this.playerDelta : new Dimensions.Delta(this.playerPosition, this.playerPosition),
                        tileStart,
                        tileSize,
                        animationOffset);
            } else {
                this.drawDeltas(g,
                        this.board,
                        this.playerDelta != null ? this.playerDelta : new Dimensions.Delta(this.playerPosition, this.playerPosition),
                        this.crateDeltas,
                        tileStart,
                        tileSize,
                        animationOffset);
            }

            long n2 = System.nanoTime();
            DebugWriter.INSTANCE.logMessage("TIMER", "%d", n2 - n1);
        } finally {
            this.boardLock.unlock();
        }
    }

    private void drawBoardLayer(Graphics2D g, LevelTile[][] board, Dimensions tileStart, int tileSize, int animationOffset) {
        int w = tileStart.getWidth();
        int h = tileStart.getHeight();

        for (int y = 0; y < this.levelSize.getHeight(); y++) {
            for (int x = 0; x < this.levelSize.getWidth(); x++) {
                this.drawBoardTileAt(g, x, y, w, h, board[y][x], tileSize);
            }
        }
    }

    private void drawCrateLayer(Graphics2D g, boolean[][] crates, Dimensions.Delta playerDelta, Dimensions tileStart, int tileSize, int animationOffset) {
        int w = tileStart.getWidth();
        int h = tileStart.getHeight();

        for (int y = 0; y < this.levelSize.getHeight(); y++) {
            for (int x = 0; x < this.levelSize.getWidth(); x++) {

                if (crates[y][x])
                    g.drawImage(this.tileCrate, w + x * tileSize, h + y * tileSize, tileSize, tileSize, null);

                if (playerDelta.getTo().getWidth() == x && playerDelta.getTo().getHeight() == y)
                    g.drawImage(this.tilePlayer, w + x * tileSize, h + y * tileSize, tileSize, tileSize, null);
            }
        }
    }

    private void drawDeltas(Graphics2D g, LevelTile[][] board, Dimensions.Delta playerDelta, Set<Dimensions.Delta> crateDeltas, Dimensions tileStart, int tileSize, int animationOffset) {
        int w = tileStart.getWidth();
        int h = tileStart.getHeight();

        Dimensions pFrom = playerDelta.getFrom(),
                pTo = playerDelta.getTo();

        if (animationOffset < ANIMATION_DURATION) {
            int x1 = pFrom.getWidth(), y1 = pFrom.getHeight();
            int x2 = pTo.getWidth(), y2 = pTo.getHeight();
            this.drawTilesUnderMovingObject(g, x1, y1, x2, y2, w, h, this.tilePlayer, board, playerDelta, tileSize, animationOffset);
        } else {
            int x = pFrom.getWidth(), y = pFrom.getHeight();
            this.drawBoardTileAt(g, x, y, w, h, board[y][x], tileSize);

            g.drawImage(this.tilePlayer, w + pTo.getWidth() * tileSize, h + pTo.getHeight() * tileSize, tileSize, tileSize, null);
        }
    }

    private void drawTilesUnderMovingObject(Graphics2D g, int x1, int y1, int x2, int y2, int w, int h, Image tileImage, LevelTile[][] board, Dimensions.Delta delta, int tileSize, int animationOffset) {
        this.drawBoardTileAt(g, x1, y1, w, h, board[y1][x1], tileSize);
        this.drawBoardTileAt(g, x2, y2, w, h, board[y2][x2], tileSize);

        g.drawImage(tileImage, (w + x1 * tileSize) + animationOffset * delta.getXChange(), (h + y1 * tileSize) + animationOffset * delta.getYChange(), tileSize, tileSize, null);
    }

    private void drawBoardTileAt(Graphics2D g, int x, int y, int w, int h, LevelTile tile, int tileSize) {
        Image image = null;
        switch (tile) {
            case WALL:
                image = this.tileWall;
                break;

            case FLOOR:
                image = this.tileFloor;
                break;

            case TARGET_SPOT:
                image = this.tileTarget;
                break;
        }

        g.drawImage(image, w + x * tileSize, h + y * tileSize, tileSize, tileSize, null);
    }

    @Override
    public void onBoardUpdated(ILevel currentLevel, LevelTile[][] board, boolean[][] crates, Dimensions playerPosition, Set<Dimensions.Delta> deltas) {
        try {
            this.boardLock.lock();

            this.board = board;
            this.crates = crates;
            if (this.playerPosition != null)
                this.playerDelta = new Dimensions.Delta(this.playerPosition, playerPosition);
            this.playerPosition = playerPosition;
            this.crateDeltas = deltas;

            this.levelSize = currentLevel.getSize();

            this.isRunning = true;

            this.lastInputLockout = System.currentTimeMillis();
            this.gameController.enableInput(false);
        } finally {
            this.boardLock.unlock();
        }
    }

    private int computeTileSize() {
        Dimension windowSize = this.getSize();

        int w = (windowSize.width - 16) / this.levelSize.getWidth();
        int h = (windowSize.height - 16) / this.levelSize.getHeight();

        int dim = Math.min(w, h);
        dim = dim - (dim % 16); // round to tile size

        return dim;
    }

    private float computeAnimationOffset(long currentTime) {
        long tDelta = currentTime - this.lastInputLockout;
        if (tDelta >= ANIMATION_DURATION)
            return 1.0f;

        return tDelta / (float)ANIMATION_DURATION;
    }

    private Dimensions computeTileStart(int tileSize) {
        Dimension windowSize = this.getSize();

        int w = (windowSize.width - 16 - tileSize * this.levelSize.getWidth()) / 2 + 8;
        int h = (windowSize.height - 16 - tileSize * this.levelSize.getHeight()) / 2 + 8;

        return new Dimensions(w, h);
    }

    @Override
    public void onGameStarted(ILevel currentLevel, int currentLives) {
        this.boardInvalid = true;
    }

    @Override
    public void onGameStopped(int totalScore, boolean completed) {
        this.isRunning = false;
        this.boardInvalid = true;
    }

    @Override
    public void onNextLevel(ILevel currentLevel, int totalScore) {
        this.boardInvalid = true;
    }

    @Override
    public void onLivesUpdated(int currentLives, int maxLives) {
    }

    @Override
    public void onScoreUpdated(int currentScore, int totalScore) {
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                this.gameController.move(GameMovementDirection.LEFT);
                break;

            case KeyEvent.VK_RIGHT:
                this.gameController.move(GameMovementDirection.RIGHT);
                break;

            case KeyEvent.VK_UP:
                this.gameController.move(GameMovementDirection.UP);
                break;

            case KeyEvent.VK_DOWN:
                this.gameController.move(GameMovementDirection.DOWN);
                break;
        }
    }

    private static class BoardTimer extends TimerTask {

        private final GuiCanvas guiCanvas;

        public BoardTimer(GuiCanvas guiCanvas) {
            this.guiCanvas = guiCanvas;
        }

        @Override
        public void run() {
            if (!this.guiCanvas.gameController.isGameRunning() || !this.guiCanvas.isRunning)
                return;

            BufferStrategy bs = this.guiCanvas.bs;
            if (bs == null)
                return;

            do {
                Graphics2D g = (Graphics2D) bs.getDrawGraphics();
                this.guiCanvas.redrawGame(g);
                g.dispose();
                bs.show();
            } while (bs.contentsLost() || bs.contentsRestored());

            this.guiCanvas.boardInvalid = false;
        }
    }
}
