package elkaproj.httpserver;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import elkaproj.DebugWriter;
import elkaproj.Inspector;
import elkaproj.config.commandline.CommandLineParser;
import elkaproj.httpserver.handlers.Handler;
import elkaproj.httpserver.services.IService;
import elkaproj.httpserver.services.Inject;
import elkaproj.httpserver.services.Service;
import elkaproj.httpserver.services.ServiceKind;
import org.reflections.Reflections;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Entrypoint for the server.
 */
public class Entry {

    public static void main(String[] args) {
        // parse commandline options
        CommandLineParser<CommandLineOptions> clp = new CommandLineParser<>(CommandLineOptions.class);
        CommandLineOptions opts = clp.parse(args);

        // print help and quit if requested
        if (opts.isHelp()) {
            clp.printHelp(System.out);
            return;
        }

        // enable debug, if applicable, and inspect options
        Inspector inspector = Inspector.INSTANCE;
        if (opts.isDebug()) {
            DebugWriter.setEnabled(true);
            DebugWriter.INSTANCE.logMessage("INIT", "Application initializing...");
            inspector.inspect(opts);
        }

        // create service provider
        ServiceProvider.Builder serviceProviderBuilder = registerAutoServices();
        ServiceProvider serviceProvider = serviceProviderBuilder.build();

        // start http
        InetSocketAddress addr = new InetSocketAddress(opts.getBindAddress(), opts.getPort());
        HttpServer http;
        try {
            http = HttpServer.create(addr, 0);
        } catch (IOException ex) {
            DebugWriter.INSTANCE.logError("INIT", ex, "Couldn't initialize HTTP.");
            return;
        }

        // here register all modules
        createContexts(http, serviceProvider);

        // add shutdown hook
        HttpServer finalHttp = http;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DebugWriter.INSTANCE.logMessage("HTTP", "Shutting down...");
            finalHttp.stop(0);
        }));

        // start http
        http.setExecutor(null);
        http.start();
    }

    private static void createContexts(HttpServer http, ServiceProvider serviceProvider) {
        Reflections r = new Reflections(Entry.class.getPackage().getName());
        ArrayList<HandlerInfo> handlers = new ArrayList<>();

        for (Class<?> klass : r.getTypesAnnotatedWith(Handler.class)) {
            if (klass.isInterface() || klass.isAnnotation()) {
                DebugWriter.INSTANCE.logMessage("HTTP-HDLR", "Type %s is not eligible for handler type (iface/annotation)");
                continue;
            }

            if (!HttpHandler.class.isAssignableFrom(klass)) {
                DebugWriter.INSTANCE.logMessage("HTTP-HDLR", "Type %s is not eligible for handler type (not HttpHandler)");
                continue;
            }

            Handler h = klass.getAnnotation(Handler.class);
            if (h == null) {
                DebugWriter.INSTANCE.logMessage("HTTP-HDLR", "Type %s is not eligible for handler type (missing annotation)");
                continue;
            }

            String path = h.value();

            Constructor<?>[] ctors = klass.getDeclaredConstructors();
            if (ctors.length != 1) {
                DebugWriter.INSTANCE.logMessage("HTTP-HDLR", "Type %s is not eligible for handler type (multiple ctors)");
                continue;
            }

            Constructor<? extends HttpHandler> ctor = (Constructor<? extends HttpHandler>) ctors[0];

            HttpHandler handler;
            try {
                Object[] args = null;
                if (ctor.getParameterCount() > 0) {
                    Parameter[] params = ctor.getParameters();
                    if (params[0].getType() != ServiceProvider.class) {
                        DebugWriter.INSTANCE.logMessage("HTTP-HDLR", "Type %s is not eligible for handler type (non-service provider arg)");
                        continue;
                    }

                    args = new Object[params.length];
                    args[0] = serviceProvider;
                    for (int i = 1; i < params.length; i++) {
                        Parameter param = params[i];
                        if (param.getType() != IService.class) {
                            DebugWriter.INSTANCE.logMessage("HTTP-HDLR", "Type %s is not eligible for handler type (non-service arg)");
                            continue;
                        }

                        Inject inject = param.getAnnotation(Inject.class);
                        if (inject == null) {
                            DebugWriter.INSTANCE.logMessage("HTTP-HDLR", "Type %s is not eligible for handler type (non-inject service)");
                            continue;
                        }

                        args[i] = serviceProvider.resolveService(inject.value());
                    }
                }

                handler = ctor.newInstance(args);
            } catch (Exception ex) {
                DebugWriter.INSTANCE.logError("HTTP-HDLR", ex, "Failed to instantiate handler of type %s", klass.getName());
                continue;
            }

            handlers.add(new HandlerInfo(path, handler));
        }

        handlers.sort(new HandlerInfo.Sorter());
        for (HandlerInfo handler : handlers) {
            DebugWriter.INSTANCE.logMessage("HTTP-HDLR", "Registered handler %s for path %s", handler.instance.getClass().getName(), handler.path);
            http.createContext(handler.path, handler.instance);
        }
    }

    private static ServiceProvider.Builder registerAutoServices() {
        Reflections r = new Reflections(Entry.class.getPackage().getName());
        ServiceProvider.Builder providerBuilder = ServiceProvider.createBuilder();

        for (Class<?> klass : r.getTypesAnnotatedWith(Service.class)) {
            if (klass.isInterface() || klass.isAnnotation()) {
                DebugWriter.INSTANCE.logMessage("HTTP-SRV", "Type %s is not eligible for service type (iface/annotation)");
                continue;
            }

            Service srv = klass.getAnnotation(Service.class);
            if (srv == null) {
                DebugWriter.INSTANCE.logMessage("HTTP-SRV", "Type %s is not eligible for service type (missing annotation)");
                continue;
            }

            ServiceKind kind = srv.kind();
            Class<?> srvKlass = srv.type();
            if (srvKlass == Object.class) {
                srvKlass = klass;
            } else if (!srvKlass.isAssignableFrom(klass)) {
                DebugWriter.INSTANCE.logMessage("HTTP-SRV", "Type %s is not eligible for service type (not assignable to its type)");
                continue;
            }

            switch (kind) {
                case SINGLETON:
                    providerBuilder.registerSingleton(srvKlass);
                    break;

                case TRANSIENT:
                    providerBuilder.registerTransient(srvKlass);
                    break;
            }
        }

        return providerBuilder;
    }

    private static class HandlerInfo {
        public final String path;
        public final HttpHandler instance;

        public HandlerInfo(String path, HttpHandler instance) {
            this.path = path;
            this.instance = instance;
        }

        private static class Sorter implements Comparator<HandlerInfo> {

            @Override
            public int compare(HandlerInfo h1, HandlerInfo h2) {
                return -(h1.path.length() - h2.path.length());
            }
        }
    }
}
