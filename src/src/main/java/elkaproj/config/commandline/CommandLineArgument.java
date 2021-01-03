package elkaproj.config.commandline;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a command line argument passed to the application.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CommandLineArgument {
    /**
     * Gets the full name of the argument (e.g. my-argument). This name will be used when parsing values such as --my-argument.
     * @return Full name of the argument.
     */
    String name();

    /**
     * Gets the type of the argument. Used when parsing.
     * @return Type of the argument.
     */
    CommandLineArgumentType type();

    /**
     * Gets the shorthand name of the argument (e.g. a). This name will be used when parsing values such as -a.
     * @return Shorthand name of the argument.
     */
    char shorthand() default '\0';

    /**
     * Gets the default value of the argument. Applicable only for arguments of type other than {@link CommandLineArgumentType#FLAG}.
     * @return Default value of the argument, expressed as string.
     */
    String defaultValue() default "";

    /**
     * Gets the help text to display in usage help view.
     * @return Help text, meant to describe the purpose of the argument.
     */
    String helpText();
}
