package elkaproj.ui;

import elkaproj.config.GamePowerup;
import elkaproj.config.ILevel;
import elkaproj.config.language.Language;
import elkaproj.game.GameController;
import elkaproj.game.IGameLifecycleHandler;
import elkaproj.game.ITimerUpdateHandler;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.EnumSet;
import java.util.stream.Collectors;

/**
 * Maintains the status panel on the bottom of the window.
 */
public class GuiStatusPanel extends JPanel implements IGameLifecycleHandler, ITimerUpdateHandler {

    private static final String STATUS_FORMAT_L10N_ID = "status.format";
    private static final String STATUS_IDLE_L10N_ID = "status.idle";
    private static final String STATUS_NOPOWERUPS_L10N_ID = "status.nopowerups";
    private static final String STATUS_TIME_L10N_ID = "status.time";

    private static final Color COLOR_VGOOD = new Color(0, 0x66, 0);
    private static final Color COLOR_GOOD = new Color(0, 0, 0x66);
    private static final Color COLOR_NEUTRAL = Color.BLACK;
    private static final Color COLOR_BAD = new Color(0xBF, 0x66, 0);
    private static final Color COLOR_VBAD = new Color(0x99, 0, 0);

    private final GameController gameController;
    private final String statusFormat; // level number, level, current lives, max lives, level score, total score, active powerups
    private final String statusIdle;
    private final String statusNoPowerups;
    private final String timeFormat; // current

    private final JLabel status, time;

    /**
     * Initializes the status panel.
     *
     * @param gameController Game controller.
     * @param parent         Size of the parent container.
     * @param language       Language to use when localizing strings.
     */
    public GuiStatusPanel(GameController gameController, Dimension parent, Language language) {
        super();

        this.gameController = gameController;
        this.statusFormat = language.getValue(STATUS_FORMAT_L10N_ID);
        this.statusIdle = language.getValue(STATUS_IDLE_L10N_ID);
        this.statusNoPowerups = language.getValue(STATUS_NOPOWERUPS_L10N_ID);
        this.timeFormat = language.getValue(STATUS_TIME_L10N_ID);

        this.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        this.setPreferredSize(new Dimension(parent.width, 24));
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        this.status = new JLabel("@status.idle", JLabel.LEFT);
        this.status.setFont(this.status.getFont().deriveFont(12f));
        this.status.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        this.add(this.status);

        this.time = new JLabel("", JLabel.RIGHT);
        this.time.setFont(this.status.getFont());
        this.time.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        this.add(this.time);

        gameController.addLifecycleHandler(this);
        gameController.addTimerUpdateHandler(this);
    }

    @Override
    public void onGameStarted(ILevel currentLevel, int currentLives) {
        this.status.setText(this.formatStatus(currentLevel,
                currentLives,
                this.gameController.getMaxLives(),
                0,
                0,
                this.gameController.getActivePowerups()));
    }

    @Override
    public void onGameStopped(int totalScore, boolean completed) {
        this.status.setText(this.statusIdle);
        this.time.setText("");
    }

    @Override
    public void onNextLevel(ILevel previousLevel, int previousLevelScore, ILevel currentLevel, int totalScore) {
        this.status.setText(this.formatStatus(currentLevel,
                this.gameController.getCurrentLives(),
                this.gameController.getMaxLives(),
                0,
                totalScore,
                this.gameController.getActivePowerups()));
    }

    @Override
    public void onLivesUpdated(int currentLives, int maxLives) {
        this.status.setText(this.formatStatus(this.gameController.getCurrentLevel(),
                currentLives,
                maxLives,
                this.gameController.getCurrentScore(),
                this.gameController.getTotalScore(),
                this.gameController.getActivePowerups()));
    }

    @Override
    public void onScoreUpdated(int currentScore, int totalScore) {
        this.status.setText(this.formatStatus(this.gameController.getCurrentLevel(),
                this.gameController.getCurrentLives(),
                this.gameController.getMaxLives(),
                currentScore,
                totalScore,
                this.gameController.getActivePowerups()));
    }

    @Override
    public void onPowerupsUpdated(EnumSet<GamePowerup> activePowerups) {
        this.status.setText(this.formatStatus(this.gameController.getCurrentLevel(),
                this.gameController.getCurrentLives(),
                this.gameController.getMaxLives(),
                this.gameController.getCurrentScore(),
                this.gameController.getTotalScore(),
                activePowerups));
    }

    @Override
    public void onTimerUpdated(long current, long bonus, long penalty, long fail) {
        this.time.setText(String.format(this.timeFormat, current));

        assert bonus < penalty;
        assert penalty < fail;
        assert penalty - fail > 5;
        assert bonus > 5;

        if (current >= fail - 5)
            this.time.setForeground(COLOR_VBAD);
        else if (current > penalty)
            this.time.setForeground(COLOR_BAD);
        else if (current >= bonus)
            this.time.setForeground(COLOR_NEUTRAL);
        else if (current >= bonus - 5)
            this.time.setForeground(COLOR_GOOD);
        else if (current < bonus - 5 && current > 0)
            this.time.setForeground(COLOR_VGOOD);
        else
            this.time.setForeground(COLOR_NEUTRAL);
    }

    private String formatStatus(ILevel level, int currentLives, int maxLives, int currentScore, int maxScore, EnumSet<GamePowerup> activePowerups) {
        String powerups = activePowerups != null && !activePowerups.isEmpty()
                ? activePowerups.stream()
                .map(Enum::toString)
                .collect(Collectors.joining(", "))
                : this.statusNoPowerups;

        return String.format(this.statusFormat, level.getOrdinal(), level.getName(), currentLives, maxLives, currentScore, maxScore, powerups);
    }
}
