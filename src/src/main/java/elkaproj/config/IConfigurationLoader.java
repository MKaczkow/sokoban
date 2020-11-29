package elkaproj.config;

/**
 * Loads {@link IConfiguration} objects from various objects.
 */
public interface IConfigurationLoader {

    /**
     * Loads configuration data.
     *
     * @return Loaded and parsed configuration data.
     */
    IConfiguration load();
}
