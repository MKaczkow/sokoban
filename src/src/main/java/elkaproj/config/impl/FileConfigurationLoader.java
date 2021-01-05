package elkaproj.config.impl;

import elkaproj.config.GamePowerup;
import elkaproj.config.IConfiguration;
import elkaproj.config.IConfigurationLoader;
import elkaproj.config.ILevelPackLoader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.*;
import java.util.EnumSet;
import java.util.List;

/**
 * Loads configuration from file streams. The configuration uses XML format. The configuration is case-sensitive, and
 * needs to have following keys:
 *
 * <ul>
 *     <li>max-lives - {@link IConfiguration#getMaxLives()}</li>
 *     <li>start-lives - {@link IConfiguration#getStartingLives()}</li>
 *     <li>zero o more active-powerup elements - {@link IConfiguration#getActivePowerups()} and {@link GamePowerup}</li>
 *     <li>life-recovery-threshold - {@link IConfiguration#getLifeRecoveryThreshold()}</li>
 *     <li>life-recovery-count - {@link IConfiguration#getLifeRecoveryCount()}</li>
 *     <li>timers-active - {@link IConfiguration#areTimersActive()}</li>
 *     <li>level-pack - {@link IConfiguration#getLevelPackId()}</li>
 * </ul>
 * @see IConfigurationLoader
 * @see IConfiguration
 */
public class FileConfigurationLoader implements IConfigurationLoader, Closeable {

    private final FileInputStream inputStream;
    private final File file;

    /**
     * Creates a new configuration loader from given file stream.
     * @param file File to read configuration from.
     */
    public FileConfigurationLoader(File file) throws FileNotFoundException {
        this.file = file;
        this.inputStream = new FileInputStream(file);
    }

    /**
     * Loads a configuration object and returns it.
     * @return Loaded configuration object.
     * @see IConfiguration
     */
    @Override
    public IConfiguration load() {
        try {
            JAXBContext jaxbctx = JAXBContext.newInstance(XmlFileConfiguration.class);
            Unmarshaller jaxb = jaxbctx.createUnmarshaller();
            return (IConfiguration) jaxb.unmarshal(this.inputStream);
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the file-based level pack loader.
     * @return File level pack loader.
     */
    @Override
    public ILevelPackLoader getLevelPackLoader() {
        File maps = new File(this.file.getParent(), "maps");
        return new FileLevelPackLoader(maps);
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
     * Implements an XML-bindable configuration object. For more information see {@link IConfiguration}.
     * @see IConfiguration
     */
    @XmlRootElement(name="configuration")
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class XmlFileConfiguration implements IConfiguration {

        @XmlElement(name="level-pack")
        private String levelPackId;

        @XmlElement(name="max-lives")
        public int maxLives;

        @XmlElement(name="start-lives")
        public int startLives;

        @XmlElement(name="life-recovery-threshold")
        public int lifeRecoveryThreshold;

        @XmlElement(name="life-recovery-count")
        public int lifeRecoveryCount;

        @XmlElement(name="timers-active")
        private boolean timersActive;

        @XmlElement(name="active-powerup")
        private List<String> activePowerups;

        private transient EnumSet<GamePowerup> activePowerupsES;

        private XmlFileConfiguration() { }

        @Override
        public String getLevelPackId() {
            return this.levelPackId;
        }

        @Override
        public int getMaxLives() {
            return this.maxLives;
        }

        @Override
        public int getStartingLives() {
            return this.startLives;
        }

        @Override
        public int getLifeRecoveryThreshold() {
            return this.lifeRecoveryThreshold;
        }

        @Override
        public int getLifeRecoveryCount() {
            return this.lifeRecoveryCount;
        }

        @Override
        public boolean areTimersActive() {
            return this.timersActive;
        }

        @Override
        public EnumSet<GamePowerup> getActivePowerups() {
            if (this.activePowerupsES != null)
                return this.activePowerupsES;

            EnumSet<GamePowerup> powerups = EnumSet.noneOf(GamePowerup.class);
            for (String s : this.activePowerups) {
                powerups.add(GamePowerup.valueOf(s));
            }

            return this.activePowerupsES = powerups;
        }
    }
}
