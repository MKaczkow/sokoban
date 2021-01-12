package elkaproj.config.impl;

import elkaproj.DebugWriter;
import elkaproj.config.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
                XmlScoreboardImpl.XmlScoreboard s = new XmlScoreboardImpl.XmlScoreboard();
                s.levelPackId = levelPack.getId();
                s.setLevelPack(levelPack);
                s.entries = new XmlScoreboardImpl.XmlScoreboardEntry[0];

                return s;
            }

            JAXBContext jaxbctx = JAXBContext.newInstance(XmlScoreboardImpl.XmlScoreboard.class);
            Unmarshaller jaxb = jaxbctx.createUnmarshaller();
            XmlScoreboardImpl.XmlScoreboard res = (XmlScoreboardImpl.XmlScoreboard) jaxb.unmarshal(scoreboard);

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
            DebugWriter.INSTANCE.logError("LDR-FILE", ex, "Error while loading scoreboard for level pack '%s'.", levelPack.getId());
            throw new IOException(ex);
        }
    }

    /**
     * Stores a scoreboard entry in the scoreboard.
     *
     * @param scoreboard Scoreboard to store the entry in.
     * @param level      Level to store the entry for.
     * @param playerName Name of the player to store the score for.
     * @param score      Score achieved by the player.
     * @throws IOException Loading failed.
     */
    @Override
    public void putEntry(IScoreboard scoreboard, ILevel level, String playerName, int score) throws IOException {
        XmlScoreboardImpl.XmlScoreboard s = (XmlScoreboardImpl.XmlScoreboard) scoreboard;

        ArrayList<XmlScoreboardImpl.XmlScoreboardEntry> newEntries = new ArrayList<>(s.entries.length + 1);
        boolean replaceEntry = true;
        for (XmlScoreboardImpl.XmlScoreboardEntry entry : s.entries) {
            if (entry.levelNumber == level.getOrdinal() && entry.playerName.equals(playerName)) {
                if (score > entry.score) {
                    replaceEntry = false;
                    continue;
                }
            }

            newEntries.add(entry);
        }

        if (replaceEntry) {
            XmlScoreboardImpl.XmlScoreboardEntry newEntry = new XmlScoreboardImpl.XmlScoreboardEntry();
            newEntry.playerName = playerName;
            newEntry.levelNumber = level.getOrdinal();
            newEntry.score = score;
            newEntry.setLevel(level);

            newEntries.add(newEntry);
        }

        newEntries.toArray(s.entries = new XmlScoreboardImpl.XmlScoreboardEntry[newEntries.size()]);

        try {
            File scoreboardF = new File(this.root, String.format("%s.xml", s.levelPackId));

            JAXBContext jaxbctx = JAXBContext.newInstance(XmlScoreboardImpl.XmlScoreboard.class);
            Marshaller jaxb = jaxbctx.createMarshaller();
            jaxb.marshal(s, scoreboardF);
        } catch (Exception ex) {
            DebugWriter.INSTANCE.logError("LDR-FILE", ex, "Error while loading scoreboard for level pack '%s'.", s.levelPackId);
            throw new IOException(ex);
        }
    }
}
