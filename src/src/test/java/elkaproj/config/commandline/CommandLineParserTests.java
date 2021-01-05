package elkaproj.config.commandline;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

public class CommandLineParserTests {

    String expectedHelp = "Available options:\n" +
            "\n" +
            "  --help | -h\n" +
            "    Displays help.\n" +
            "\n" +
            "  --debug | -d\n" +
            "    Enables debug mode. This prints details to the console.\n" +
            "\n" +
            "  --online | -o\n" +
            "    Enables online mode. This causes the game to pull configuration data from the specified server.\n" +
            "\n" +
            "  --online-hostname=value | -svalue | -s value\n" +
            "    Online server to pull configuration data from.\n" +
            "\n" +
            "  --online-port=value | -pvalue | -p value\n" +
            "    Port of the configuration server.\n" +
            "\n" +
            "  --language=value | -lvalue | -l value\n" +
            "    UI language.\n" +
            "\n";

    private static final String UTF8 = StandardCharsets.UTF_8.name();

    @Test
    public void testHelp() {
        CommandLineParser<CommandLineOptions> clp = new CommandLineParser<>(CommandLineOptions.class);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (PrintStream ps = new PrintStream(baos, true, UTF8)) {
                // fix \r\n
                Field f = ps.getClass().getDeclaredField("textOut");
                f.setAccessible(true);
                Object bw = f.get(ps);
                f = bw.getClass().getDeclaredField("lineSeparator");
                f.setAccessible(true);
                f.set(bw, "\n");

                clp.printHelp(ps);
            }

            Assert.assertEquals(expectedHelp, baos.toString(UTF8));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testLongArguments() {
        CommandLineParser<CommandLineOptions> clp = new CommandLineParser<>(CommandLineOptions.class);
        CommandLineOptions opts = clp.parse(new String[] { "--debug", "--online-hostname=test.example.com", "--online-port=42069" });

        this.assertCommon1(opts);
    }

    @Test
    public void testShortArguments1() {
        CommandLineParser<CommandLineOptions> clp = new CommandLineParser<>(CommandLineOptions.class);
        CommandLineOptions opts = clp.parse(new String[] { "-dstest.example.com", "-p42069" });

        this.assertCommon1(opts);
    }

    @Test
    public void testShortArguments2() {
        CommandLineParser<CommandLineOptions> clp = new CommandLineParser<>(CommandLineOptions.class);
        CommandLineOptions opts = clp.parse(new String[] { "-d", "-stest.example.com", "-p42069" });

        this.assertCommon1(opts);
    }

    @Test
    public void testShortArguments3() {
        CommandLineParser<CommandLineOptions> clp = new CommandLineParser<>(CommandLineOptions.class);
        CommandLineOptions opts = clp.parse(new String[] { "-d", "-s", "test.example.com", "-p42069" });

        this.assertCommon1(opts);
    }

    @Test
    public void testShortArguments4() {
        CommandLineParser<CommandLineOptions> clp = new CommandLineParser<>(CommandLineOptions.class);
        CommandLineOptions opts = clp.parse(new String[] { "-dho" });

        this.assertCommon2(opts);
    }

    @Test
    public void testShortArguments5() {
        CommandLineParser<CommandLineOptions> clp = new CommandLineParser<>(CommandLineOptions.class);
        CommandLineOptions opts = clp.parse(new String[] { "-dh", "-o" });

        this.assertCommon2(opts);
    }

    @Test
    public void testShortArguments6() {
        CommandLineParser<CommandLineOptions> clp = new CommandLineParser<>(CommandLineOptions.class);
        CommandLineOptions opts = clp.parse(new String[] { "-d", "-h", "-o" });

        this.assertCommon2(opts);
    }

    @Test
    public void testMixedArguments() {
        CommandLineParser<CommandLineOptions> clp = new CommandLineParser<>(CommandLineOptions.class);
        CommandLineOptions opts = clp.parse(new String[] { "-d", "--online-hostname=test.example.com", "-p", "42069" });

        this.assertCommon1(opts);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsIncompleteArg1() {
        CommandLineParser<CommandLineOptions> clp = new CommandLineParser<>(CommandLineOptions.class);
        CommandLineOptions opts = clp.parse(new String[] { "-p" });

        Assert.assertNull(opts);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsIncompleteArg2() {
        CommandLineParser<CommandLineOptions> clp = new CommandLineParser<>(CommandLineOptions.class);
        CommandLineOptions opts = clp.parse(new String[] { "--online-port" });

        Assert.assertNull(opts);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsUnknownArg() {
        CommandLineParser<CommandLineOptions> clp = new CommandLineParser<>(CommandLineOptions.class);
        CommandLineOptions opts = clp.parse(new String[] { "--lmao" });

        Assert.assertNull(opts);
    }

    private void assertCommon1(CommandLineOptions opts) {
        Assert.assertNotNull(opts);
        Assert.assertTrue(opts.isDebug());
        Assert.assertFalse(opts.isHelp());
        Assert.assertFalse(opts.useOnline());
        Assert.assertEquals("test.example.com", opts.getOnlineServer());
        Assert.assertEquals(42069, opts.getOnlinePort());
        Assert.assertEquals("pl-PL", opts.getLanguage());
    }

    private void assertCommon2(CommandLineOptions opts) {
        Assert.assertNotNull(opts);
        Assert.assertTrue(opts.isDebug());
        Assert.assertTrue(opts.isHelp());
        Assert.assertTrue(opts.useOnline());
        Assert.assertEquals("localhost", opts.getOnlineServer());
        Assert.assertEquals(20420, opts.getOnlinePort());
        Assert.assertEquals("pl-PL", opts.getLanguage());
    }
}