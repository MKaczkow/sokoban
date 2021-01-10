package elkaproj.config;

import java.io.Closeable;
import java.io.IOException;

/**
 * Loads {@link ILevelPack} instances from various sources.
 */
public interface ILevelPackLoader extends Closeable {

    /**
     * Loads the specified level pack, and returns it.
     *
     * @param id ID of the level pack to load.
     * @return Loaded level pack.
     * @throws IOException IO exception occurred while loading levels.
     * @see ILevelPack
     */
    ILevelPack loadPack(String id) throws IOException;
}
