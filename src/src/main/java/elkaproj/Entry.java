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
        File f = new File("config", "config.xml");
        IConfiguration config = null;
        ILevelPack levelPack = null;
        try (FileConfigurationLoader loader = new FileConfigurationLoader(f)) {
            config = loader.load();

            System.out.print("Configuration valid? ");
            System.out.println(ConfigurationValidator.validateConfiguration(config));

            try (ILevelPackLoader lvlloader = loader.getLevelPackLoader()) {
                levelPack = lvlloader.loadPack(config.getLevelPackId());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Inspector inspector = new Inspector(System.out);
        inspector.inspect(config);
        inspector.inspect(levelPack);

        CommandLineParser clp = new CommandLineParser(args);
        CommandLineOptions opts = clp.parse(CommandLineOptions.class);
        inspector.inspect(opts);

        if (opts.isHelp()) {
            clp.printHelp(System.out, CommandLineOptions.class);
            return;
        }
    }
}
