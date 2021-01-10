package elkaproj.game;

import elkaproj.config.*;
import elkaproj.config.IConfiguration;
import elkaproj.config.ILevelPack;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class GameClock {

    Timer timer;

    private final IConfiguration configuration;
    private final ILevelPack levelPack;

    private int bonusTimeThreshold;
    private int penaltyTimeThreshold;
    private int failTimeThreshold;

    /**
     * Initializes the clock.
     * @param configuration Configuration to use for this game.
     * @param levelPack Level pack the player will play through.
     */
    public GameClock(IConfiguration configuration, ILevelPack levelPack) {
        this.configuration = configuration;
        this.levelPack = levelPack;
        // TODO: set time thresholds from configuration files, but how??
    }

    /**
     * Starts clock, should be executed at the beggining of each level and after resuming game.
     */
    public void startClock(){

    }

    /**
     * Stops clock, should be executed when pausing or finishing level
     */
    public void stopClock(){

    }

    /**
     * Resets clock, should be executed beggining of each level.
     */
    public void resetClock(){

    }
}
