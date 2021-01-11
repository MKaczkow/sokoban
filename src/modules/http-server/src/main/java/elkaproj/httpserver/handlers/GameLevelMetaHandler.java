package elkaproj.httpserver.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import elkaproj.DebugWriter;
import elkaproj.config.IConfigurationLoader;
import elkaproj.config.ILevelPack;
import elkaproj.config.ILevelPackLoader;
import elkaproj.httpserver.ServiceProvider;
import elkaproj.httpserver.services.EndpointParserService;
import elkaproj.httpserver.services.ErrorHandlerService;
import elkaproj.httpserver.services.IService;
import elkaproj.httpserver.services.Inject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Sends level pack metadata to the client.
 */
@Handler("/levels/meta")
public class GameLevelMetaHandler implements HttpHandler {

    private static final String ROUTE_TEMPLATE = "/levels/meta/:id";

    private final ServiceProvider serviceProvider;
    private final IService<IConfigurationLoader> configurationLoaderService;
    private final IService<EndpointParserService> endpointParserService;
    private final IService<ErrorHandlerService> errorHandlerService;

    private GameLevelMetaHandler(
            ServiceProvider serviceProvider,
            @Inject(IConfigurationLoader.class) IService<IConfigurationLoader> configurationLoaderService,
            @Inject(EndpointParserService.class) IService<EndpointParserService> endpointParserService,
            @Inject(ErrorHandlerService.class) IService<ErrorHandlerService> errorHandlerService) {
        this.serviceProvider = serviceProvider;
        this.configurationLoaderService = configurationLoaderService;
        this.endpointParserService = endpointParserService;
        this.errorHandlerService = errorHandlerService;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        if (!httpExchange.getRequestMethod().equals("GET")) {
            this.errorHandlerService.getInstance(this.serviceProvider).write400(httpExchange);
            return;
        }

        EndpointParserService eps = this.endpointParserService.getInstance(this.serviceProvider);
        Map<String, String> args = eps.parse(httpExchange.getRequestURI().getPath(), ROUTE_TEMPLATE);
        if (args == null || !args.containsKey("id")) {
            this.errorHandlerService.getInstance(this.serviceProvider).write404(httpExchange);
            return;
        }

        IConfigurationLoader cfgLoader = this.configurationLoaderService.getInstance(this.serviceProvider);
        ILevelPackLoader levelLoader = cfgLoader.getLevelPackLoader();
        ILevelPack levelPack;
        try {
            levelPack = levelLoader.loadPack(args.get("id"));
        } catch (Exception ex) {
            DebugWriter.INSTANCE.logError("LDR-FILE", ex, "Error while loading level pack.");
            this.errorHandlerService.getInstance(this.serviceProvider).write404(httpExchange);
            return;
        }

        httpExchange.getResponseHeaders().add("Content-Type", "application/xml; charset=utf-8");

        try {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                levelPack.serialize(baos);

                try (OutputStream os = httpExchange.getResponseBody()) {
                    httpExchange.sendResponseHeaders(200, baos.size());
                    os.write(baos.toByteArray());
                }
            }
        } catch (Exception ex) {
            DebugWriter.INSTANCE.logError("LDR-FILE", ex, "Error while sending level pack.");
            this.errorHandlerService.getInstance(this.serviceProvider).write500(httpExchange);
        }
    }
}
