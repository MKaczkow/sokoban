package elkaproj.ui;

import elkaproj.config.IScoreboard;
import elkaproj.config.IScoreboardTotalEntry;
import elkaproj.config.language.Language;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Displays total scores, that is, scores from players who completed all the levels in current level pack.
 */
public class GuiTotalScoreboard extends JPanel {

    private IScoreboard scoreboard;

    private final JTable scoreTable;
    private final String[] headers;

    /**
     * Initializes new aggregated scoreboard.
     *
     * @param scoreboard Scoreboard to display.
     * @param language   Language used for localization.
     */
    public GuiTotalScoreboard(IScoreboard scoreboard, Language language) {

        this.scoreboard = scoreboard;

        String strNumber = language.getValue(GuiScoreboardView.STRING_SCOREBOARD_NUMBER_L10N_ID);
        String strPlayer = language.getValue(GuiScoreboardView.STRING_SCOREBOARD_PLAYER_L10N_ID);
        String strScore = language.getValue(GuiScoreboardView.STRING_SCOREBOARD_SCORE_L10N_ID);
        this.headers = new String[]{strNumber, strPlayer, strScore};

        this.setLayout(new BorderLayout());

        this.scoreTable = new JTable(new ScoreboardTableModel(this.scoreboard, this.headers));
        this.add(new JScrollPane(this.scoreTable), BorderLayout.CENTER);
    }

    /**
     * Updates the scoreboard.
     *
     * @param newScoreboard New scoreboard to display.
     */
    public void updateScoreboard(IScoreboard newScoreboard) {
        this.scoreboard = newScoreboard;
        this.scoreTable.setModel(new ScoreboardTableModel(this.scoreboard, this.headers));
    }

    private static class ScoreboardTableModel extends AbstractTableModel {

        private final List<IScoreboardTotalEntry> entries;
        private final String[] headers;

        public ScoreboardTableModel(IScoreboard scoreboard, String[] headers) {
            this.entries = scoreboard.getAllTotalEntries()
                    .stream()
                    .filter(IScoreboardTotalEntry::hasCompletedAllLevels)
                    .collect(Collectors.toList());
            this.entries.sort(new GuiScoreboardView.ScoreboardTotalEntryComparator());
            this.headers = headers;
        }

        @Override
        public int getRowCount() {
            return this.entries.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public Object getValueAt(int row, int col) {
            switch (col) {
                case 0:
                    return row + 1;

                case 1:
                    return this.entries.get(row).getPlayerName();

                case 2:
                    return this.entries.get(row).getScore();

                default:
                    return null;
            }
        }

        @Override
        public String getColumnName(int i) {
            return this.headers[i];
        }

        @Override
        public Class<?> getColumnClass(int i) {
            switch (i) {
                case 0:
                case 2:
                    return Integer.TYPE;

                case 1:
                    return String.class;

                default:
                    return Object.class;
            }
        }

        @Override
        public boolean isCellEditable(int i, int i1) {
            return false;
        }
    }
}
