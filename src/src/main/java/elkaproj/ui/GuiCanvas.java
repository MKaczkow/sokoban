package elkaproj.ui;

import elkaproj.DebugWriter;
import elkaproj.Entry;
import elkaproj.config.Dimensions;
import elkaproj.config.ILevel;
import elkaproj.config.LevelTile;
import elkaproj.game.GameController;
import elkaproj.game.GameMovementDirection;
import elkaproj.game.IGameEventHandler;
import elkaproj.game.IGameLifecycleHandler;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Root canvas, on which the actual game will be drawn.
 */
public class GuiCanvas extends Canvas implements IGameEventHandler, IGameLifecycleHandler, KeyListener {

    private final float animationFrameDelay;
    private final int animationDuration = 150;

    private final GameController gameController;

    private Dimensions levelSize = null;

    private boolean isRunning = false;
    private LevelTile[][] board = null;
    private boolean[][] crates = null;
    private Dimensions playerPosition = null;
    private Dimensions.Delta playerDelta = null;
    private Set<Dimensions.Delta> crateDeltas = null;
    private final ReentrantLock boardLock = new ReentrantLock();

    private final BoardTimer boardTimer;
    private final Thread animationThread;

    private long lastInputLockout = 0;

    private final Image tileFloor, tileWall, tileTarget, tileCrate, tilePlayer;

    private BufferStrategy bs;
    private final String pauseString;

    /**
     * Initializes the game canvas.
     * @param gameController Controller, which handles the gameplay component itself.
     * @throws IOException Loading tile graphics failed.
     */
    public GuiCanvas(GameController gameController, String pauseString) throws IOException {
        this.pauseString = pauseString;
        this.gameController = gameController;
        this.gameController.addGameEventHandler(this);
        this.addKeyListener(this);
        this.setFocusable(true);

        int animationFps = this.computeOptimalFps();
        this.animationFrameDelay = 1000f / animationFps;
        DebugWriter.INSTANCE.logMessage("CANVAS", "Animating at %d FPS", animationFps);

        this.boardTimer = new BoardTimer(this);
        this.animationThread = new Thread(this.boardTimer);
        this.animationThread.start();

        this.tileFloor = ImageIO.read(this.getClass().getResource("/tiles/floor.png"));
        this.tileWall = ImageIO.read(this.getClass().getResource("/tiles/wall.png"));
        this.tileTarget = ImageIO.read(this.getClass().getResource("/tiles/target.png"));
        this.tileCrate = ImageIO.read(this.getClass().getResource("/tiles/crate.png"));
        this.tilePlayer = ImageIO.read(this.getClass().getResource("/tiles/player.png"));
    }

    private int computeOptimalFps() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        int ref = 60;
        for (GraphicsDevice g : gs) {
            DisplayMode dm = g.getDisplayMode();
            int r = dm.getRefreshRate();
            ref = Math.max(r, ref);
        }

        return ref;
    }

    /**
     * Stops timers and performs necessary shutdown operations.
     */
    public void performShutdown() {
        this.boardTimer.cancel();
        try {
            this.animationThread.join();
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Updates buffering strategy to account for items going in and out of view. Should be called whenever this component is made visible.
     */
    public void updateBufferStrategy() {
        SwingUtilities.invokeLater(() -> {
            this.createBufferStrategy(3);
            this.bs = this.getBufferStrategy();
        });
    }

    private void redrawGame(Graphics2D g) {
        try {
            this.boardLock.lock();

            long currentTime = System.currentTimeMillis();
            this.gameController.enableInput(!this.gameController.isPaused() && currentTime + this.animationDuration >= this.lastInputLockout);

            int tileSize = this.computeTileSize();
            Dimensions tileStart = this.computeTileStart(tileSize);

            float animationOffsetPercent = this.computeAnimationOffset(currentTime);
            int animationOffset = (int)(animationOffsetPercent * tileSize);

            Dimension size = this.getSize();

            g.setColor(Color.BLACK);
            g.fillRect(0, 0, size.width, size.height);

            this.drawBoardLayer(g, this.board, tileStart, tileSize);
            this.drawCrateLayer(g,
                    this.crates,
                    this.playerDelta != null ? this.playerDelta : new Dimensions.Delta(this.playerPosition, this.playerPosition),
                    this.crateDeltas,
                    tileStart,
                    tileSize,
                    animationOffset);

            if (this.gameController.isPaused()) {
                Font f = Entry.IBMPlexBoldItalic.deriveFont(36f);
                FontMetrics fm = g.getFontMetrics(f);

                int h = fm.getHeight();
                int w = fm.stringWidth(this.pauseString);

                g.setColor(new Color(33, 33, 33));
                g.fillRect(16, 16, w + 24, h + 24);

                g.setColor(Color.WHITE);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
                g.setFont(f);
                g.drawString(this.pauseString, 28, 28 + h - fm.getDescent());
            }
        } finally {
            this.boardLock.unlock();
        }
    }

    private void drawBoardLayer(Graphics2D g, LevelTile[][] board, Dimensions tileStart, int tileSize) {
        int w = tileStart.getWidth();
        int h = tileStart.getHeight();

        for (int y = 0; y < this.levelSize.getHeight(); y++) {
            for (int x = 0; x < this.levelSize.getWidth(); x++) {
                this.drawBoardTileAt(g, x, y, w, h, board[y][x], tileSize);
            }
        }
    }

    private void drawCrateLayer(Graphics2D g, boolean[][] crates, Dimensions.Delta playerDelta, Set<Dimensions.Delta> crateDeltas, Dimensions tileStart, int tileSize, int animationOffset) {
        int w = tileStart.getWidth();
        int h = tileStart.getHeight();

        int x = playerDelta.getTo().getWidth(), y = playerDelta.getTo().getHeight();
        if (animationOffset >= tileSize) {
            g.drawImage(this.tilePlayer, w + x * tileSize, h + y * tileSize, tileSize, tileSize, null);
        } else {
            g.drawImage(this.tilePlayer, (w + playerDelta.getFrom().getWidth() * tileSize) + playerDelta.getXChange() * animationOffset, (h + playerDelta.getFrom().getHeight() * tileSize) + playerDelta.getYChange() * animationOffset, tileSize, tileSize, null);
        }

        Set<Dimensions> forbiddenCrates = crateDeltas != null ? crateDeltas.stream()
                .map(Dimensions.Delta::getTo)
                .collect(Collectors.toSet()) : new HashSet<>();

        for (y = 0; y < this.levelSize.getHeight(); y++) {
            for (x = 0; x < this.levelSize.getWidth(); x++) {
                if (crates[y][x] && (animationOffset >= tileSize || !forbiddenCrates.contains(new Dimensions(x, y))))
                    g.drawImage(this.tileCrate, w + x * tileSize, h + y * tileSize, tileSize, tileSize, null);
            }
        }

        if (animationOffset < tileSize && crateDeltas != null) {
            for (Dimensions.Delta crateDelta : crateDeltas) {
                int cx = crateDelta.getFrom().getWidth(), cy = crateDelta.getFrom().getHeight();
                g.drawImage(this.tileCrate, (w + cx * tileSize) + animationOffset * crateDelta.getXChange(), (h + cy * tileSize) + animationOffset * crateDelta.getYChange(), tileSize, tileSize, null);
            }
        }
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
        if (tDelta >= this.animationDuration)
            return 1.0f;

        return tDelta / (float)this.animationDuration;
    }

    private Dimensions computeTileStart(int tileSize) {
        Dimension windowSize = this.getSize();

        int w = (windowSize.width - 16 - tileSize * this.levelSize.getWidth()) / 2 + 8;
        int h = (windowSize.height - 16 - tileSize * this.levelSize.getHeight()) / 2 + 8;

        return new Dimensions(w, h);
    }

    @Override
    public void onGameStopped(int totalScore, boolean completed) {
        this.isRunning = false;
        this.bs = null;
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
                this.gameController.enableInput(false);
                break;

            case KeyEvent.VK_RIGHT:
                this.gameController.move(GameMovementDirection.RIGHT);
                this.gameController.enableInput(false);
                break;

            case KeyEvent.VK_UP:
                this.gameController.move(GameMovementDirection.UP);
                this.gameController.enableInput(false);
                break;

            case KeyEvent.VK_DOWN:
                this.gameController.move(GameMovementDirection.DOWN);
                this.gameController.enableInput(false);
                break;
        }
    }

    private static class BoardTimer implements Runnable {

        private final GuiCanvas guiCanvas;
        private boolean run = true;

        public BoardTimer(GuiCanvas guiCanvas) {
            this.guiCanvas = guiCanvas;
        }

        @Override
        public void run() {
            DebugWriter.INSTANCE.logMessage("ANIM-THREAD", "Animation thread started");

            while (this.run) {
                try {
                    long nt = System.nanoTime();

                    Thread.yield();

                    if (!this.guiCanvas.gameController.isGameRunning() || !this.guiCanvas.isRunning)
                        continue;

                    BufferStrategy bs = this.guiCanvas.bs;
                    if (bs == null)
                        continue;

                    do {
                        do {
                            Graphics2D g = (Graphics2D) bs.getDrawGraphics();
                            this.guiCanvas.redrawGame(g);
                            g.dispose();
                        } while (bs.contentsRestored());

                        bs.show();
                    } while (bs.contentsLost());

                    nt = System.nanoTime() - nt;

                    int wait = (int) (this.guiCanvas.animationFrameDelay * 1e6 - nt);
                    if (wait > 0) {
                        Thread.sleep(wait / 1000000, wait % 1000000);
                    }
                } catch (IllegalStateException ignored) {
                } catch (Exception ex) {
                    DebugWriter.INSTANCE.logError("ANIM-THREAD", ex, "Animation exception");
                }
            }
        }

        public void cancel() {
            this.run = false;
        }
    }
}
