package elkaproj.game;

import org.apache.commons.lang3.time.StopWatch;

import java.util.concurrent.TimeUnit;

/**
 * Handles counting player level solve time.
 */
public class GameClock {

    private final StopWatch stopWatch;

    /**
     * Initializes the clock.
     */
    public GameClock() {
        this.stopWatch = new StopWatch();
    }

    /**
     * Starts clock, should be executed at the beginning of each level and after resuming game.
     */
    public void start() {
        if (this.stopWatch.isSuspended())
            this.stopWatch.resume();
        else
            this.stopWatch.start();
    }

    /**
     * Stops clock, should be executed when pausing or finishing level
     *
     * @param pause Whether the stop is a pause.
     */
    public void stop(boolean pause) {
        if (!this.stopWatch.isStarted())
            return;

        if (pause)
            this.stopWatch.suspend();
        else
            this.stopWatch.stop();
    }

    /**
     * Resets clock, should be executed beginning of each level.
     */
    public void reset() {
        this.stopWatch.reset();
    }

    /**
     * Gets the amount of seconds that elapsed, excluding pauses.
     *
     * @return Number of seconds that elapsed since the timer was started, excluding pauses.
     */
    public long getElapsedSeconds() {
        return this.stopWatch.getTime(TimeUnit.SECONDS);
    }
}
