package elkaproj.config.commandline;

import elkaproj.config.language.Language;
import elkaproj.config.language.LanguageLoader;
import org.junit.Assert;
import org.junit.Test;

public class LanguageLoaderTests {

    @Test
    public void testLanguageLoading() {
        LanguageLoader languageLoader = new LanguageLoader();
        Language language = languageLoader.loadLanguage("pl-PL");

        Assert.assertEquals("Polski", language.getName());

        Assert.assertEquals("Plik", language.getValue("menu.file.label"));
        Assert.assertEquals("Wyjście", language.getValue("menu.file.items.exit"));

        Assert.assertEquals("Gra", language.getValue("menu.game.label"));
        Assert.assertEquals("Pauza", language.getValue("menu.game.items.pause"));
        Assert.assertEquals("Wznów", language.getValue("menu.game.items.resume"));
        Assert.assertEquals("Resetuj poziom", language.getValue("menu.game.items.reset"));
        Assert.assertEquals("Tabele wyników", language.getValue("menu.game.items.scoreboard"));
        Assert.assertEquals("Twórcy", language.getValue("menu.game.items.authors"));
    }
}
