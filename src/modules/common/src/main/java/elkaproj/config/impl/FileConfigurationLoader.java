package elkaproj.config.impl;

import elkaproj.DebugWriter;
import elkaproj.config.GamePowerup;
import elkaproj.config.IConfiguration;
import elkaproj.config.IConfigurationLoader;
import elkaproj.config.ILevelPackLoader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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
 *
 * @see IConfigurationLoader
 * @see IConfiguration
 */
public class FileConfigurationLoader implements IConfigurationLoader, Closeable {

    private final File file;

    /**
     * Creates a new configuration loader from given file stream.
     *
     * @param file File to read configuration from.
     * @throws FileNotFoundException Configuration file was not found.
     */
    public FileConfigurationLoader(File file) throws FileNotFoundException {
        this.file = file;
    }

    /**
     * Loads a configuration object and returns it.
     *
     * @return Loaded configuration object.
     * @see IConfiguration
     */
    @Override
    public IConfiguration load() {
        try {
            JAXBContext jaxbctx = JAXBContext.newInstance(XmlConfigImpl.XmlConfiguration.class);
            Unmarshaller jaxb = jaxbctx.createUnmarshaller();
            return (IConfiguration) jaxb.unmarshal(this.file);
        } catch (JAXBException e) {
            DebugWriter.INSTANCE.logError("LDR-FILE", e, "Error while loading file.");
            return null;
        }
    }

    /**
     * Gets the file-based level pack loader.
     *
     * @return File level pack loader.
     */
    @Override
    public ILevelPackLoader getLevelPackLoader() {
        File maps = new File(this.file.getParent(), "maps");
        return new FileLevelPackLoader(maps);
    }

    /**
     * Closes this reader and the underlying stream.
     *
     * @throws IOException An exception occurred while closing the underlying stream.
     */
    @Override
    public void close() throws IOException {
    }
}
