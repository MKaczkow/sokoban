package elkaproj.config.language;

import elkaproj.Union;

import java.util.Map;

/**
 * Represents a section of a language definition.
 */
public class LanguageSection {

    private final String name;
    private final Map<String, String> values;
    private final Map<String, LanguageSection> sections;
    private final LanguageSection parent;

    /**
     * Creates a new language section with specified data.
     * @param name Name of this section.
     * @param values Values placed directly in this section.
     * @param sections Subsections of this section.
     * @param parent Parent section of this section. Null if top-level section.
     */
    public LanguageSection(String name, Map<String, String> values, Map<String, LanguageSection> sections, LanguageSection parent) {
        this.name = name;
        this.values = values;
        this.sections = sections;
        this.parent = parent;
    }

    /**
     * Gets the name of this section.
     * @return Name of this section.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets a direct child of this section. Can be a section or value.
     * @param name Name of section or value to get.
     * @return Value/Section union containing retrieved child.
     */
    public Union<String, LanguageSection> get(String name) {
        if (name == null || name.equals(""))
            throw new IllegalArgumentException("Name cannot be null or empty.");

        if (name.contains("."))
            throw new IllegalArgumentException("Name must be a single token.");

        if (this.sections.containsKey(name))
            return new Union<>(String.class, LanguageSection.class, this.sections.get(name));

        if (this.values.containsKey(name))
            return new Union<>(String.class, LanguageSection.class, this.values.get(name));

        throw new IllegalArgumentException("Given name (" + name + ") is not present in this section.");
    }

    /**
     * Gets a value at specified path. Can be multi-level path.
     * @param path Path of the item to retrieve.
     * @return Retrieved value.
     */
    public String getValue(String path) {
        if (path == null || path.equals("") || path.startsWith("."))
            throw new IllegalArgumentException("Path cannot be null or empty.");

        int nextTokenIdx = path.indexOf('.');
        boolean hasMorePath = nextTokenIdx > 0;
        String name = nextTokenIdx < 0 ? path : path.substring(0, nextTokenIdx);
        Union<String, LanguageSection> next = this.get(name);

        if (hasMorePath && next.isType1() /* string */)
            throw new IllegalArgumentException("Specified path (" + path + ") does not exist.");

        if (!hasMorePath && next.isType2() /* section */)
            throw new IllegalArgumentException("Specified path (" + path + ") leads to a section.");

        try {
            if (next.isType1())
                return next.unwrapType1();
            else
                return next.unwrapType2().getValue(path.substring(nextTokenIdx + 1));
        } catch (Exception ignored) {
            return ""; // will never happen
        }
    }

    /**
     * Gets the parent section of this section.
     * @return Parent section or null if this is top-level section.
     */
    public LanguageSection getParent() {
        return this.parent;
    }
}
