package elkaproj.httpserver.services;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes a service type to inject.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Inject {

    /**
     * Gets the type of service to inject.
     *
     * @return Type of service to inject.
     */
    Class<?> value();
}
