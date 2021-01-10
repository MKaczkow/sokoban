package elkaproj.config.impl;

import elkaproj.DebugWriter;
import elkaproj.Entry;
import elkaproj.config.GamePowerup;
import elkaproj.config.IConfiguration;
import elkaproj.config.IConfigurationLoader;
import elkaproj.config.ILevelPackLoader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.EnumSet;
import java.util.List;

/**
 * Loads configuration from HTTP endpoints. The configuration uses XML format. The configuration is case-sensitive, and
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
public class HttpConfigurationLoader implements IConfigurationLoader {

    private final URL endpointBase;

    /**
     * Creates a new configuration loader from given URL endpoint.
     *
     * @param endpointBase URL endpoint to load configuration from.
     */
    public HttpConfigurationLoader(URL endpointBase) {
        this.endpointBase = endpointBase;
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
            URL url = new URL(this.endpointBase, this.appendPath(this.endpointBase.getPath(), "config.xml"));

            JAXBContext jaxbctx = JAXBContext.newInstance(XmlHttpConfiguration.class);
            Unmarshaller jaxb = jaxbctx.createUnmarshaller();

            URLConnection con = url.openConnection();
            con.setRequestProperty("User-Agent", Entry.USER_AGENT);
            try (InputStream is = con.getInputStream()) {
                return (IConfiguration) jaxb.unmarshal(is);
            }
        } catch (Exception ex) {
            DebugWriter.INSTANCE.logError("LDR-HTTP", ex, "Error while loading file.");
            return null;
        }
    }

    /**
     * Gets the HTTP-based level pack loader.
     *
     * @return HTTP level pack loader.
     */
    @Override
    public ILevelPackLoader getLevelPackLoader() {
        try {
            URL url = new URL(this.endpointBase, this.appendPath(this.endpointBase.getPath(), "maps"));
            return new HttpLevelPackLoader(url);
        } catch (MalformedURLException ex) {
            DebugWriter.INSTANCE.logError("HTTP-LDR", ex, "Failed to construct HTTP level loader.");
            return null;
        }
    }

    /**
     * Closes this reader.
     *
     * @throws IOException An exception occurred while closing the reader.
     */
    @Override
    public void close() throws IOException {
    }

    private String appendPath(String base, String appendix) {
        if (appendix.startsWith("/"))
            appendix = appendix.substring(1);

        if (base.endsWith("/"))
            return base + appendix;
        else
            return base + "/" + appendix;
    }

    /**
     * Implements an XML-bindable configuration object. For more information see {@link IConfiguration}.
     *
     * @see IConfiguration
     */
    @XmlRootElement(name = "configuration")
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class XmlHttpConfiguration implements IConfiguration {

        @XmlElement(name = "level-pack")
        private String levelPackId;

        @XmlElement(name = "max-lives")
        public int maxLives;

        @XmlElement(name = "start-lives")
        public int startLives;

        @XmlElement(name = "life-recovery-threshold")
        public int lifeRecoveryThreshold;

        @XmlElement(name = "life-recovery-count")
        public int lifeRecoveryCount;

        @XmlElement(name = "timers-active")
        private boolean timersActive;

        @XmlElement(name = "active-powerup")
        private List<String> activePowerups;

        private transient EnumSet<GamePowerup> activePowerupsES;

        private XmlHttpConfiguration() {
        }

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
