package elkaproj;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Inspects objects and prints out declared fields and their values.
 */
public class Inspector {

    private final PrintStream writer;

    /**
     * Creates a new inspector which writes to specified PrintStream.
     * @param writer PrintStream to write to.
     */
    public Inspector(PrintStream writer) {
        this.writer = writer;
    }

    /**
     * Inspects an object.
     * @param o Object to inspect.
     */
    public void inspect(Object o) {
        if (o == null) {
            this.writer.println("Object is null.\n");
        }

        assert o != null;
        Class<?> type = o.getClass();
        this.writer.println("Object of type " + type);
        Field[] fields = type.getDeclaredFields();
        int ml = Arrays.stream(fields)
                .mapToInt(x -> x.getName().length())
                .max().orElse(0);

        try {
            for (Field f : fields) {
                f.setAccessible(true);
                String name = f.getName();
                int l = name.length();
                this.writer.print(name);
                this.writer.print(": ");
                if (ml - l != 0) {
                    this.writer.print(new String(new char[ml - l]).replace("\0", " "));
                }

                Object v = f.get(o);
                if (v != null) {
                    this.writer.println(v.toString());
                } else {
                    this.writer.println("<null>");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
