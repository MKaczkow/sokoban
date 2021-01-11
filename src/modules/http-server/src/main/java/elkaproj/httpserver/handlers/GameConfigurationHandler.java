package elkaproj.httpserver.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import elkaproj.DebugWriter;
import elkaproj.config.IConfiguration;
import elkaproj.config.IConfigurationLoader;
import elkaproj.httpserver.ServiceProvider;
import elkaproj.httpserver.services.ErrorHandlerService;
import elkaproj.httpserver.services.IService;
import elkaproj.httpserver.services.Inject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Sends game configuration data to the client.
 */
@Handler("/configuration")
public class GameConfigurationHandler implements HttpHandler {

    private final ServiceProvider serviceProvider;
    private final IService<IConfigurationLoader> configurationLoaderService;
    private final IService<ErrorHandlerService> errorHandlerService;

    private GameConfigurationHandler(
            ServiceProvider serviceProvider,
            @Inject(IConfigurationLoader.class) IService<IConfigurationLoader> configurationLoaderService,
            @Inject(ErrorHandlerService.class) IService<ErrorHandlerService> errorHandlerService) {
        this.serviceProvider = serviceProvider;
        this.configurationLoaderService = configurationLoaderService;
        this.errorHandlerService = errorHandlerService;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        if (!httpExchange.getRequestMethod().equals("GET")) {
            this.errorHandlerService.getInstance(this.serviceProvider).write400(httpExchange);
            return;
        }

        IConfigurationLoader cfgLoader = this.configurationLoaderService.getInstance(this.serviceProvider);
        IConfiguration cfg = cfgLoader.load();

        httpExchange.getResponseHeaders().add("Content-Type", "application/xml; charset=utf-8");

        try {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                cfg.serialize(baos);

                try (OutputStream os = httpExchange.getResponseBody()) {
                    httpExchange.sendResponseHeaders(200, baos.size());
                    os.write(baos.toByteArray());
                }
            }
        } catch (Exception ex) {
            DebugWriter.INSTANCE.logError("LDR-FILE", ex, "Error while sending configuration.");
            this.errorHandlerService.getInstance(this.serviceProvider).write500(httpExchange);
        }

        DebugWriter.INSTANCE.logMessage("GDAT-CFG", "Sent configuration");
    }
}
