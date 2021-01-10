package elkaproj.config.commandline;

/**
 * Represents commandline options passed to the program.
 */
public class CommandLineOptions {

    @CommandLineArgument(name = "help", shorthand = 'h', type = CommandLineArgumentType.FLAG, helpText = "Displays help.")
    private boolean help;

    @CommandLineArgument(name = "debug", shorthand = 'd', type = CommandLineArgumentType.FLAG, helpText = "Enables debug mode. This prints details to the console.")
    private boolean debug;

    @CommandLineArgument(name = "online", shorthand = 'o', type = CommandLineArgumentType.FLAG, helpText = "Enables online mode. This causes the game to pull configuration data from the specified server.")
    private boolean online;

    @CommandLineArgument(name = "online-endpoint", shorthand = 's', type = CommandLineArgumentType.STRING, defaultValue = "http://localhost/proze", helpText = "HTTP endpoint to pull configuration data from.")
    private String onlineEndpoint;

    @CommandLineArgument(name = "language", shorthand = 'l', type = CommandLineArgumentType.STRING, defaultValue = "pl-PL", helpText = "UI language.")
    private String language;

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
     * Gets whether to connect to an online server to load configuration data.
     *
     * @return Whether to connect to an online server to load configuration data.
     */
    public boolean useOnline() {
        return this.online;
    }

    /**
     * Gets the address of the online configuration endpoint.
     *
     * @return Address of online configuration endpoint.
     */
    public String getOnlineEndpoint() {
        return this.onlineEndpoint;
    }

    /**
     * Gets the interface language code.
     *
     * @return Interface language code.
     */
    public String getLanguage() {
        return this.language;
    }
}
