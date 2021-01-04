package elkaproj;

import elkaproj.config.ConfigurationValidator;
import elkaproj.config.IConfiguration;
import elkaproj.config.ILevelPack;
import elkaproj.config.ILevelPackLoader;
import elkaproj.config.commandline.CommandLineOptions;
import elkaproj.config.commandline.CommandLineParser;
import elkaproj.config.impl.FileConfigurationLoader;

import java.io.File;
import java.io.IOException;

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
            e.printStackTrace();
        }

        // inspect config
        inspector.inspect(config);
        inspector.inspect(levelPack);
    }
}
