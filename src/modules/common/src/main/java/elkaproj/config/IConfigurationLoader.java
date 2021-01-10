package elkaproj.config;

import java.io.Closeable;

/**
 * Loads {@link IConfiguration} objects from various objects.
 */
public interface IConfigurationLoader extends Closeable {

    /**
     * Loads configuration data.
     *
     * @return Loaded and parsed configuration data.
     */
    IConfiguration load();

    /**
     * Gets the level pack loader for this configuration loader.
     *
     * @return A level pack loader.
     * @see ILevelPackLoader
     */
    ILevelPackLoader getLevelPackLoader();
}
