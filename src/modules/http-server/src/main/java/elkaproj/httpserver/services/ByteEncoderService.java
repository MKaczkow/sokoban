package elkaproj.httpserver.services;

import java.nio.charset.StandardCharsets;

/**
 * Handles common encoding.
 */
@Service(kind = ServiceKind.SINGLETON)
public class ByteEncoderService {

    private ByteEncoderService() {
    }

    /**
     * Encodes given string to bytes.
     *
     * @param data String to encode.
     * @return Encoded string.
     */
    public byte[] encode(String data) {
        return data.getBytes(StandardCharsets.UTF_8);
    }
}
