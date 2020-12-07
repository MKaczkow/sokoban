package elkaproj.config.impl;

import elkaproj.config.GamePowerup;
import elkaproj.config.IConfiguration;
import elkaproj.config.IConfigurationLoader;
import elkaproj.config.ILevelPack;
import elkaproj.kvcreader.KVCName;
import elkaproj.kvcreader.KVCReader;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;

/**
 * Loads configuration from file streams. The configuration uses Key-Value Configuration format, which is an ini-like simple text format. The configuration uses a line starting with dot (.) to denote
 * the end of one configuration stream. This allows for embedding multiple configuration streams or other data in a configuration file. The configuration is case-sensitive, and needs to have
 * following keys:
 *
 * <ul>
 *     <li>max lives - {@link IConfiguration#getMaxLives()}</li>
 *     <li>starting lives - {@link IConfiguration#getStartingLives()}</li>
 *     <li>active powerups - {@link IConfiguration#getActivePowerups()}</li>
 *     <li>life recovery threshold - {@link IConfiguration#getLifeRecoveryThreshold()}</li>
 *     <li>life recovery count - {@link IConfiguration#getLifeRecoveryMagnitude()}</li>
 *     <li>timers active - {@link IConfiguration#areTimersActive()}</li>
 *     <li>level pack - {@link IConfiguration#getLevelPack()}</li>
 * </ul>
 * @see IConfigurationLoader
 * @see IConfiguration
 */
public class FileConfigurationLoader implements IConfigurationLoader, Closeable {

    private final FileInputStream inputStream;

    /**
     * Creates a new configuration loader from given file stream.
     * @param inputStream Input stream to read configuration from.
     */
    public FileConfigurationLoader(FileInputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * Loads a configuration object and returns it.
     * @return Loaded configuration object.
     * @see IConfiguration
     */
    @Override
    public IConfiguration load() {
        try (KVCReader<FileConfiguration> reader = new KVCReader<>(this.inputStream, FileConfiguration.class)) {
            return reader.readObject();
        } catch (IOException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Closes this reader and the underlying stream.
     * @throws IOException An exception occured while closing the underlying stream.
     */
    @Override
    public void close() throws IOException {
        this.inputStream.close();
    }

    /**
     * A configuration object loaded from a file. For documentation of individual methods, see {@link IConfiguration}.
     * @see IConfiguration
     */
    private static class FileConfiguration implements IConfiguration {

        @KVCName(name="level pack")
        private ILevelPack levelPack;

        @KVCName(name="max lives")
        private int maxLives;

        @KVCName(name="starting lives")
        private int startingLives;

        @KVCName(name="life recovery threshold")
        private int lifeRecoveryThreshold;

        @KVCName(name="life recovery count")
        private int lifeRecoveryMagnitude;

        @KVCName(name="timers active")
        private boolean timersActive;

        @KVCName(name="active powerups")
        private EnumSet<GamePowerup> activePowerups;

        @Override
        public ILevelPack getLevelPack() {
            return this.levelPack;
        }

        @Override
        public int getMaxLives() {
            return this.maxLives;
        }

        @Override
        public int getStartingLives() {
            return this.startingLives;
        }

        @Override
        public int getLifeRecoveryThreshold() {
            return 0;
        }

        @Override
        public int getLifeRecoveryMagnitude() {
            return 0;
        }

        @Override
        public boolean areTimersActive() {
            return false;
        }

        @Override
        public EnumSet<GamePowerup> getActivePowerups() {
            return null;
        }
    }
}
