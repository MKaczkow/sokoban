package elkaproj.config.language;

import elkaproj.DebugWriter;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Parses language files.
 */
public class LanguageParser implements Closeable {

    private static final Map<String, LanguageDirectiveModel> directives;
    private static final Field sectionTarget;

    private final BufferedReader br;

    static {
        LanguageTypeModel ltm = buildTypeModel();
        directives = ltm.directives;
        sectionTarget = ltm.sectionTarget;
    }

    public LanguageParser(File langFile) {
        FileReader fr;
        try {
            fr = new FileReader(langFile);
        } catch (Exception ignored) {
            this.br = null;
            return;
        }

        this.br = new BufferedReader(fr);
    }

    public Language parse() throws IOException, IllegalArgumentException, IllegalStateException {
        if (this.br == null)
            return null;

        Language lang;
        try {
            Constructor<Language> ctor = Language.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            lang = ctor.newInstance();
        } catch (Exception ex) {
            DebugWriter.INSTANCE.logError("LANG-PRSR", ex, "Failed to instantiate language.");
            return null;
        }

        HashMap<String, SectionModel> sections = new HashMap<>();
        SectionModel current = null;

        int lineNo = 1;
        for (String line; (line = this.br.readLine()) != null; lineNo++) {
            String sline = line.trim();

            if (sline.equals(""))
                continue;

            if (sline.startsWith("#"))
                continue;

            if (sline.startsWith("%")) {
                if (current != null)
                    throw new IllegalStateException("Cannot place directive after any section has started. Line " + lineNo);

                this.parseDirective(lang, sline);
                continue;
            }

            if (sline.startsWith("[")) { // section
                String name = sline.substring(1, sline.length() - 1);
                if (name.startsWith(".") && current == null)
                    throw new IllegalStateException("Cannot place subsection outside of a section. Line " + lineNo);

                if (!name.startsWith(".")) {
                    current = SectionModel.createRootSection(name);
                    sections.put(current.name, current);
                } else {
                    assert current != null;

                    int dots = this.countDots(name);
                    if (dots == 1)
                        current = current.createSection(name.substring(1));
                    else if (dots % 2 != 0)
                        throw new IllegalArgumentException("Section relative name needs to have 1 or even amount of dots before the name. Line " + lineNo);
                    else {
                        int levels = dots / 2;
                        while (levels > 0) {
                            if (current == null)
                                throw new IllegalStateException("Cannot place subsection outside of a section. Line " + lineNo);

                            current = current.parent;
                            levels--;
                        }

                        current = current.createSection(name.substring(dots));
                    }
                }

                continue;
            }

            if (current == null)
                throw new IllegalStateException("Cannot place value outside of a section. Line " + lineNo);

            this.parseValue(current, sline);
        }

        try {
            sectionTarget.set(lang, sections.values()
                    .stream()
                    .map(sectionModel -> sectionModel.toSection(null))
                    .collect(Collectors.toMap(LanguageSection::getName, x -> x)));
        } catch (Exception ignored) {
        }

        return lang;
    }

    private void parseDirective(Language target, String directiveLine) {
        String name = directiveLine.substring(1).trim();

        int t = name.indexOf(' ');
        String value = name.substring(t + 1).trim();
        name = name.substring(0, t);

        if (!directives.containsKey(name))
            throw new IllegalArgumentException("Invalid directive '" + name + "'.");

        LanguageDirectiveModel ldm = directives.get(name);
        try {
            ldm.field.set(target, value);
        } catch (IllegalAccessException ignored) {
        }
    }

    private void parseValue(SectionModel target, String valueLine) {
        int t = valueLine.indexOf('=');

        String name = valueLine.substring(0, t).trim();
        String value = valueLine.substring(t + 1).trim();

        target.putValue(name, value);
    }

    private int countDots(String s) {
        for (int i = 0; i < s.length(); i++)
            if (s.charAt(i) != '.')
                return i;

        return s.length();
    }

    @Override
    public void close() throws IOException {
        if (this.br != null)
            this.br.close();
    }

    private static LanguageTypeModel buildTypeModel() {
        LanguageTypeModel ltm = new LanguageTypeModel();
        HashMap<String, LanguageDirectiveModel> lds = new HashMap<>();
        ltm.directives = lds;

        for (Field f : Language.class.getDeclaredFields()) {
            if (f.getAnnotation(LanguageData.class) != null) {
                f.setAccessible(true);
                ltm.sectionTarget = f;
                continue;
            }

            LanguageDirective ld = f.getAnnotation(LanguageDirective.class);
            if (ld == null)
                continue;

            f.setAccessible(true);
            LanguageDirectiveModel ldm = new LanguageDirectiveModel();
            ldm.name = ld.value();
            ldm.field = f;
            lds.put(ldm.name, ldm);
        }

        return ltm;
    }

    private static class LanguageDirectiveModel {
        public String name;
        public Field field;
    }

    private static class LanguageTypeModel {
        public Map<String, LanguageDirectiveModel> directives;
        public Field sectionTarget;
    }

    private static class SectionModel {
        public String name;
        public Map<String, String> values = new HashMap<>();
        public Map<String, SectionModel> sections = new HashMap<>();
        public SectionModel parent;

        public void putValue(String name, String value) {
            if (!this.sections.isEmpty())
                throw new IllegalStateException("Cannot insert value after any subsections are inserted.");

            this.values.put(name, value);
        }

        public SectionModel createSection(String name) {
            SectionModel sm = new SectionModel();
            sm.name = name;
            sm.parent = this;
            this.sections.put(name, sm);
            return sm;
        }

        public LanguageSection toSection(LanguageSection parent) {
            HashMap<String, LanguageSection> tmp = new HashMap<>();
            LanguageSection ls = new LanguageSection(this.name, this.values, tmp, parent);
            for (Map.Entry<String, SectionModel> sme : sections.entrySet()) {
                tmp.put(sme.getKey(), sme.getValue().toSection(ls));
            }

            return ls;
        }

        public static SectionModel createRootSection(String name) {
            SectionModel sm = new SectionModel();
            sm.name = name;
            sm.parent = null;
            return sm;
        }
    }
}
