package elkaproj.kvcreader;

/**
 * Converts string values from a Key-Value Configuration stream to appropriate types in their POJO containers.
 */
public class KVCValueConverter {

    /**
     * Converts a string value to indicated type. Returns null if conversion failed.
     * @param value String value to convert.
     * @param type Type to convert to.
     * @return Converted value.
     */
    public static Object convert(String value, Class<?> type) {
        if (type.equals(String.class)) {
            return value;
        } else if (type.equals(Boolean.TYPE)) {
            return Boolean.valueOf(value);
        } else if (type.equals(Integer.TYPE)) {
            return Integer.valueOf(value);
        }

        return null;
    }
}
