package elkaproj.config.language;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a language directive.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LanguageDirective {

    /**
     * Gets the name of the directive.
     * @return Name of the directive.
     */
    public String value();
}
