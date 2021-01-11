package elkaproj.httpserver.services;

import java.util.HashMap;
import java.util.Map;

/**
 * Parses endpoints into route argument maps.
 */
@Service(kind = ServiceKind.TRANSIENT)
public class EndpointParserService {

    public EndpointParserService() {
    }

    /**
     * Parses path according to a template.
     *
     * @param path     Path to parse.
     * @param template Template to parse according to.
     * @return Map of argument name -&gt; argument value.
     */
    public Map<String, String> parse(String path, String template) {
        if (template.startsWith("/"))
            template = template.substring(1);

        if (path.startsWith("/"))
            path = path.substring(1);

        int ppos = 0;
        HashMap<String, String> args = new HashMap<>();
        String[] templateParts = template.split("/");
        for (String templatePart : templateParts) {
            String part;
            if (!templatePart.startsWith(":")) { // constant element
                int end = path.indexOf("/", ppos);
                part = end < 0 ? path.substring(ppos) : path.substring(ppos, end);
                ppos = end < 0 ? path.length() : end + 1;

                if (!part.equals(templatePart))
                    return null;

                continue;
            }

            if (templatePart.endsWith("...")) { // catch-all
                part = path.substring(ppos);
                ppos = path.length();

                args.put(templatePart.substring(1, templatePart.length() - 3), part);
                break;
            }

            if (templatePart.endsWith("?")) { // optional
                templatePart = templatePart.substring(0, templatePart.length() - 1);

                if (ppos >= path.length())
                    break;
            }

            if (ppos >= path.length())
                return null;

            templatePart = templatePart.substring(1);
            int end = path.indexOf("/", ppos);
            part = end < 0 ? path.substring(ppos) : path.substring(ppos, end);
            args.put(templatePart, part);
            ppos = end < 0 ? path.length() : end + 1;
        }

        if (ppos < path.length())
            return null;

        return args;
    }
}
