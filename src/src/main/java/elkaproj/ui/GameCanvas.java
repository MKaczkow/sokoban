package elkaproj.ui;

import java.awt.*;
import java.awt.image.BufferStrategy;

/**
 * Root canvas, on which the actual game will be drawn.
 */
public class GameCanvas extends Canvas {

    // ATTN: when drawing use this
    private BufferStrategy bs = null;

    @Override
    public void addNotify() {
        super.addNotify();
        this.createBufferStrategy(3);
        this.bs = this.getBufferStrategy();
    }


}
