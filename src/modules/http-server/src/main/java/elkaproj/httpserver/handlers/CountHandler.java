package elkaproj.httpserver.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import elkaproj.httpserver.ServiceProvider;
import elkaproj.httpserver.services.*;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

/**
 * Returns the total number of rows in the scoreboard database.
 */
@Handler("/count")
public class CountHandler implements HttpHandler {

    private final ServiceProvider serviceProvider;
    private final IService<PostgresHandler> postgresHandlerService;
    private final IService<ByteEncoderService> byteEncoderService;
    private final IService<ErrorHandlerService> errorHandlerService;

    private CountHandler(
            ServiceProvider serviceProvider,
            @Inject(PostgresHandler.class) IService<PostgresHandler> postgresHandlerService,
            @Inject(ByteEncoderService.class) IService<ByteEncoderService> byteEncoderService,
            @Inject(ErrorHandlerService.class) IService<ErrorHandlerService> errorHandlerService) {
        this.serviceProvider = serviceProvider;
        this.postgresHandlerService = postgresHandlerService;
        this.byteEncoderService = byteEncoderService;
        this.errorHandlerService = errorHandlerService;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        if (!httpExchange.getRequestMethod().equals("GET")) {
            this.errorHandlerService.getInstance(this.serviceProvider).write400(httpExchange);
            return;
        }

        PostgresHandler pgh = this.postgresHandlerService.getInstance(this.serviceProvider);
        int count = -1;
        try {
            pgh.open();
            count = pgh.getTotalCount();
            pgh.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        ByteEncoderService bes = this.byteEncoderService.getInstance(this.serviceProvider);
        byte[] bytes = bes.encode(String.valueOf(count));

        httpExchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        httpExchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
