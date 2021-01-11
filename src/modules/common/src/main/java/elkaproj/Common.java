package elkaproj;

/**
 * Contains common properties.
 */
public class Common {

    /**
     * User agent for HTTP requests.
     */
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36 Edg/87.0.664.66";

    /**
     * Appends paths.
     *
     * @param base     Base path to append to.
     * @param appendix Path to append.
     * @return Concatenated path.
     */
    public static String appendPath(String base, String appendix) {
        if (appendix.startsWith("/"))
            appendix = appendix.substring(1);

        if (base.endsWith("/"))
            return base + appendix;
        else
            return base + "/" + appendix;
    }
}
