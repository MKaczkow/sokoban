package elkaproj.ui;

import elkaproj.config.Dimensions;
import elkaproj.config.ILevel;
import elkaproj.config.LevelTile;
import elkaproj.game.GameController;
import elkaproj.game.IGameEventHandler;
import elkaproj.game.PlayerPosition;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Root canvas, on which the actual game will be drawn.
 */
public class GuiCanvas extends Canvas implements IGameEventHandler {

    private final GameController gameController;

    private Dimensions levelSize = null;

    private LevelTile[][] board = null;
    private boolean[][] crates = null;
    private PlayerPosition playerPosition = null;
    private Dimensions tileSize;
    private Dimensions tileStart;
    private final ReentrantLock boardLock = new ReentrantLock();

    private BufferStrategy bs = null;
    private final java.util.Timer timer;

    private final Image tileFloor, tileWall, tileTarget, tileCrate, tilePlayer;

    public GuiCanvas(GameController gameController) throws IOException {
        this.gameController = gameController;
        this.gameController.addGameEventHandler(this);
        //this.addComponentListener(new ResizeListener(this));

        this.timer = new java.util.Timer();
        this.timer.scheduleAtFixedRate(new BoardTimer(this), 50, 50);

        this.tileFloor = ImageIO.read(this.getClass().getResource("/tiles/floor.png"));
        this.tileWall = ImageIO.read(this.getClass().getResource("/tiles/wall.png"));
        this.tileTarget = ImageIO.read(this.getClass().getResource("/tiles/target.png"));
        this.tileCrate = ImageIO.read(this.getClass().getResource("/tiles/crate.png"));
        this.tilePlayer = ImageIO.read(this.getClass().getResource("/tiles/player.png"));
    }

    @Override
    public void addNotify() {
        super.addNotify();
        this.updateBufferStrategy();
    }

    private void drawBoardLayer(Graphics g, LevelTile[][] board) {
        int w = this.tileStart.getWidth();
        int h = this.tileStart.getHeight();

        for (int y = 0; y < this.levelSize.getHeight(); y++) {
            for (int x = 0; x < this.levelSize.getWidth(); x++) {
                Image image = null;
                switch (this.board[y][x]) {
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

                g.drawImage(image, w + x * this.tileSize.getWidth(), h + y * this.tileSize.getHeight(), this.tileSize.getWidth(), this.tileSize.getHeight(), null);
            }
        }
    }

    private void drawCrateLayer(Graphics g, boolean[][] crates, PlayerPosition playerPosition) {
        int w = this.tileStart.getWidth();
        int h = this.tileStart.getHeight();

        for (int y = 0; y < this.levelSize.getHeight(); y++) {
            for (int x = 0; x < this.levelSize.getWidth(); x++) {
                if (this.crates[y][x]) {
                    g.drawImage(this.tileCrate, w + x * this.tileSize.getWidth(), h + y * this.tileSize.getHeight(), this.tileSize.getWidth(), this.tileSize.getHeight(), null);
                }

                if (this.playerPosition.getX() == x && this.playerPosition.getY() == y) {
                    g.drawImage(this.tilePlayer, w + x * this.tileSize.getWidth(), h + y * this.tileSize.getHeight(), this.tileSize.getWidth(), this.tileSize.getHeight(), null);
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

            this.tileSize = this.computeTileSize();
            this.tileStart = this.computeTileStart(this.tileSize);
        } finally {
            this.boardLock.unlock();
        }
    }

    private Dimensions computeTileSize() {
        Dimension windowSize = this.getSize();

        int w = (windowSize.width - 16) / this.levelSize.getWidth();
        int h = (windowSize.height - 16) / this.levelSize.getHeight();

        int dim = Math.min(w, h);
        dim = dim - (dim % 16); // round to tile size

        return new Dimensions(dim, dim);
    }

    private Dimensions computeTileStart(Dimensions tileSize) {
        Dimension windowSize = this.getSize();

        int w = (windowSize.width - 16 - tileSize.getWidth() * this.levelSize.getWidth()) / 2 + 8;
        int h = (windowSize.height - 16 - tileSize.getHeight() * this.levelSize.getHeight()) / 2 + 8;

        return new Dimensions(w, h);
    }

    private void updateBufferStrategy() {
        if (this.getBufferStrategy() == null)
            this.createBufferStrategy(3);

        this.bs = this.getBufferStrategy();
    }

    private static class ResizeListener extends ComponentAdapter {

        private final GuiCanvas guiCanvas;

        public ResizeListener(GuiCanvas canvas) {
            this.guiCanvas = canvas;
        }

        @Override
        public void componentResized(ComponentEvent componentEvent) {
            super.componentResized(componentEvent);

            this.guiCanvas.onBoardUpdated(this.guiCanvas.gameController.getCurrentLevel(), this.guiCanvas.board, this.guiCanvas.crates, this.guiCanvas.playerPosition);
        }
    }

    private static class BoardTimer extends TimerTask {

        private final GuiCanvas guiCanvas;

        public BoardTimer(GuiCanvas guiCanvas) {
            this.guiCanvas = guiCanvas;
        }

        @Override
        public void run() {
            if (this.guiCanvas.bs == null)
                return;

            try {
                this.guiCanvas.boardLock.lock();

                do {
                    do {
                        Graphics g = this.guiCanvas.bs.getDrawGraphics();

                        this.guiCanvas.tileSize = this.guiCanvas.computeTileSize();
                        this.guiCanvas.tileStart = this.guiCanvas.computeTileStart(this.guiCanvas.tileSize);

                        Dimension size = this.guiCanvas.getSize();
                        g.clearRect(0, 0, size.width, size.height);
                        g.setColor(Color.BLACK);
                        g.fillRect(0, 0, size.width, size.height);

                        this.guiCanvas.drawBoardLayer(g, this.guiCanvas.board);
                        this.guiCanvas.drawCrateLayer(g, this.guiCanvas.crates, this.guiCanvas.playerPosition);

                        g.dispose();
                    } while (this.guiCanvas.bs.contentsRestored());

                    this.guiCanvas.bs.show();
                } while (this.guiCanvas.bs.contentsLost());
            } finally {
                this.guiCanvas.boardLock.unlock();
            }
        }
    }
}
