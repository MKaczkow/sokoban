package elkaproj.ui;

import elkaproj.config.GamePowerup;
import elkaproj.config.ILevel;
import elkaproj.config.language.Language;
import elkaproj.game.GameController;
import elkaproj.game.IGameLifecycleHandler;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.EnumSet;
import java.util.stream.Collectors;

/**
 * Maintains the status panel on the bottom of the window.
 */
public class GuiStatusPanel extends JPanel implements IGameLifecycleHandler {

    private static final String STATUS_FORMAT_L10N_ID = "status.format";
    private static final String STATUS_IDLE_L10N_ID = "status.idle";
    private static final String STATUS_NOPOWERUPS_L10N_ID = "status.nopowerups";

    private final GameController gameController;
    private final String statusFormat; // level number, level, current lives, max lives, level score, total score, active powerups
    private final String statusIdle;
    private final String statusNoPowerups;

    private final JLabel status;

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

        this.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        this.setPreferredSize(new Dimension(parent.width, 24));
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        this.status = new JLabel("@status.idle", JLabel.LEFT);
        this.status.setFont(this.status.getFont().deriveFont(12f));
        this.status.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        this.add(this.status);

        gameController.addLifecycleHandler(this);
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

    private String formatStatus(ILevel level, int currentLives, int maxLives, int currentScore, int maxScore, EnumSet<GamePowerup> activePowerups) {
        String powerups = activePowerups != null && !activePowerups.isEmpty()
                ? activePowerups.stream()
                .map(Enum::toString)
                .collect(Collectors.joining(", "))
                : this.statusNoPowerups;

        return String.format(this.statusFormat, level.getOrdinal(), level.getName(), currentLives, maxLives, currentScore, maxScore, powerups);
    }
}
