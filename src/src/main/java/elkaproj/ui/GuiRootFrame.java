package elkaproj.ui;

import elkaproj.DebugWriter;
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
public class GuiRootFrame extends JFrame implements ActionListener {

    public static final String COMMAND_EXIT = "PROZEkt_exit";
    public static final String COMMAND_PAUSE_RESUME = "PROZEkt_pause_resume";
    public static final String COMMAND_RESET = "PROZEkt_reset";
    public static final String COMMAND_SCOREBOARD = "PROZEkt_highscores";
    public static final String COMMAND_AUTHORS = "PROZEkt_authors";
    public static final String COMMAND_CONFIRM_PLAYERNAME = "PROZEkt_confirm_playername";
    public static final String COMMAND_NEW_GAME = "PROZEkt_new_game";

    private final Language language;

    private final GuiPlayerNameView playerNameView;
    private String playerName = null;

    private final GuiMainMenuView mainMenuView;

    /**
     * Creates an initializes a new game window.
     * @param language UI language to use.
     */
    public GuiRootFrame(Language language) {
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
        this.setJMenuBar(new GuiMenuBar(this));

        this.playerNameView = new GuiPlayerNameView(this);
        this.add(this.playerNameView);

        this.mainMenuView = new GuiMainMenuView(this);
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
    public void paint(Graphics graphics) {
        this.localize(new Component[] { this });
        super.paint(graphics);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        JOptionPane.showMessageDialog(this, actionEvent.getActionCommand());

        switch (actionEvent.getActionCommand()) {
            case COMMAND_EXIT:
                this.dispose();
                break;

            case COMMAND_CONFIRM_PLAYERNAME:
                this.playerName = this.playerNameView.getPlayerName();

                if (this.playerName != null) {
                    this.remove(this.playerNameView);
                    DebugWriter.INSTANCE.logMessage("PLAYER", "New player name (%d): '%s'", this.playerName.length(), this.playerName);

                    this.mainMenuView.setPlayerName(this.playerName);
                    this.add(this.mainMenuView);
                    this.forceUpdate();
                }

                break;
        }
    }

    private void forceUpdate() {
        this.repaint();
        this.revalidate();
    }

    private static class GameFrameWindowAdapter extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent windowEvent) {
            System.exit(0);
        }
    }
}
