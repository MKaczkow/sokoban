package elkaproj.httpserver.services.impl;

import elkaproj.httpserver.ServiceProvider;
import elkaproj.httpserver.services.IService;

/**
 * Represents a service instantiated every time it is requested.
 *
 * @param <T> Type of object this service provides.
 */
public class TransientService<T> implements IService<T> {

    private final Class<T> klass;

    public TransientService(Class<T> klass) {
        this.klass = klass;
    }

    @Override
    public T getInstance(ServiceProvider serviceProvider) {
        return ServiceProvider.instantiateService(this.klass, serviceProvider);
    }

    @Override
    public Class<T> getType() {
        return this.klass;
    }
}
