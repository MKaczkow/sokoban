package elkaproj.ui;

import elkaproj.config.Dimensions;
import elkaproj.config.ILevel;
import elkaproj.config.LevelTile;
import elkaproj.game.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Root canvas, on which the actual game will be drawn.
 */
public class GuiCanvas extends Canvas implements IGameEventHandler, IGameLifecycleHandler, KeyListener {

    private final GameController gameController;

    private Dimensions levelSize = null;

    private boolean isRunning = false;
    private LevelTile[][] board = null;
    private boolean[][] crates = null;
    private PlayerPosition playerPosition = null;
    private final ReentrantLock boardLock = new ReentrantLock();

    private BufferStrategy bs = null;
    private final java.util.Timer timer;

    private long lastInputLockout = 0;

    private final Image tileFloor, tileWall, tileTarget, tileCrate, tilePlayer;

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
        this.timer.scheduleAtFixedRate(new BoardTimer(this), 15, 15); // ~66.6FPS

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
        this.updateBufferStrategy();
    }

    private void drawBoardLayer(Graphics g, LevelTile[][] board, Dimensions tileStart, int tileSize) {
        int w = tileStart.getWidth();
        int h = tileStart.getHeight();

        for (int y = 0; y < this.levelSize.getHeight(); y++) {
            for (int x = 0; x < this.levelSize.getWidth(); x++) {
                Image image = null;
                switch (board[y][x]) {
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
        }
    }

    private void drawCrateLayer(Graphics g, boolean[][] crates, PlayerPosition playerPosition, Dimensions tileStart, int tileSize) {
        int w = tileStart.getWidth();
        int h = tileStart.getHeight();

        for (int y = 0; y < this.levelSize.getHeight(); y++) {
            for (int x = 0; x < this.levelSize.getWidth(); x++) {
                if (crates[y][x]) {
                    g.drawImage(this.tileCrate, w + x * tileSize, h + y * tileSize, tileSize, tileSize, null);
                }

                if (playerPosition.getX() == x && playerPosition.getY() == y) {
                    g.drawImage(this.tilePlayer, w + x * tileSize, h + y * tileSize, tileSize, tileSize, null);
                }
            }
        }
    }

    @Override
    public void onBoardUpdated(ILevel currentLevel, LevelTile[][] board, boolean[][] crates, PlayerPosition playerPosition) {
        try {
            this.board = board;
            this.crates = crates;
            this.playerPosition = playerPosition;

            this.boardLock.lock();

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

    private Dimensions computeTileStart(int tileSize) {
        Dimension windowSize = this.getSize();

        int w = (windowSize.width - 16 - tileSize * this.levelSize.getWidth()) / 2 + 8;
        int h = (windowSize.height - 16 - tileSize * this.levelSize.getHeight()) / 2 + 8;

        return new Dimensions(w, h);
    }

    private void updateBufferStrategy() {
        this.createBufferStrategy(3);
        this.bs = this.getBufferStrategy();
    }

    @Override
    public void onGameStarted(ILevel currentLevel, int currentLives) {
    }

    @Override
    public void onGameStopped(int totalScore, boolean completed) {
        this.isRunning = false;
        this.bs = null;
    }

    @Override
    public void onNextLevel(ILevel currentLevel, int totalScore) {
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

            if (this.guiCanvas.bs == null)
                return;

            BufferStrategy bs = this.guiCanvas.bs;
            try {
                this.guiCanvas.boardLock.lock();

                if (System.currentTimeMillis() + 450 >= this.guiCanvas.lastInputLockout)
                    this.guiCanvas.gameController.enableInput(true);

                do {
                    do {
                        Graphics g = bs.getDrawGraphics();

                        int tileSize = this.guiCanvas.computeTileSize();
                        Dimensions tileStart = this.guiCanvas.computeTileStart(tileSize);

                        Dimension size = this.guiCanvas.getSize();
                        g.clearRect(0, 0, size.width, size.height);
                        g.setColor(Color.BLACK);
                        g.fillRect(0, 0, size.width, size.height);

                        this.guiCanvas.drawBoardLayer(g, this.guiCanvas.board, tileStart, tileSize);
                        this.guiCanvas.drawCrateLayer(g, this.guiCanvas.crates, this.guiCanvas.playerPosition, tileStart, tileSize);

                        g.dispose();
                    } while (bs.contentsRestored());

                    bs.show();
                } while (bs.contentsLost());
            } finally {
                this.guiCanvas.boardLock.unlock();
            }
        }
    }
}
