package elkaproj.ui;

import elkaproj.game.GameController;

import java.awt.*;
import java.awt.image.BufferStrategy;

/**
 * Root canvas, on which the actual game will be drawn.
 */
public class GuiCanvas extends Canvas {

    // ATTN: when drawing use this
    private BufferStrategy bs = null;

    private final GameController gameController;

    public GuiCanvas(GameController gameController) {
        this.gameController = gameController;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        this.createBufferStrategy(3);
        this.bs = this.getBufferStrategy();
    }


}
