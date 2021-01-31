package elkaproj.ui;

import elkaproj.DebugWriter;
import elkaproj.config.*;
import elkaproj.config.language.Language;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Comparator;

/**
 * Presents the scoreboard.
 */
public class GuiScoreboardView extends JPanel implements Runnable {

    public static final String STRING_SCOREBOARD_PLAYER_L10N_ID = "scoreboard.player";
    public static final String STRING_SCOREBOARD_SCORE_L10N_ID = "scoreboard.score";
    public static final String STRING_SCOREBOARD_NUMBER_L10N_ID = "scoreboard.number";

    private final IScoreboardStore scoreboardStore;
    private IScoreboard scoreboard;
    private final ILevelPack levelPack;

    private final GuiTotalScoreboard totalScoreboardView;
    private final GuiLevelScoreboard levelScoreboardView;

    /**
     * Initiates scoreboard UI.
     *
     * @param buttonListener  Controller action listener.
     * @param scoreboardStore Scoreboard store used to store the scoreboard.
     * @param scoreboard      Scoreboard to show.
     * @param levelPack       Level pack the scoreboard is for.
     * @param language        Language to laod strings from.
     */
    public GuiScoreboardView(ActionListener buttonListener,
                             IScoreboardStore scoreboardStore,
                             IScoreboard scoreboard,
                             ILevelPack levelPack,
                             Language language) {

        this.scoreboardStore = scoreboardStore;
        this.scoreboard = scoreboard;
        this.levelPack = levelPack;

        this.setLayout(new BorderLayout());

        JLabel scTitle = new JLabel("@scoreboard.title", JLabel.CENTER);
        scTitle.setFont(scTitle.getFont().deriveFont(18f));
        this.add(scTitle, BorderLayout.NORTH);

        JTabbedPane scTabs = new JTabbedPane();
        scTabs.add("@scoreboard.total", this.totalScoreboardView = new GuiTotalScoreboard(this.scoreboard, language));
        scTabs.add("@scoreboard.perlevel", this.levelScoreboardView = new GuiLevelScoreboard(this.scoreboard, language));
        this.add(scTabs, BorderLayout.CENTER);

        JButton scBack = new JButton("@scoreboard.back");
        scBack.setActionCommand(GuiRootFrame.COMMAND_MAINMENU);
        scBack.addActionListener(buttonListener);
        this.add(scBack, BorderLayout.SOUTH);
    }

    /**
     * Forces a scoreboard update for all views,
     */
    public void refreshScoreboard() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            this.scoreboard = this.scoreboardStore.loadScoreboard(this.levelPack);
            SwingUtilities.invokeLater(() -> {
                this.totalScoreboardView.updateScoreboard(this.scoreboard);
                this.levelScoreboardView.updateScoreboard(this.scoreboard);
            });
        } catch (IOException ex) {
            DebugWriter.INSTANCE.logError("SCORE-UI", ex, "Failed to refresh scoreboard");
        }
    }

    /**
     * Handles comparing entry instances.
     */
    public static class ScoreboardEntryComparator implements Comparator<IScoreboardEntry> {

        @Override
        public int compare(IScoreboardEntry left, IScoreboardEntry right) {
            return left.getScore() - right.getScore();
        }
    }

    /**
     * Handles comparing total entry instances.
     */
    public static class ScoreboardTotalEntryComparator implements Comparator<IScoreboardTotalEntry> {

        @Override
        public int compare(IScoreboardTotalEntry left, IScoreboardTotalEntry right) {
            return left.getScore() - right.getScore();
        }
    }
}
