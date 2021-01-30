package elkaproj;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

/**
 * Produces debug output when enabled.
 */
public class DebugWriter {

    /**
     * Singleton instance of the writer.
     */
    public static final DebugWriter INSTANCE = new DebugWriter(System.out, System.err);

    private static final String UTF8 = StandardCharsets.UTF_8.name();

    private static boolean isEnabled = false;

    private final PrintStream output, error;

    private DebugWriter(PrintStream out, PrintStream error) {
        this.output = out;
        this.error = error;
    }

    /**
     * Logs a tagged message to stdout.
     *
     * @param tag     Tag to prefix the message with.
     * @param message Message format to log.
     * @param inserts Items to insert into the formatted message.
     */
    public void logMessage(String tag, String message, Object... inserts) {
        if (!DebugWriter.isEnabled)
            return;

        this.output.printf("[%s] %s%n", tag, String.format(message, inserts));
    }

    /**
     * Logs a tagged message to stderr.
     *
     * @param tag       Tag to prefix the message with.
     * @param throwable Associated throwable, if applicable.
     * @param message   Message format to log.
     * @param inserts   Items to insert into the formatted message.
     */
    public void logError(String tag, Throwable throwable, String message, Object... inserts) {
        if (!DebugWriter.isEnabled)
            return;

        this.error.printf("[%s] %s%n", tag, String.format(message, inserts));

        if (throwable == null)
            return;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (PrintStream ps = new PrintStream(baos, true, UTF8)) {
                // fix \r\n
                try {
                    Field f = ps.getClass().getDeclaredField("textOut");
                    f.setAccessible(true);
                    Object bw = f.get(ps);
                    f = bw.getClass().getDeclaredField("lineSeparator");
                    f.setAccessible(true);
                    f.set(bw, "\n");
                } catch (Exception ignored) {
                }

                throwable.printStackTrace(ps);

                String exs = baos.toString(UTF8).trim().replace("\n", String.format("\n[%s] ", tag));
                this.error.printf("[%s] %s%n", tag, exs);
            }
        } catch (Exception ex) {
            this.error.printf("Failed to log exception.%n");
            ex.printStackTrace();
        }
    }

    /**
     * Sets whether the debug writer output is enabled. If set to false, the output will be suppressed.
     *
     * @param isEnabled Whether the writer output is enabled.
     */
    public static void setEnabled(boolean isEnabled) {
        DebugWriter.isEnabled = isEnabled;
    }
}
