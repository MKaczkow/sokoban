package elkaproj.ui;

import elkaproj.config.ILevel;
import elkaproj.config.ILevelPack;
import elkaproj.config.IScoreboard;
import elkaproj.config.IScoreboardEntry;
import elkaproj.config.language.Language;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

/**
 * Displays per-level leaderboards.
 */
public class GuiLevelScoreboard extends JPanel {

    private IScoreboard scoreboard;
    private ILevel selectedLevel = null;

    private final JTable scoreTable;
    private final String[] headers;

    /**
     * Initializes new breakdown scoreboard.
     *
     * @param scoreboard Scoreboard to display.
     * @param language   Language used for localization.
     */
    public GuiLevelScoreboard(IScoreboard scoreboard, Language language) {

        this.scoreboard = scoreboard;

        String strNumber = language.getValue(GuiScoreboard.STRING_SCOREBOARD_NUMBER_L10N_ID);
        String strPlayer = language.getValue(GuiScoreboard.STRING_SCOREBOARD_PLAYER_L10N_ID);
        String strScore = language.getValue(GuiScoreboard.STRING_SCOREBOARD_SCORE_L10N_ID);
        this.headers = new String[]{strNumber, strPlayer, strScore};

        this.setLayout(new BorderLayout());

        JPanel selectorContainer = new JPanel();
        selectorContainer.setLayout(new BoxLayout(selectorContainer, BoxLayout.LINE_AXIS));

        selectorContainer.add(new JLabel("@scoreboard.level"));
        selectorContainer.add(Box.createRigidArea(new Dimension(16, 0)));

        JComboBox<ILevel> levelSelector = new JComboBox<>(new LevelModel(scoreboard.getLevelPack(), this));
        levelSelector.setRenderer(new LevelRenderer());
        selectorContainer.add(levelSelector);

        this.add(selectorContainer, BorderLayout.NORTH);

        this.scoreTable = new JTable(null); //new ScoreboardTableModel(this.scoreboard, this.headers));
        this.add(new JScrollPane(this.scoreTable), BorderLayout.CENTER);
    }

    /**
     * Updates the scoreboard.
     *
     * @param newScoreboard New scoreboard to display.
     */
    public void updateScoreboard(IScoreboard newScoreboard) {
        this.scoreboard = newScoreboard;
        if (this.selectedLevel != null)
            this.scoreTable.setModel(new ScoreboardTableModel(this.scoreboard, this.headers, this.selectedLevel));
    }

    private static class ScoreboardTableModel extends AbstractTableModel {

        private final List<IScoreboardEntry> entries;
        private final String[] headers;

        public ScoreboardTableModel(IScoreboard scoreboard, String[] headers, ILevel level) {
            this.entries = scoreboard.getLevelEntries(level);
            this.entries.sort(new GuiScoreboard.ScoreboardEntryComparator());
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

    private static class LevelModel implements ComboBoxModel<ILevel> {

        private GuiLevelScoreboard parent;
        private ILevelPack levelPack;

        public LevelModel(ILevelPack levelPack, GuiLevelScoreboard parent) {
            this.levelPack = levelPack;
            this.parent = parent;
        }

        @Override
        public void setSelectedItem(Object o) {
            this.parent.selectedLevel = (ILevel) o;
            this.parent.updateScoreboard(this.parent.scoreboard);
        }

        @Override
        public Object getSelectedItem() {
            return this.parent.selectedLevel;
        }

        @Override
        public int getSize() {
            return this.levelPack.getCount();
        }

        @Override
        public ILevel getElementAt(int i) {
            return this.levelPack.getLevel(i);
        }

        @Override
        public void addListDataListener(ListDataListener listDataListener) {

        }

        @Override
        public void removeListDataListener(ListDataListener listDataListener) {

        }
    }

    private static class LevelRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> jList, Object o, int i, boolean b, boolean b1) {
            if (o instanceof ILevel)
                o = ((ILevel) o).getName();

            return super.getListCellRendererComponent(jList, o, i, b, b1);
        }
    }
}
