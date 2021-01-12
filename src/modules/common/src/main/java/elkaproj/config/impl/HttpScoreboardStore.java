package elkaproj.config.impl;

import elkaproj.Common;
import elkaproj.DebugWriter;
import elkaproj.config.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

/**
 * Loads scoreboards from HTTP endpoints.
 *
 * @see IScoreboard
 * @see IScoreboardEntry
 * @see IScoreboardTotalEntry
 */
public class HttpScoreboardStore implements IScoreboardStore {

    private final URL endpointBase;

    /**
     * Creates a new configuration loader.
     *
     * @param endpointBase Base endpoint of the scoreboard.
     */
    public HttpScoreboardStore(URL endpointBase) {
        this.endpointBase = endpointBase;
    }

    @Override
    public IScoreboard loadScoreboard(ILevelPack levelPack) throws IOException {
        try {
            URL scoreboard = new URL(this.endpointBase, Common.appendPath(this.endpointBase.getPath(), levelPack.getId()));

            JAXBContext jaxbctx = JAXBContext.newInstance(XmlScoreboardImpl.XmlScoreboard.class);
            Unmarshaller jaxb = jaxbctx.createUnmarshaller();

            URLConnection con = scoreboard.openConnection();
            con.setRequestProperty("User-Agent", Common.USER_AGENT);

            XmlScoreboardImpl.XmlScoreboard res;
            try (InputStream is = con.getInputStream()) {
                res = (XmlScoreboardImpl.XmlScoreboard) jaxb.unmarshal(is);
            }

            res.setLevelPack(levelPack);
            if (res.entries != null) {
                for (XmlScoreboardImpl.XmlScoreboardEntry entry : res.entries) {
                    entry.setLevel(levelPack.getLevel(entry.levelNumber));
                }
            } else {
                res.entries = new XmlScoreboardImpl.XmlScoreboardEntry[0];
            }

            return res;
        } catch (Exception ex) {
            DebugWriter.INSTANCE.logError("LDR-HTTP", ex, "Error while loading scoreboard for level pack '%s'.", levelPack.getId());
            throw new IOException(ex);
        }
    }

    @Override
    public void putEntry(IScoreboard scoreboard, ILevel level, String playerName, int score) throws IOException {
        String scoreStr = String.valueOf(score);
        byte[] scoreData = scoreStr.getBytes(StandardCharsets.UTF_8);

        URL url = new URL(this.endpointBase, Common.appendPath(this.endpointBase.getPath(), scoreboard.getLevelPack().getId() + "/" + level.getOrdinal() + "?player=" + playerName));
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", Common.USER_AGENT);
        con.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
        con.setRequestProperty("Content-Length", String.valueOf(scoreData.length));
        con.setUseCaches(false);
        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            os.write(scoreData);
        }

        DebugWriter.INSTANCE.logMessage("LDR-HTTP", "Score write result %d", con.getResponseCode());

        XmlScoreboardImpl.XmlScoreboard xs1 = (XmlScoreboardImpl.XmlScoreboard) scoreboard;
        XmlScoreboardImpl.XmlScoreboard xs2 = (XmlScoreboardImpl.XmlScoreboard) this.loadScoreboard(scoreboard.getLevelPack());

        xs1.entries = xs2.entries;
    }
}
