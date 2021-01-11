package elkaproj.httpserver.services;

import elkaproj.httpserver.ServiceProvider;

/**
 * Represents a service unit.
 *
 * @param <T> Type of object this service provides.
 */
public interface IService<T> {

    /**
     * Resolves the service and all of its dependencies, then returns it.
     *
     * @param serviceProvider Service provider to resolve dependencies with.
     * @return Resolved service.
     */
    T getInstance(ServiceProvider serviceProvider);

    /**
     * Gets the service type.
     *
     * @return Type of the service.
     */
    Class<T> getType();
}
