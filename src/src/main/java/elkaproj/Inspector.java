package elkaproj;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Inspects objects and prints out declared fields and their values.
 */
public class Inspector {

    /**
     * Singleton instance of the inspector.
     */
    public static final Inspector INSTANCE = new Inspector();

    /**
     * Creates a new inspector which writes to specified PrintStream.
     */
    private Inspector() {
    }

    /**
     * Inspects an object.
     * @param o Object to inspect.
     */
    public void inspect(Object o) {
        if (o == null) {
            DebugWriter.INSTANCE.logMessage("INSPECT", "Object is null.");
        }

        assert o != null;
        Class<?> type = o.getClass();
        DebugWriter.INSTANCE.logMessage("INSPECT", "Object of type " + type);
        Field[] fields = type.getDeclaredFields();
        int ml = Arrays.stream(fields)
                .mapToInt(x -> x.getName().length())
                .max().orElse(0);

        try {
            for (Field f : fields) {
                f.setAccessible(true);
                String name = f.getName();
                int l = name.length();
                Object v = f.get(o);
                DebugWriter.INSTANCE.logMessage("INSPECT", "%s: %s%s",
                        name,
                        ml - l != 0 ? new String(new char[ml - l]).replace("\0", " ") : "",
                        v != null ? v.toString() : "<null>");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
