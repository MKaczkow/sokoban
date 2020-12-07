package elkaproj;

import elkaproj.config.IConfiguration;
import elkaproj.config.impl.FileConfigurationLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Entry {
    public static void main(String[] args) {
        File f = new File("config", "config.txt");
        IConfiguration config = null;
        try (FileInputStream fi = new FileInputStream(f)) {
            try (FileConfigurationLoader loader = new FileConfigurationLoader(fi)) {
                config = loader.load();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Inspector inspector = new Inspector(System.out);
        inspector.inspect(config);
    }
}
