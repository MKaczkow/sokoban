package elkaproj.config.impl;

import elkaproj.config.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class XmlScoreboardImpl {

    @XmlRootElement(name = "entry")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class XmlScoreboardEntry implements IScoreboardEntry {

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

    public static class XmlScoreboardTotalEntry implements IScoreboardTotalEntry {

        private final String playerName;
        private final boolean completedAll;
        private final int score;
        private final List<IScoreboardEntry> entries;

        public XmlScoreboardTotalEntry(String playerName, boolean completedAll, int score, List<IScoreboardEntry> entries) {
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

            public XmlScoreboardTotalEntry toTotalEntry(Set<Integer> allLevels) {
                return new XmlScoreboardTotalEntry(this.playerName, this.completedLevels.equals(allLevels), this.score, this.entries);
            }
        }
    }

    @XmlRootElement(name = "scoreboard")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class XmlScoreboard implements IScoreboard {

        @XmlElement(name = "level-pack")
        public String levelPackId;

        @XmlElement(name = "entry")
        public XmlScoreboardEntry[] entries;

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

            Map<String, XmlScoreboardTotalEntry.Temporary> entries = new HashMap<>();

            for (XmlScoreboardEntry entry : this.entries) {
                String pname = entry.playerName;
                XmlScoreboardTotalEntry.Temporary temp;
                if (!entries.containsKey(pname))
                    entries.put(pname, temp = new XmlScoreboardTotalEntry.Temporary(pname));
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
