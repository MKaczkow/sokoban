package elkaproj.httpserver.services;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Handles writing errors.
 */
@Service(kind = ServiceKind.SINGLETON)
public class ErrorHandlerService {

    private final ByteEncoderService byteEncoderService;

    private ErrorHandlerService(ByteEncoderService byteEncoderService) {
        this.byteEncoderService = byteEncoderService;
    }

    /**
     * Writes a 500 error.
     *
     * @param t Context.
     * @throws IOException Exception occured during writing.
     */
    public void write500(HttpExchange t) throws IOException {
        byte[] bytes = this.byteEncoderService.encode("Internal server error");

        t.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        t.sendResponseHeaders(500, bytes.length);
        try (OutputStream os = t.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Writes a 404 error.
     *
     * @param t Context.
     * @throws IOException Exception occured during writing.
     */
    public void write404(HttpExchange t) throws IOException {
        byte[] bytes = this.byteEncoderService.encode("Not found");

        t.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        t.sendResponseHeaders(404, bytes.length);
        try (OutputStream os = t.getResponseBody()) {
            os.write(bytes);
        }
    }
}
