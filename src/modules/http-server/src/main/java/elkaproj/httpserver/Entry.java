package elkaproj.httpserver;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import elkaproj.DebugWriter;
import elkaproj.Inspector;
import elkaproj.config.commandline.CommandLineParser;
import elkaproj.httpserver.handlers.Handler;
import org.reflections.Reflections;

import java.io.IOException;
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

        // start http
        InetSocketAddress addr = new InetSocketAddress(opts.getBindAddress(), opts.getPort());
        HttpServer http = null;
        try {
            http = HttpServer.create(addr, 0);
        } catch (IOException ex) {
            DebugWriter.INSTANCE.logError("INIT", ex, "Couldn't initialize HTTP.");
            return;
        }

        // here register all modules
        createContexts(http);

        // add shutdown hook
        HttpServer finalHttp = http;
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                finalHttp.stop(0);
            }
        }));

        // start http
        http.setExecutor(null);
        http.start();
    }

    private static void createContexts(HttpServer http) {
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
            HttpHandler handler;
            try {
                handler = (HttpHandler) klass.getDeclaredConstructor().newInstance();
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
