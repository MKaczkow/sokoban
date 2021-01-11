package elkaproj.config.commandline;

import elkaproj.httpserver.services.EndpointParserService;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class EndpointParserServiceTests {

    @Test
    public void testEndpointParser1() {
        EndpointParserService eps = new EndpointParserService();

        Map<String, String> data = eps.parse("/maps/meta/pack0", "/maps/meta/:id");

        Assert.assertNotNull(data);
        Assert.assertEquals(1, data.size());
        Assert.assertTrue(data.containsKey("id"));
        Assert.assertEquals("pack0", data.get("id"));
    }

    @Test
    public void testEndpointParser2() {
        EndpointParserService eps = new EndpointParserService();

        Map<String, String> data = eps.parse("/maps/meta/pack0/data", "/maps/meta/:id/data");

        Assert.assertNotNull(data);
        Assert.assertEquals(1, data.size());
        Assert.assertTrue(data.containsKey("id"));
        Assert.assertEquals("pack0", data.get("id"));
    }

    @Test
    public void testEndpointParserTooShort() {
        EndpointParserService eps = new EndpointParserService();

        Map<String, String> data = eps.parse("/maps/meta", "/maps/meta/:id");

        Assert.assertNull(data);
    }

    @Test
    public void testEndpointParserTooLong1() {
        EndpointParserService eps = new EndpointParserService();

        Map<String, String> data = eps.parse("/maps/meta/pack0/data", "/maps/meta/:id");

        Assert.assertNull(data);
    }

    @Test
    public void testEndpointParserTooLong2() {
        EndpointParserService eps = new EndpointParserService();

        Map<String, String> data = eps.parse("/maps/meta/pack0/data/zzz", "/maps/meta/:id/data");

        Assert.assertNull(data);
    }

    @Test
    public void testEndpointParserOptional() {
        EndpointParserService eps = new EndpointParserService();

        Map<String, String> data = eps.parse("/maps/meta", "/maps/meta/:id?");

        Assert.assertNotNull(data);
        Assert.assertEquals(0, data.size());
        Assert.assertFalse(data.containsKey("id"));
    }

    @Test
    public void testEndpointParserOptionalSpecified() {
        EndpointParserService eps = new EndpointParserService();

        Map<String, String> data = eps.parse("/maps/meta/pack0", "/maps/meta/:id?");

        Assert.assertNotNull(data);
        Assert.assertEquals(1, data.size());
        Assert.assertTrue(data.containsKey("id"));
        Assert.assertEquals("pack0", data.get("id"));
    }

    @Test
    public void testEndpointParserCatchAll1() {
        EndpointParserService eps = new EndpointParserService();

        Map<String, String> data = eps.parse("/maps/meta/pack0/data", "/maps/meta/:id...");

        Assert.assertNotNull(data);
        Assert.assertEquals(1, data.size());
        Assert.assertTrue(data.containsKey("id"));
        Assert.assertEquals("pack0/data", data.get("id"));
    }

    @Test
    public void testEndpointParserCatchAll2() {
        EndpointParserService eps = new EndpointParserService();

        Map<String, String> data = eps.parse("/maps/meta", "/maps/meta/:id...");

        Assert.assertNotNull(data);
        Assert.assertEquals(1, data.size());
        Assert.assertTrue(data.containsKey("id"));
        Assert.assertEquals("", data.get("id"));
    }
}
