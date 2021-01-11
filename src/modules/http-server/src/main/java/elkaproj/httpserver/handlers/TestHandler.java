package elkaproj.httpserver.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Handles /test.
 */
@Handler("/test")
public class TestHandler implements HttpHandler {

    private static final byte[] BYTES = "OK!".getBytes(StandardCharsets.UTF_8);

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        httpExchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        httpExchange.sendResponseHeaders(200, BYTES.length);
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(BYTES);
        }
    }
}
