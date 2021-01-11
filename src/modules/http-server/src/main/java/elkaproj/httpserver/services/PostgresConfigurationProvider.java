package elkaproj.httpserver.services;

import elkaproj.DebugWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Loads PostgreSQL configuration files.
 */
public class PostgresConfigurationProvider {

    private final String configurationFilePath;

    private Properties props = null;
    private String url = null;

    private PostgresXmlConfiguration configuration = null;

    /**
     * Initializes a new configuration provider with specified config path.
     *
     * @param configurationFilePath Path to configuration file.
     */
    public PostgresConfigurationProvider(String configurationFilePath) {
        this.configurationFilePath = configurationFilePath;
    }

    /**
     * Retrieves all necessary connection parameters.
     *
     * @return Additional connection parameters.
     * @throws IOException Loading XML configuration fails.
     */
    public synchronized Properties getProperties() throws IOException {
        if (this.props != null)
            return this.props;

        PostgresXmlConfiguration xmlConfig = this.loadXmlConfiguration();
        this.props = new Properties();
        this.props.setProperty("user", xmlConfig.username);
        this.props.setProperty("password", xmlConfig.password);
        this.props.setProperty("ssl", String.valueOf(xmlConfig.ssl));
        this.props.setProperty("sslmode", xmlConfig.ssl ? "require" : "disable");
        return this.props;
    }

    /**
     * Retrieves the connection URL.
     *
     * @return JDBC connection URL.
     * @throws IOException Loading XML configuration fails.
     */
    public synchronized String getUrl() throws IOException {
        if (this.url != null)
            return this.url;

        PostgresXmlConfiguration xmlConfig = this.loadXmlConfiguration();
        return this.url = String.format("jdbc:postgresql://%s:%d/%s", xmlConfig.host, xmlConfig.port, xmlConfig.database);
    }

    private synchronized PostgresXmlConfiguration loadXmlConfiguration() throws IOException {
        if (this.configuration != null)
            return this.configuration;

        try {
            JAXBContext jaxbctx = JAXBContext.newInstance(PostgresXmlConfiguration.class);
            Unmarshaller jaxb = jaxbctx.createUnmarshaller();
            return (PostgresXmlConfiguration) jaxb.unmarshal(new File(this.configurationFilePath));
        } catch (JAXBException e) {
            DebugWriter.INSTANCE.logError("LDR-XCONF", e, "Error while loading file.");
            throw new IOException(e);
        }
    }

    @XmlRootElement(name = "pg-config")
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class PostgresXmlConfiguration {

        @XmlElement(name = "pg-host")
        public String host;

        @XmlElement(name = "pg-port")
        public int port;

        @XmlElement(name = "pg-db")
        public String database;

        @XmlElement(name = "pg-user")
        public String username;

        @XmlElement(name = "pg-pass")
        public String password;

        @XmlElement(name = "pg-ssl")
        public boolean ssl;
    }
}
