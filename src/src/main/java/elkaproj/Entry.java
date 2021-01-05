package elkaproj;

import elkaproj.config.ConfigurationValidator;
import elkaproj.config.IConfiguration;
import elkaproj.config.ILevelPack;
import elkaproj.config.ILevelPackLoader;
import elkaproj.config.commandline.CommandLineOptions;
import elkaproj.config.commandline.CommandLineParser;
import elkaproj.config.impl.FileConfigurationLoader;
import elkaproj.config.language.Language;
import elkaproj.config.language.LanguageLoader;
import elkaproj.ui.GameFrame;
import sun.security.ssl.Debug;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Enumeration;

public class Entry {
    public static void main(String[] args) {
        // parse commandline options
        CommandLineParser<CommandLineOptions> clp = new CommandLineParser<>(CommandLineOptions.class);
        CommandLineOptions opts = clp.parse(args);

        // print help and quit if requested
        if (opts.isHelp()) {
            clp.printHelp(System.out);
            return;
        }

        // enable debug, if applicable, and inspect options
        Inspector inspector = Inspector.INSTANCE;
        if (opts.isDebug()) {
            DebugWriter.setEnabled(true);
            DebugWriter.INSTANCE.logMessage("INIT", "Application initializing...");
            inspector.inspect(opts);
        }

        // load configuration
        // TODO: networked load if applicable
        // TODO: delegate to UI?
        File f = new File("config", "config.xml");
        IConfiguration config = null;
        ILevelPack levelPack = null;
        try (FileConfigurationLoader loader = new FileConfigurationLoader(f)) {
            config = loader.load();

            DebugWriter.INSTANCE.logMessage("INIT", "Configuration valid? %b", ConfigurationValidator.validateConfiguration(config));

            try (ILevelPackLoader lvlloader = loader.getLevelPackLoader()) {
                levelPack = lvlloader.loadPack(config.getLevelPackId());
            }
        } catch (IOException e) {
            DebugWriter.INSTANCE.logError("INIT", e, "Failed to load configuration.");
            System.exit(1);
        }

        // inspect config
        inspector.inspect(config);
        inspector.inspect(levelPack);

        // load languages
        LanguageLoader languageLoader = new LanguageLoader();
        Language uiLang = null;
        try {
            uiLang = languageLoader.loadLanguage(opts.getLanguage());
        } catch (Exception ex) {
            DebugWriter.INSTANCE.logError("INIT", ex, "Failed to load language '%s'.", opts.getLanguage());
            System.exit(2);
        }

        if (uiLang == null) {
            DebugWriter.INSTANCE.logError("INIT", null, "Failed to load language '%s'.", opts.getLanguage());
            System.exit(2);
        }

        DebugWriter.INSTANCE.logMessage("INIT", "Loaded language: %s", uiLang.getName());

        // fire up the UI
        loadPlexFont();
        GameFrame mainframe = new GameFrame(uiLang);
        mainframe.setVisible(true);
    }

    private static void loadPlexFont() {
        try {
            File plexFR = new File(Entry.class.getResource("/fonts/IBMPlexSans-Regular.ttf").toURI()),
                    plexFI = new File(Entry.class.getResource("/fonts/IBMPlexSans-Italic.ttf").toURI()),
                    plexFB = new File(Entry.class.getResource("/fonts/IBMPlexSans-Bold.ttf").toURI()),
                    plexFBI = new File(Entry.class.getResource("/fonts/IBMPlexSans-BoldItalic.ttf").toURI());

            Font plexR = Font.createFont(Font.TRUETYPE_FONT, plexFR),
                    plexI = Font.createFont(Font.TRUETYPE_FONT, plexFI),
                    plexB = Font.createFont(Font.TRUETYPE_FONT, plexFB),
                    plexBI = Font.createFont(Font.TRUETYPE_FONT, plexFBI);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(plexR);
            ge.registerFont(plexI);
            ge.registerFont(plexB);
            ge.registerFont(plexBI);

            Enumeration<Object> uiSettings = UIManager.getDefaults().keys();
            while (uiSettings.hasMoreElements()) {
                Object k = uiSettings.nextElement();
                Object v = UIManager.get(k);
                if (v instanceof FontUIResource) {
                    boolean bold = ((FontUIResource) v).isBold();
                    boolean italic = ((FontUIResource) v).isItalic();

                    int fontstyle = Font.PLAIN;
                    if (bold)
                        fontstyle |= Font.BOLD;
                    if (italic)
                        fontstyle |= Font.ITALIC;

                    UIManager.put(k, new FontUIResource(plexR.getFontName(), fontstyle, ((FontUIResource) v).getSize() - 1 /* Plex is large */));
                }
            }
        } catch (Exception ex) {
            DebugWriter.INSTANCE.logError("FONT", ex, "Failed to load font.");
        }
    }
}
