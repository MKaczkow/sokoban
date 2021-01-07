package elkaproj;

import elkaproj.config.ConfigurationValidator;
import elkaproj.config.IConfiguration;
import elkaproj.config.ILevelPack;
import elkaproj.config.ILevelPackLoader;
import elkaproj.config.commandline.CommandLineOptions;
import elkaproj.config.commandline.CommandLineParser;
import elkaproj.config.impl.FileConfigurationLoader;
import elkaproj.config.impl.HttpConfigurationLoader;
import elkaproj.config.language.Language;
import elkaproj.config.language.LanguageLoader;
import elkaproj.ui.GuiRootFrame;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * Program entrypoint.
 */
public class Entry {

    /**
     * User agent for HTTP requests.
     */
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36 Edg/87.0.664.66";

    /**
     * Regular variant of the UI font.
     */
    public static Font IBMPlexRegular;

    /**
     * Italic variant of the UI font.
     */
    public static Font IBMPlexItalic;


    /**
     * Bold variant of the UI font.
     */
    public static Font IBMPlexBold;


    /**
     * Bold italic variant of the UI font.
     */
    public static Font IBMPlexBoldItalic;

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
        IConfiguration config = null;
        ILevelPack levelPack = null;
        if (!opts.useOnline()) {
            System.setProperty("http.agent", USER_AGENT);

            File f = new File("config", "config.xml");
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
        } else {
            try {
                URL url = new URL(opts.getOnlineEndpoint());
                try (HttpConfigurationLoader loader = new HttpConfigurationLoader(url)) {
                    config = loader.load();

                    DebugWriter.INSTANCE.logMessage("INIT", "Configuration valid? %b", ConfigurationValidator.validateConfiguration(config));

                    try (ILevelPackLoader lvlloader = loader.getLevelPackLoader()) {
                        levelPack = lvlloader.loadPack(config.getLevelPackId());
                    }
                }
            } catch (Exception ex) {
                DebugWriter.INSTANCE.logError("INIT", ex, "Failed to load configuration.");
                System.exit(1);
            }
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
        // have to fix fonts first, since java can't into unicode
        Language finalUiLang = uiLang;
        IConfiguration finalConfig = config;
        ILevelPack finalLevelPack = levelPack;
        SwingUtilities.invokeLater(() -> {
            loadPlexFont();
            try {
                GuiRootFrame mainframe = new GuiRootFrame(finalUiLang, finalConfig, finalLevelPack);
                mainframe.setVisible(true);
            } catch (Exception ex) {
                DebugWriter.INSTANCE.logError("UI-INIT", ex, "Failed to initialize UI");
            }
        });
    }

    private static void loadPlexFont() {
        try {
            InputStream plexFR = Entry.class.getResourceAsStream("/fonts/IBMPlexSans-Regular.ttf"),
                    plexFI = Entry.class.getResourceAsStream("/fonts/IBMPlexSans-Italic.ttf"),
                    plexFB = Entry.class.getResourceAsStream("/fonts/IBMPlexSans-Bold.ttf"),
                    plexFBI = Entry.class.getResourceAsStream("/fonts/IBMPlexSans-BoldItalic.ttf");

            Font plexR = Font.createFont(Font.TRUETYPE_FONT, plexFR),
                    plexI = Font.createFont(Font.TRUETYPE_FONT, plexFI),
                    plexB = Font.createFont(Font.TRUETYPE_FONT, plexFB),
                    plexBI = Font.createFont(Font.TRUETYPE_FONT, plexFBI);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(plexR);
            ge.registerFont(plexI);
            ge.registerFont(plexB);
            ge.registerFont(plexBI);

            IBMPlexRegular = plexR;
            IBMPlexBold = plexB;
            IBMPlexItalic = plexI;
            IBMPlexBoldItalic = plexBI;

            Enumeration<Object> uiSettings = UIManager.getDefaults().keys();
            while (uiSettings.hasMoreElements()) {
                Object k = uiSettings.nextElement();
                Object v = UIManager.get(k);
                if (v instanceof FontUIResource) {
                    boolean bold = ((FontUIResource) v).isBold();
                    boolean italic = ((FontUIResource) v).isItalic();

                    Font f = plexR;
                    if (bold && !italic)
                        f = plexB;
                    if (!bold && italic)
                        f = plexI;
                    else if (bold && italic)
                        f = plexBI;

                    UIManager.put(k, new FontUIResource(f.deriveFont(14F)));
                }
            }
        } catch (Exception ex) {
            DebugWriter.INSTANCE.logError("FONT", ex, "Failed to load font.");
        }
    }
}
