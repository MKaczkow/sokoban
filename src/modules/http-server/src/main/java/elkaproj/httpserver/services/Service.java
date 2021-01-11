package elkaproj.httpserver.services;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes a service.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Service {

    /**
     * Gets the kind of this service.
     *
     * @return Kind of this service.
     */
    ServiceKind kind();

    /**
     * Gets the type of this service.
     *
     * @return Type of this service
     */
    Class<?> type() default Object.class;
}
