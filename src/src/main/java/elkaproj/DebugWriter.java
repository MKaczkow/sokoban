package elkaproj;

import java.io.PrintStream;

/**
 * Produces debug output when enabled.
 */
public class DebugWriter {

    /**
     * Singleton instance of the writer.
     */
    public static final DebugWriter INSTANCE = new DebugWriter(System.out, System.err);

    private static boolean isEnabled = false;

    private final PrintStream output, error;

    private DebugWriter(PrintStream out, PrintStream error) {
        this.output = out;
        this.error = error;
    }

    /**
     * Logs a tagged message to stdout.
     * @param tag Tag to prefix the message with.
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
     * @param tag Tag to prefix the message with.
     * @param throwable Associated throwable, if applicable.
     * @param message Message format to log.
     * @param inserts Items to insert into the formatted message.
     */
    public void logError(String tag, Throwable throwable, String message, Object... inserts) {
        if (!DebugWriter.isEnabled)
            return;

        this.error.printf("[%s] %s%n", tag, String.format(message, inserts));
    }

    /**
     * Sets whether the debug writer output is enabled. If set to false, the output will be suppressed.
     * @param isEnabled Whether the writer output is enabled.
     */
    public static void setEnabled(boolean isEnabled) {
        DebugWriter.isEnabled = isEnabled;
    }
}
