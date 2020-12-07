package elkaproj.config;

import java.util.EnumSet;

/**
 * Validates {@link IConfiguration} objects, to determine whether they are valid.
 */
public class ConfigurationValidator {

    /**
     * Validates given configuration object and returns whether it is valid.
     *
     * @param configuration Configuration object to valiadte.
     * @return Whether the configuration object is valid.
     * @see IConfiguration
     */
    public static boolean validateConfiguration(IConfiguration configuration) {
        // Check if any configuration supplied at all.
        if (configuration == null) {
            return false;
        }

        // Check if any level pack is configured
        String levelPackId = configuration.getLevelPackId();
        if (levelPackId == null)
            return false;

        // Check if at least 1 max life is given
        int maxl = configuration.getMaxLives();
        if (maxl < 1)
            return false;

        // Check if starting lives is >0 and <=max
        int startl = configuration.getStartingLives();
        if (startl < 1 || startl > maxl)
            return false;

        // Check if level recovery threshold is >=2
        if (configuration.getLifeRecoveryThreshold() < 2)
            return false;

        // Check if life recovery is >0 and <=max
        int recl = configuration.getLifeRecoveryCount();
        if (recl < 1 || recl > maxl)
            return false;

        // Check if bonuses overlap with legal options
        EnumSet<GamePowerup> ref = EnumSet.allOf(GamePowerup.class);
        return ref.containsAll(configuration.getActivePowerups());
    }
}
