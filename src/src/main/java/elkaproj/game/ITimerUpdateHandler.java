package elkaproj.game;

/**
 * Handles events pertaining to game timer updates.
 */
public interface ITimerUpdateHandler {

    /**
     * Triggered whenever the timer is updated.
     *
     * @param current Current timer value.
     * @param bonus   Bonus time threshold.
     * @param penalty Penalty time threshold.
     * @param fail    Fail time threshold.
     */
    void onTimerUpdated(long current, long bonus, long penalty, long fail);
}
