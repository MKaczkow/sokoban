package elkaproj.config.impl;

import elkaproj.Common;
import elkaproj.DebugWriter;
import elkaproj.config.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

            JAXBContext jaxbctx = JAXBContext.newInstance(XmlHttpScoreboard.class);
            Unmarshaller jaxb = jaxbctx.createUnmarshaller();

            URLConnection con = scoreboard.openConnection();
            con.setRequestProperty("User-Agent", Common.USER_AGENT);

            XmlHttpScoreboard res;
            try (InputStream is = con.getInputStream()) {
                res = (XmlHttpScoreboard) jaxb.unmarshal(is);
            }

            res.levelPack = levelPack;
            for (XmlHttpScoreboardEntry entry : res.entries) {
                entry.level = levelPack.getLevel(entry.levelNumber);
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

        XmlHttpScoreboard xs1 = (XmlHttpScoreboard) scoreboard;
        XmlHttpScoreboard xs2 = (XmlHttpScoreboard) this.loadScoreboard(scoreboard.getLevelPack());

        xs1.entries = xs2.entries;
    }

    @XmlRootElement(name = "entry")
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class XmlHttpScoreboardEntry implements IScoreboardEntry {

        @XmlElement(name = "player")
        public String playerName;

        @XmlElement(name = "level")
        public int levelNumber;

        @XmlElement(name = "score")
        public int score;

        private transient ILevel level = null;

        @Override
        public String getPlayerName() {
            return this.playerName;
        }

        @Override
        public ILevel getLevel() {
            return this.level;
        }

        public void setLevel(ILevel level) {
            this.level = level;
        }

        @Override
        public int getScore() {
            return this.score;
        }
    }

    private static class XmlHttpScoreboardTotalEntry implements IScoreboardTotalEntry {

        private final String playerName;
        private final boolean completedAll;
        private final int score;
        private final List<IScoreboardEntry> entries;

        public XmlHttpScoreboardTotalEntry(String playerName, boolean completedAll, int score, List<IScoreboardEntry> entries) {
            this.playerName = playerName;
            this.completedAll = completedAll;
            this.score = score;
            this.entries = entries;
        }

        @Override
        public String getPlayerName() {
            return this.playerName;
        }

        @Override
        public int getScore() {
            return this.score;
        }

        @Override
        public boolean hasCompletedAllLevels() {
            return this.completedAll;
        }

        @Override
        public List<IScoreboardEntry> getLevelEntries() {
            return this.entries;
        }

        public static class Temporary {
            public final String playerName;

            public int score = 0;
            public ArrayList<IScoreboardEntry> entries = new ArrayList<>();
            public HashSet<Integer> completedLevels = new HashSet<>();

            public Temporary(String playerName) {
                this.playerName = playerName;
            }

            public XmlHttpScoreboardTotalEntry toTotalEntry(Set<Integer> allLevels) {
                return new XmlHttpScoreboardTotalEntry(this.playerName, this.completedLevels.equals(allLevels), this.score, this.entries);
            }
        }
    }

    @XmlRootElement(name = "scoreboard")
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class XmlHttpScoreboard implements IScoreboard {

        @XmlElement(name = "level-pack")
        public String levelPackId;

        @XmlElement(name = "entry")
        public XmlHttpScoreboardEntry[] entries;

        private transient ILevelPack levelPack = null;

        @Override
        public ILevelPack getLevelPack() {
            return this.levelPack;
        }

        @Override
        public List<IScoreboardTotalEntry> getAllTotalEntries() {
            final Set<Integer> levels = StreamSupport.stream(this.levelPack.spliterator(), false)
                    .map(ILevel::getOrdinal)
                    .collect(Collectors.toSet());

            Map<String, XmlHttpScoreboardTotalEntry.Temporary> entries = new HashMap<>();

            for (XmlHttpScoreboardEntry entry : this.entries) {
                String pname = entry.playerName;
                XmlHttpScoreboardTotalEntry.Temporary temp;
                if (!entries.containsKey(pname))
                    entries.put(pname, temp = new XmlHttpScoreboardTotalEntry.Temporary(pname));
                else
                    temp = entries.get(pname);

                temp.score += entry.score;
                temp.entries.add(entry);
                temp.completedLevels.add(entry.levelNumber);
            }

            return entries.values()
                    .stream()
                    .map(temporary -> temporary.toTotalEntry(levels))
                    .collect(Collectors.toList());
        }

        @Override
        public List<IScoreboardEntry> getLevelEntries(ILevel level) {
            return Arrays.stream(this.entries)
                    .filter(x -> x.levelNumber == level.getOrdinal())
                    .collect(Collectors.toList());
        }

        @Override
        public void serialize(OutputStream os) throws IOException, JAXBException {
            JAXBContext jaxbctx = JAXBContext.newInstance(this.getClass());
            Marshaller jaxb = jaxbctx.createMarshaller();
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                jaxb.marshal(this, baos);
                os.write(baos.toByteArray());
            }
        }
    }
}
