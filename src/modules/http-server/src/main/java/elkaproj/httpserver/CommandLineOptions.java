package elkaproj.httpserver;

import elkaproj.config.commandline.CommandLineArgument;
import elkaproj.config.commandline.CommandLineArgumentType;

/**
 * Represents commandline options passed to the program.
 */
public class CommandLineOptions {

    @CommandLineArgument(name = "help", shorthand = 'h', type = CommandLineArgumentType.FLAG, helpText = "Displays help.")
    private boolean help;

    @CommandLineArgument(name = "debug", shorthand = 'd', type = CommandLineArgumentType.FLAG, helpText = "Enables debug mode. This prints details to the console.")
    private boolean debug;

    @CommandLineArgument(name = "port", shorthand = 'p', type = CommandLineArgumentType.NUMBER, defaultValue = "36000", helpText = "Port to listen on.")
    private int port;

    @CommandLineArgument(name = "bind", shorthand = 'b', type = CommandLineArgumentType.STRING, defaultValue = "0.0.0.0", helpText = "IP address to bind to.")
    private String bindAddress;

    @CommandLineArgument(name = "config", shorthand = 'c', type = CommandLineArgumentType.STRING, defaultValue = "config.xml", helpText = "Configuration file to load DB config from.")
    private String configurationFile;

    public CommandLineOptions() {
    }

    /**
     * Gets whether to display help.
     *
     * @return Whether to display help.
     */
    public boolean isHelp() {
        return help;
    }

    /**
     * Gets whether to write detailed debug information.
     *
     * @return Whether to write detailed debug information.
     */
    public boolean isDebug() {
        return this.debug;
    }

    /**
     * Gets the port number to bind to.
     *
     * @return Port number to bind to.
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Gets the IP address to bind to.
     *
     * @return IP address to bind to.
     */
    public String getBindAddress() {
        return this.bindAddress;
    }

    /**
     * Gets the path to DB configuration file.
     *
     * @return Path to DB configuration file.
     */
    public String getConfigurationFile() {
        return this.configurationFile;
    }
}
