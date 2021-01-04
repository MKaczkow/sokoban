package elkaproj.ui;

import java.awt.*;
import java.awt.image.BufferStrategy;

public class ProzektCanvas extends Canvas {

    // ATTN: when drawing use this
    private BufferStrategy bs = null;

    @Override
    public void addNotify() {
        super.addNotify();
        this.createBufferStrategy(3);
        this.bs = this.getBufferStrategy();
    }


}
