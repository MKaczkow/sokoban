package elkaproj.kvcreader;

public class KVCValueConverter {

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
