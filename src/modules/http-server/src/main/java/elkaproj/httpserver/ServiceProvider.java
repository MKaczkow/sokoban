package elkaproj.httpserver;

import elkaproj.httpserver.services.IService;
import elkaproj.httpserver.services.impl.SingletonService;
import elkaproj.httpserver.services.impl.TransientService;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Provides a simple dependency injection mechanism.
 */
public class ServiceProvider {

    private final List<ServiceInfo> descriptors;

    private ServiceProvider(List<ServiceInfo> descriptors) {
        this.descriptors = descriptors;
    }

    /**
     * Resolves a given service and returns it.
     *
     * @param klass Type of service to resolve.
     * @param <T>   Type of service to resolve.
     * @return Resolved service.
     */
    public <T> IService<T> resolveService(Class<T> klass) {
        IService<T> candidate = null;
        for (ServiceInfo service : this.descriptors) {
            if (service.klass.isAssignableFrom(klass)) {
                if (candidate != null)
                    throw new IllegalStateException("More than 1 service registered matches specified type.");

                candidate = (IService<T>) service.service;
            }
        }

        if (candidate == null)
            throw new IllegalStateException("No services of given type (" + klass.getName() + ") were registered.");

        return candidate;
    }

    /**
     * Instantiates a service and returns its instance.
     *
     * @param klass           Type of service to instantiate.
     * @param serviceProvider Provider to resolve any additional services from.
     * @param <T>             Type of service to instantiate.
     * @return Instantiated service instance.
     */
    public static <T> T instantiateService(Class<T> klass, ServiceProvider serviceProvider) {
        Constructor<T>[] ctors = (Constructor<T>[]) klass.getDeclaredConstructors();
        if (ctors.length != 1)
            throw new IllegalStateException("Invalid service type supplied.");

        Constructor<T> ctor = ctors[0];
        Class<?>[] types = ctor.getParameterTypes();
        Object[] services = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            Class<?> type = types[i];
            IService<?> srv = serviceProvider.resolveService(type);
            services[i] = srv.getInstance(serviceProvider);
        }

        try {
            ctor.setAccessible(true);
            return ctor.newInstance(services);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Creates a new service provider builder.
     *
     * @return Service provider builder.
     * @see Builder
     */
    public static Builder createBuilder() {
        return new Builder();
    }

    /**
     * Builds instances of {@link ServiceProvider}.
     */
    public static class Builder {

        private final ArrayList<ServiceInfo> descriptors = new ArrayList<>();
        private final HashSet<Class<?>> registeredTypes = new HashSet<>();

        private Builder() {
        }

        /**
         * Registers a singleton service.
         *
         * @param klass Type of service to register.
         * @param <T>   Type of service to register.
         */
        public <T> void registerSingleton(Class<T> klass) {
            if (this.registeredTypes.contains(klass))
                throw new IllegalStateException("Cannot register a service more than once.");

            this.registeredTypes.add(klass);
            this.descriptors.add(new ServiceInfo(klass, new SingletonService<>(klass)));
        }

        /**
         * Registers a pre-constructed singleton service.
         *
         * @param instance Singleton instance to register.
         * @param klass    Type of service to register.
         * @param <T>      Type of service to register.
         */
        public <T> void registerSingleton(T instance, Class<T> klass) {
            if (this.registeredTypes.contains(klass))
                throw new IllegalStateException("Cannot register a service more than once.");

            this.registeredTypes.add(klass);
            this.descriptors.add(new ServiceInfo(klass, new SingletonService<>(instance, klass)));
        }

        /**
         * Registers a transient service.
         *
         * @param klass Type of service to register.
         * @param <T>   Type of service to register.
         */
        public <T> void registerTransient(Class<T> klass) {
            if (this.registeredTypes.contains(klass))
                throw new IllegalStateException("Cannot register a service more than once.");

            this.registeredTypes.add(klass);
            this.descriptors.add(new ServiceInfo(klass, new TransientService<>(klass)));
        }

        /**
         * Finalizes this builder.
         *
         * @return Constructed {@link ServiceProvider}.
         */
        public ServiceProvider build() {
            return new ServiceProvider(new ArrayList<>(descriptors));
        }
    }

    private static class ServiceInfo {
        public final Class<?> klass;
        public final IService<?> service;

        public ServiceInfo(Class<?> klass, IService<?> service) {
            this.klass = klass;
            this.service = service;
        }
    }
}
