package elkaproj.httpserver.services;

import java.nio.charset.StandardCharsets;

/**
 * Test service.
 */
@Service(kind = ServiceKind.SINGLETON)
public class TestService {
    private static final byte[] BYTES = "OK!".getBytes(StandardCharsets.UTF_8);

    /**
     * Gets bytes for OK! string.
     *
     * @return Bytes for OK! string.
     */
    public byte[] getOkBytes() {
        return BYTES;
    }

    /**
     * Gets the length of array returned by {@link #getOkBytes()}.
     *
     * @return Length of array returned by {@link #getOkBytes()}.
     */
    public int getOkBytesLength() {
        return BYTES.length;
    }
}
