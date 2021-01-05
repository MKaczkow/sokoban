package elkaproj.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Main menu view. Allows the player to choose what to do next.
 */
public class GuiMainMenuView extends JPanel {

    private String playerName;

    public GuiMainMenuView(ActionListener actionListener) {
        super();

        // set layout
        this.setBackground(Color.BLACK);
        this.setForeground(Color.WHITE);

        // panel
        JPanel menuPanel = new JPanel();
        GridLayout gl = new GridLayout(0, 1);
        gl.setVgap(8);
        menuPanel.setLayout(gl);
        menuPanel.setBackground(Color.BLACK);
        menuPanel.setForeground(Color.WHITE);

        JLabel title = new JLabel("@mainmenu.title", JLabel.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(48f));
        menuPanel.add(title);

        JButton start = new JButton("@mainmenu.items.newgame");
        start.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.WHITE));
        start.setBackground(Color.BLACK);
        start.setForeground(Color.WHITE);
        start.setFont(start.getFont().deriveFont(32f));
        start.setActionCommand(GuiRootFrame.COMMAND_NEW_GAME);
        start.addActionListener(actionListener);
        menuPanel.add(start);

        JButton scoreboard = new JButton("@mainmenu.items.scoreboard");
        scoreboard.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.WHITE));
        scoreboard.setBackground(Color.BLACK);
        scoreboard.setForeground(Color.WHITE);
        scoreboard.setFont(scoreboard.getFont().deriveFont(32f));
        scoreboard.setActionCommand(GuiRootFrame.COMMAND_SCOREBOARD);
        scoreboard.addActionListener(actionListener);
        menuPanel.add(scoreboard);

        JButton quit = new JButton("@mainmenu.items.exit");
        quit.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.WHITE));
        quit.setBackground(Color.BLACK);
        quit.setForeground(Color.WHITE);
        quit.setFont(quit.getFont().deriveFont(32f));
        quit.setActionCommand(GuiRootFrame.COMMAND_EXIT);
        quit.addActionListener(actionListener);
        menuPanel.add(quit);

        this.add(menuPanel, BorderLayout.CENTER);
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
