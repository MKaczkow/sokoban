package elkaproj.config.impl;

import elkaproj.config.GamePowerup;
import elkaproj.config.IConfiguration;
import elkaproj.config.IConfigurationLoader;
import elkaproj.config.ILevelPack;
import elkaproj.kvcreader.KVCName;
import elkaproj.kvcreader.KVCReader;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;

public class FileConfigurationLoader implements IConfigurationLoader, Closeable {

    private final InputStream inputStream;

    public FileConfigurationLoader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public IConfiguration load() {
        try (KVCReader<FileConfiguration> reader = new KVCReader<>(this.inputStream, FileConfiguration.class)) {
            return reader.readObject();
        } catch (IOException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        this.inputStream.close();
    }

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
