package elkaproj.config.impl;

import elkaproj.Common;
import elkaproj.DebugWriter;
import elkaproj.config.GamePowerup;
import elkaproj.config.IConfiguration;
import elkaproj.config.IConfigurationLoader;
import elkaproj.config.ILevelPackLoader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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
            URL url = new URL(this.endpointBase, Common.appendPath(this.endpointBase.getPath(), "configuration"));

            JAXBContext jaxbctx = JAXBContext.newInstance(XmlConfigImpl.XmlConfiguration.class);
            Unmarshaller jaxb = jaxbctx.createUnmarshaller();

            URLConnection con = url.openConnection();
            con.setRequestProperty("User-Agent", Common.USER_AGENT);
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
            URL url = new URL(this.endpointBase, Common.appendPath(this.endpointBase.getPath(), "levels"));
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
}
