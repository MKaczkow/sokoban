package elkaproj.httpserver.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import elkaproj.httpserver.ServiceProvider;
import elkaproj.httpserver.services.IService;
import elkaproj.httpserver.services.Inject;
import elkaproj.httpserver.services.TestService;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Handles /test.
 */
@Handler("/test")
public class TestHandler implements HttpHandler {

    private final ServiceProvider serviceProvider;
    private final IService<TestService> testService;

    public TestHandler(
            ServiceProvider serviceProvider,
            @Inject(TestService.class) IService<TestService> testService) {
        this.serviceProvider = serviceProvider;
        this.testService = testService;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        TestService srv = this.testService.getInstance(this.serviceProvider);

        httpExchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        httpExchange.sendResponseHeaders(200, srv.getOkBytesLength());
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(srv.getOkBytes());
        }
    }
}
