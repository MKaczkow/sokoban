package elkaproj.ui;

import elkaproj.config.language.Language;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main window class of the game. Holds all other components.
 */
public class GameFrame extends JFrame implements ActionListener {

    public static final String COMMAND_EXIT = "PROZEkt_exit";
    public static final String COMMAND_PAUSE_RESUME = "PROZEkt_pause_resume";
    public static final String COMMAND_RESET = "PROZEkt_reset";
    public static final String COMMAND_SCOREBOARD = "PROZEkt_highscores";
    public static final String COMMAND_AUTHORS = "PROZEkt_authors";

    private final Language language;

    /**
     * Creates an initializes a new game window.
     * @param language UI language to use.
     */
    public GameFrame(Language language) {
        super("@window.title");

        // set language
        this.language = language;

        // set the listener so we can close the application
        this.addWindowListener(new GameFrameWindowAdapter());

        // set size and location
        this.setSize(640, 400);
        this.setMinimumSize(new Dimension(640, 400));
        this.setLocationRelativeTo(null); // center on screen

        // add UI components
        this.setJMenuBar(new GameMenuBar(this));
        this.add(new JLabel("@menu.file.items.exit", JLabel.CENTER));

        this.localize(new Component[] { this });
    }

    private void localize(Component[] components) {
        for (Component component : components) {
            if (component instanceof Container) {
                this.localize(((Container) component).getComponents());
            }

            if (component instanceof JMenu) {
                int count = ((JMenu) component).getItemCount();
                Component[] items = new Component[count];
                for (int i = 0; i < count; i++) {
                    items[i] = ((JMenu) component).getItem(i);
                }
                this.localize(items);
            }

            if (component instanceof Frame) {
                String title = ((Frame) component).getTitle();
                if (title.startsWith("@"))
                    ((Frame) component).setTitle(this.language.getValue(title.substring(1)));
            }

            if (component instanceof AbstractButton) {
                String text = ((AbstractButton) component).getText();
                if (text.startsWith("@"))
                    ((AbstractButton) component).setText(this.language.getValue(text.substring(1)));
            }

            if (component instanceof JLabel) {
                String text = ((JLabel) component).getText();
                if (text.startsWith("@"))
                    ((JLabel) component).setText(this.language.getValue(text.substring(1)));
            }

            if (component instanceof JTextComponent) {
                String text = ((JTextComponent) component).getText();
                if (text.startsWith("@"))
                    ((JTextComponent) component).setText(this.language.getValue(text.substring(1)));
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        JOptionPane.showMessageDialog(this, actionEvent.getActionCommand());

        switch (actionEvent.getActionCommand()) {
            case COMMAND_EXIT:
                this.dispose();
                break;
        }
    }

    private static class GameFrameWindowAdapter extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent windowEvent) {
            System.exit(0);
        }
    }
}
