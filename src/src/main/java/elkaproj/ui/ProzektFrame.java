package elkaproj.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ProzektFrame extends JFrame {
    public ProzektFrame() {
        super("PROZEkt Sokoban");

        // set the listener so we can close the application
        this.addWindowListener(new ProzektFrameWindowAdapter());

        // set size and location
        this.setSize(640, 400);
        this.setMinimumSize(new Dimension(640, 400));
        this.setLocationRelativeTo(null); // center on screen

        // add UI components
        this.add(new JLabel("lmao"));
    }

    private static class ProzektFrameWindowAdapter extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent windowEvent) {
            System.exit(0);
        }
    }
}
