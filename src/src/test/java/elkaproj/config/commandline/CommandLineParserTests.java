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
            "    Port of the configuration server.\n\n";

    private static final String UTF8 = StandardCharsets.UTF_8.name();

    @Test
    public void testHelp() {
        CommandLineParser clp = new CommandLineParser(new String[] {});

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (PrintStream ps = new PrintStream(baos, true, UTF8)) {
                // fix \r\n
                Field f = ps.getClass().getDeclaredField("textOut");
                f.setAccessible(true);
                Object bw = f.get(ps);
                f = bw.getClass().getDeclaredField("lineSeparator");
                f.setAccessible(true);
                f.set(bw, "\n");

                clp.printHelp(ps, CommandLineOptions.class);
            }

            Assert.assertEquals(expectedHelp, baos.toString(UTF8));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testLongArguments() {
        CommandLineParser clp = new CommandLineParser(new String[] { "--debug", "--online-hostname=test.example.com", "--online-port=42069" });
        CommandLineOptions opts = clp.parse(CommandLineOptions.class);

        this.assertCommon1(opts);
    }

    @Test
    public void testShortArguments1() {
        CommandLineParser clp = new CommandLineParser(new String[] { "-dstest.example.com", "-p42069" });
        CommandLineOptions opts = clp.parse(CommandLineOptions.class);

        this.assertCommon1(opts);
    }

    @Test
    public void testShortArguments2() {
        CommandLineParser clp = new CommandLineParser(new String[] { "-d", "-stest.example.com", "-p42069" });
        CommandLineOptions opts = clp.parse(CommandLineOptions.class);

        this.assertCommon1(opts);
    }

    @Test
    public void testShortArguments3() {
        CommandLineParser clp = new CommandLineParser(new String[] { "-d", "-s", "test.example.com", "-p42069" });
        CommandLineOptions opts = clp.parse(CommandLineOptions.class);

        this.assertCommon1(opts);
    }

    @Test
    public void testShortArguments4() {
        CommandLineParser clp = new CommandLineParser(new String[] { "-dho" });
        CommandLineOptions opts = clp.parse(CommandLineOptions.class);

        this.assertCommon2(opts);
    }

    @Test
    public void testShortArguments5() {
        CommandLineParser clp = new CommandLineParser(new String[] { "-dh", "-o" });
        CommandLineOptions opts = clp.parse(CommandLineOptions.class);

        this.assertCommon2(opts);
    }

    @Test
    public void testShortArguments6() {
        CommandLineParser clp = new CommandLineParser(new String[] { "-d", "-h", "-o" });
        CommandLineOptions opts = clp.parse(CommandLineOptions.class);

        this.assertCommon2(opts);
    }

    @Test
    public void testMixedArguments() {
        CommandLineParser clp = new CommandLineParser(new String[] { "-d", "--online-hostname=test.example.com", "-p", "42069" });
        CommandLineOptions opts = clp.parse(CommandLineOptions.class);

        this.assertCommon1(opts);
    }

    private void assertCommon1(CommandLineOptions opts) {
        Assert.assertNotNull(opts);
        Assert.assertTrue(opts.isDebug());
        Assert.assertFalse(opts.isHelp());
        Assert.assertFalse(opts.useOnline());
        Assert.assertEquals("test.example.com", opts.getOnlineServer());
        Assert.assertEquals(42069, opts.getOnlinePort());
    }

    private void assertCommon2(CommandLineOptions opts) {
        Assert.assertNotNull(opts);
        Assert.assertTrue(opts.isDebug());
        Assert.assertTrue(opts.isHelp());
        Assert.assertTrue(opts.useOnline());
        Assert.assertEquals("localhost", opts.getOnlineServer());
        Assert.assertEquals(20420, opts.getOnlinePort());
    }
}