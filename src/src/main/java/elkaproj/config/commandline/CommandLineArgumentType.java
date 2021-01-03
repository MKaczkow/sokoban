package elkaproj.config.commandline;

/**
 * Defines the type of an argument.
 */
public enum CommandLineArgumentType {
    /**
     * Defines that an argument is a flag argument (i.e. its underlying type is bool, its absence sets the value to false, its presence sets it to true).
     */
    FLAG,

    /**
     * Defines that an argument is a numeric argument (i.e. its underlying type is int, its absence sets the value to specified default).
     */
    NUMBER,

    /**
     * Defines that an argument is a string argument (i.e. its underlying type is string, its absence sets the value to specified default).
     */
    STRING;
}
