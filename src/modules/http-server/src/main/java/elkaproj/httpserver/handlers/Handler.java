package elkaproj.httpserver.handlers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that this class is a handler for HTTP requests.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Handler {

    /**
     * Gets the path this handler is for.
     *
     * @return Path to handle.
     */
    String value();
}
