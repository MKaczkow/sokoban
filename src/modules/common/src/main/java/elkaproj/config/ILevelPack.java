package elkaproj.config;

/**
 * Represents a level pack, which is a collection of {@link ILevel} objects, and additional metadata.
 */
public interface ILevelPack extends Iterable<ILevel>, IXmlSerializable {

    /**
     * Gets the name of this level pack.
     *
     * @return Name of this level pack.
     */
    String getName();

    /**
     * Gets the ID of this level pack.
     *
     * @return ID of this level pack.
     */
    String getId();

    /**
     * Gets the total number of levels in this pack.
     *
     * @return Total number of levels in this pack.
     */
    int getCount();

    /**
     * Gets the specified level in this pack. Level number is an ordinal starting with 0, denoting the first level,
     * and less than the number returned by {@link #getCount()}, which, reduced by 1, denotes the last level.
     *
     * @param number Ordinal number of the level to get.
     * @return Requested level.
     * @see ILevel
     */
    ILevel getLevel(int number);
}
