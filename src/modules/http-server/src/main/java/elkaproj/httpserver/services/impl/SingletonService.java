package elkaproj.httpserver.services.impl;

import elkaproj.httpserver.ServiceProvider;
import elkaproj.httpserver.services.IService;

/**
 * Type of service instantiated exactly once.
 *
 * @param <T> Type of object this service provides.
 */
public class SingletonService<T> implements IService<T> {

    private final Class<T> klass;
    private T instance;

    public SingletonService(Class<T> klass) {
        this.klass = klass;
        this.instance = null;
    }

    public SingletonService(T instance, Class<T> klass) {
        this.klass = klass;
        this.instance = instance;
    }

    @Override
    public synchronized T getInstance(ServiceProvider serviceProvider) {
        if (this.instance != null)
            return this.instance;

        return this.instance = ServiceProvider.instantiateService(this.klass, serviceProvider);
    }

    @Override
    public Class<T> getType() {
        return this.klass;
    }
}
