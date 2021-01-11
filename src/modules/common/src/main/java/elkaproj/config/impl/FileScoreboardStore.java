package elkaproj.config.impl;

import elkaproj.DebugWriter;
import elkaproj.config.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Loads and stores scoreboards in files.
 *
 * @see IScoreboard
 * @see IScoreboardEntry
 * @see IScoreboardTotalEntry
 */
public class FileScoreboardStore implements IScoreboardStore {

    private final File root;

    /**
     * Creates a new scoreboard store from given root directory.
     *
     * @param root Root directory for the scoreboard.
     */
    public FileScoreboardStore(File root) {
        this.root = root;
    }

    /**
     * Loads a scoreboard and returns it.
     *
     * @param levelPack Level pack to load scoreboard for.
     * @return Loaded scoreboard.
     * @throws IOException Loading failed.
     */
    @Override
    public IScoreboard loadScoreboard(ILevelPack levelPack) throws IOException {
        try {
            File scoreboard = new File(this.root, String.format("%s.xml", levelPack.getId()));
            if (!scoreboard.exists()) {
                XmlFileScoreboard s = new XmlFileScoreboard();
                s.levelPackId = levelPack.getId();
                s.levelPack = levelPack;
                s.entries = new XmlFileScoreboardEntry[0];

                return s;
            }

            JAXBContext jaxbctx = JAXBContext.newInstance(XmlFileScoreboard.class);
            Unmarshaller jaxb = jaxbctx.createUnmarshaller();
            return (IScoreboard) jaxb.unmarshal(scoreboard);
        } catch (Exception ex) {
            DebugWriter.INSTANCE.logError("LDR-FILE", ex, "Error while loading scoreboard for level pack '%s'.", levelPack.getId());
            throw new IOException(ex);
        }
    }

    /**
     * Stores a scoreboard entry in the scoreboard.
     *
     * @param scoreboard Scoreboard to store the entry in.
     * @param level Level to store the entry for.
     * @param playerName Name of the player to store the score for.
     * @param score Score achieved by the player.
     * @throws IOException Loading failed.
     */
    @Override
    public void putEntry(IScoreboard scoreboard, ILevel level, String playerName, int score) throws IOException {
        XmlFileScoreboard s = (XmlFileScoreboard) scoreboard;

        ArrayList<XmlFileScoreboardEntry> newEntries = new ArrayList<>(s.entries.length + 1);
        boolean replaceEntry = true;
        for (XmlFileScoreboardEntry entry : s.entries) {
            if (entry.levelNumber == level.getOrdinal() && entry.playerName.equals(playerName)) {
                if (score > entry.score) {
                    replaceEntry = false;
                    continue;
                }
            }

            newEntries.add(entry);
        }

        if (replaceEntry) {
            XmlFileScoreboardEntry newEntry = new XmlFileScoreboardEntry();
            newEntry.playerName = playerName;
            newEntry.levelNumber = level.getOrdinal();
            newEntry.level = level;
            newEntry.score = score;

            newEntries.add(newEntry);
        }

        newEntries.toArray(s.entries = new XmlFileScoreboardEntry[newEntries.size()]);

        try {
            File scoreboardF = new File(this.root, String.format("%s.xml", s.levelPackId));

            JAXBContext jaxbctx = JAXBContext.newInstance(XmlFileScoreboard.class);
            Marshaller jaxb = jaxbctx.createMarshaller();
            jaxb.marshal(s, scoreboardF);
        } catch (Exception ex) {
            DebugWriter.INSTANCE.logError("LDR-FILE", ex, "Error while loading scoreboard for level pack '%s'.", s.levelPackId);
            throw new IOException(ex);
        }
    }

    @XmlRootElement(name = "entry")
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class XmlFileScoreboardEntry implements IScoreboardEntry {

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

    private static class XmlFileScoreboardTotalEntry implements IScoreboardTotalEntry {

        private final String playerName;
        private final boolean completedAll;
        private final int score;
        private final List<IScoreboardEntry> entries;

        public XmlFileScoreboardTotalEntry(String playerName, boolean completedAll, int score, List<IScoreboardEntry> entries) {
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

            public XmlFileScoreboardTotalEntry toTotalEntry(Set<Integer> allLevels) {
                return new XmlFileScoreboardTotalEntry(this.playerName, this.completedLevels.equals(allLevels), this.score, this.entries);
            }
        }
    }

    @XmlRootElement(name = "scoreboard")
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class XmlFileScoreboard implements IScoreboard {

        @XmlElement(name = "level-pack")
        public String levelPackId;

        @XmlElement(name = "entry")
        public XmlFileScoreboardEntry[] entries;

        private transient ILevelPack levelPack = null;

        @Override
        public ILevelPack getLevelPack() {
            return this.levelPack;
        }

        public void setLevelPack(ILevelPack levelPack) {
            this.levelPack = levelPack;
        }

        @Override
        public List<IScoreboardTotalEntry> getAllTotalEntries() {
            final Set<Integer> levels = StreamSupport.stream(this.levelPack.spliterator(), false)
                    .map(ILevel::getOrdinal)
                    .collect(Collectors.toSet());

            Map<String, XmlFileScoreboardTotalEntry.Temporary> entries = new HashMap<>();

            for (XmlFileScoreboardEntry entry : this.entries) {
                String pname = entry.playerName;
                XmlFileScoreboardTotalEntry.Temporary temp;
                if (!entries.containsKey(pname))
                    entries.put(pname, temp = new XmlFileScoreboardTotalEntry.Temporary(pname));
                else
                    temp = entries.get(pname);

                temp.score += entry.score;
                temp.entries.add(entry);
                temp.completedLevels.add(entry.levelNumber);
            }

            return entries.entrySet()
                    .stream()
                    .map(x -> x.getValue().toTotalEntry(levels))
                    .collect(Collectors.toList());
        }

        @Override
        public List<IScoreboardEntry> getLevelEntries(ILevel level) {
            return Arrays.stream(this.entries)
                    .filter(x -> x.levelNumber == level.getOrdinal())
                    .collect(Collectors.toList());
        }
    }
}
