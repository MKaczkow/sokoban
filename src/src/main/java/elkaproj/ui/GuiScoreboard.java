package elkaproj.ui;

import elkaproj.config.IScoreboard;
import elkaproj.config.IScoreboardStore;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Presents the scoreboard.
 */
public class GuiScoreboard extends JPanel {

    private final IScoreboardStore scoreboardStore;
    private final IScoreboard scoreboard;

    /**
     * Initiates scoreboard UI.
     *
     * @param buttonListener  controller listener
     * @param scoreboardStore Scoreboard store used to store the scoreboard.
     * @param scoreboard      Scoreboard holding current highscores.
     */
    public GuiScoreboard(ActionListener buttonListener, IScoreboardStore scoreboardStore, IScoreboard scoreboard) {

        this.scoreboardStore = scoreboardStore;
        this.scoreboard = scoreboard;

        this.setLayout(new BorderLayout());

        JLabel scTitle = new JLabel("@scoreboard.title", JLabel.CENTER);
        scTitle.setFont(scTitle.getFont().deriveFont(18f));
        this.add(scTitle, BorderLayout.NORTH);

        JTabbedPane scTabs = new JTabbedPane();

        scTabs.add("@scoreboard.total", new JLabel("@scoreboard.total"));

        scTabs.add("@scoreboard.perlevel", new JLabel("@scoreboard.perlevel"));

        this.add(scTabs, BorderLayout.CENTER);

        JButton scBack = new JButton("@scoreboard.back");
        scBack.setActionCommand(GuiRootFrame.COMMAND_MAINMENU);
        scBack.addActionListener(buttonListener);
        this.add(scBack, BorderLayout.SOUTH);
    }

    /**
     * Initiates scoreboard label
     *
     * @return scoreboard label
     */
    private JLabel makeScoreboardLalorcabel() {
        JLabel highScoreLbl = new JLabel("<html><br> Scoreboard <br><br></html>");
        highScoreLbl.setHorizontalAlignment(JLabel.CENTER);
        highScoreLbl.setVerticalAlignment(JLabel.CENTER);
        highScoreLbl.setFont(new Font("Arial", Font.PLAIN, 15));
        return highScoreLbl;
    }
}
