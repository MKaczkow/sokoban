package elkaproj.config.language;

import java.util.Map;

/**
 * Represents a language, as loaded from a strings.*.txt file.
 */
public class Language {

    @LanguageData
    private Map<String, LanguageSection> sections;

    @LanguageDirective("name")
    private String name;

    private Language() {
        this.sections = null;
        this.name = null;
    }

    /**
     * Gets the display name of the language.
     * @return Display name of the language.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets a section by name,
     * @param name Name of the section to retrieve.
     * @return Retrieved section.
     */
    public LanguageSection getSection(String name) {
        if (name == null || name.equals(""))
            throw new IllegalArgumentException("Name cannot be null or empty.");

        if (name.contains("."))
            throw new IllegalArgumentException("Name must be a single token.");

        if (!this.sections.containsKey(name))
            throw new IllegalArgumentException("Specified section (" + name + ") does not exist.");

        return this.sections.get(name);
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
        if (nextTokenIdx < 0)
            throw new IllegalArgumentException("Path must have at least 2 tokens.");

        String name = path.substring(0, nextTokenIdx);
        LanguageSection section = this.getSection(name);

        return section.getValue(path.substring(nextTokenIdx + 1));
    }
}
