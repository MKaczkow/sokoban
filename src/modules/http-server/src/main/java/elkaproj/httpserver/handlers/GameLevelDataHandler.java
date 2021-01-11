package elkaproj.httpserver.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import elkaproj.DebugWriter;
import elkaproj.config.*;
import elkaproj.httpserver.ServiceProvider;
import elkaproj.httpserver.services.EndpointParserService;
import elkaproj.httpserver.services.ErrorHandlerService;
import elkaproj.httpserver.services.IService;
import elkaproj.httpserver.services.Inject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Sends level data to clients.
 */
@Handler("/levels/data")
public class GameLevelDataHandler implements HttpHandler {

    private static final String ROUTE_TEMPLATE = "/levels/data/:id/:level";

    private final ServiceProvider serviceProvider;
    private final IService<IConfigurationLoader> configurationLoaderService;
    private final IService<EndpointParserService> endpointParserService;
    private final IService<ErrorHandlerService> errorHandlerService;

    private GameLevelDataHandler(
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
        EndpointParserService eps = this.endpointParserService.getInstance(this.serviceProvider);
        Map<String, String> args = eps.parse(httpExchange.getRequestURI().getPath(), ROUTE_TEMPLATE);
        if (args == null || !args.containsKey("id") || !args.containsKey("level")) {
            this.errorHandlerService.getInstance(this.serviceProvider).write404(httpExchange);
            return;
        }

        IConfigurationLoader cfgLoader = this.configurationLoaderService.getInstance(this.serviceProvider);
        ILevelPackLoader levelLoader = cfgLoader.getLevelPackLoader();
        ILevelPack levelPack;
        int levelId;
        try {
            levelPack = levelLoader.loadPack(args.get("id"));
            levelId = Integer.parseInt(args.get("level"));
        } catch (Exception ex) {
            DebugWriter.INSTANCE.logError("LDR-FILE", ex, "Error while loading level pack.");
            this.errorHandlerService.getInstance(this.serviceProvider).write404(httpExchange);
            return;
        }

        try {
            for (ILevel level : levelPack) {
                if (level.getOrdinal() != levelId)
                    continue;

                LevelTile[][] tiles = level.getTiles();
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    try (OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
                        for (LevelTile[] tile : tiles) {
                            for (LevelTile levelTile : tile) {
                                osw.write(levelTile.getRepresentation());
                            }

                            osw.write('\n');
                        }
                    }

                    try (OutputStream os = httpExchange.getResponseBody()) {
                        httpExchange.sendResponseHeaders(200, baos.size());
                        os.write(baos.toByteArray());
                    }
                }
            }
        } catch (Exception ex) {
            DebugWriter.INSTANCE.logError("LDR-FILE", ex, "Error while sending level data.");
            this.errorHandlerService.getInstance(this.serviceProvider).write500(httpExchange);
        }

        this.errorHandlerService.getInstance(this.serviceProvider).write404(httpExchange);
    }
}
