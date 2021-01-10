package elkaproj.config.impl;

import elkaproj.Common;
import elkaproj.Dimensions;
import elkaproj.config.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Loads levels from remote XML data over HTTP.
 */
public class HttpLevelPackLoader implements ILevelPackLoader {

    private final URL endpointBase;

    /**
     * Creates a new load from URL endpoint.
     *
     * @param endpointBase URL endpoint to load configuration from.
     */
    public HttpLevelPackLoader(URL endpointBase) {
        this.endpointBase = endpointBase;
    }

    /**
     * Loads the specified level pack.
     *
     * @param id ID of the level pack to load.
     * @return Loaded level pack.
     * @throws IOException Loading of the pack failed.
     */
    @Override
    public ILevelPack loadPack(String id) throws IOException {
        URL pack = new URL(this.endpointBase, this.appendPath(this.endpointBase.getPath(), id));
        URL meta = new URL(pack, this.appendPath(pack.getPath(), "meta.xml"));

        XmlHttpLevelPackMeta xlpm;
        try {
            JAXBContext jaxbctx = JAXBContext.newInstance(XmlHttpLevelPackMeta.class);
            Unmarshaller jaxb = jaxbctx.createUnmarshaller();

            URLConnection con = meta.openConnection();
            con.setRequestProperty("User-Agent", Common.USER_AGENT);
            try (InputStream is = con.getInputStream()) {
                xlpm = (XmlHttpLevelPackMeta) jaxb.unmarshal(is);
            }
        } catch (JAXBException e) {
            throw new IOException(e);
        }

        List<ILevel> levels = new ArrayList<>();
        try {
            for (XmlHttpLevelMeta leveldef : xlpm.levels) {
                levels.add(this.loadLevel(leveldef, pack));
            }
        } catch (Exception e) {
            throw new IOException(e);
        }

        return new XmlHttpLevelPack(xlpm, levels);
    }

    private ILevel loadLevel(XmlHttpLevelMeta xdef, URL pack) throws IOException {
        URL lvldata = new URL(pack, this.appendPath(pack.getPath(), xdef.definitionFile));

        ArrayList<String> lines = new ArrayList<>();

        URLConnection con = lvldata.openConnection();
        con.setRequestProperty("User-Agent", Common.USER_AGENT);
        try (BufferedReader lvlin = new BufferedReader(
                new InputStreamReader(
                        con.getInputStream(), StandardCharsets.UTF_8.name()))) {

            String inputLine;
            while ((inputLine = lvlin.readLine()) != null && !inputLine.equals(""))
                lines.add(inputLine);
        }

        int width = lines.stream()
                .mapToInt(String::length)
                .max()
                .orElseThrow(IOException::new);
        int height = lines.size();

        LevelTile[][] tiles = new LevelTile[height][];
        for (int i = 0; i < height; i++) {
            tiles[i] = new LevelTile[width];
            Arrays.fill(tiles[i], LevelTile.WALL);
            String line = lines.get(i);
            for (int j = 0; j < width; j++) {
                tiles[i][j] = LevelTile.fromRepresentation(line.charAt(j));
            }
        }

        return new XmlHttpLevel(xdef, new Dimensions(width, height), tiles);
    }

    /**
     * Closes this loader.
     *
     * @throws IOException Closing failed.
     */
    @Override
    public void close() throws IOException {
    }

    private String appendPath(String base, String appendix) {
        if (appendix.startsWith("/"))
            appendix = appendix.substring(1);

        if (base.endsWith("/"))
            return base + appendix;
        else
            return base + "/" + appendix;
    }

    private static class XmlHttpLevel implements ILevel {

        private final int ordinal, bonusTimeThreshold, penaltyTimeThreshold, failTimeThreshold;
        private final String name;
        private final Dimensions dimensions;
        private final LevelTile[][] tiles;

        public XmlHttpLevel(XmlHttpLevelMeta xdef, Dimensions dims, LevelTile[][] tiles) {
            this.ordinal = xdef.ordinal;
            this.name = xdef.name;
            this.bonusTimeThreshold = xdef.bonusTime;
            this.penaltyTimeThreshold = xdef.penaltyTime;
            this.failTimeThreshold = xdef.failTime;
            this.dimensions = dims;
            this.tiles = tiles;
        }

        @Override
        public int getOrdinal() {
            return this.ordinal;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public int getBonusTimeThreshold() {
            return this.bonusTimeThreshold;
        }

        @Override
        public int getPenaltyTimeThreshold() {
            return this.penaltyTimeThreshold;
        }

        @Override
        public int getFailTimeThreshold() {
            return this.failTimeThreshold;
        }

        @Override
        public Dimensions getSize() {
            return this.dimensions;
        }

        @Override
        public LevelTile[][] getTiles() {
            // copy to prevent modifications
            LevelTile[][] tilesCopy = new LevelTile[this.dimensions.getHeight()][];
            for (int i = 0; i < this.dimensions.getHeight(); i++) {
                tilesCopy[i] = new LevelTile[this.dimensions.getWidth()];
                System.arraycopy(this.tiles[i], 0, tilesCopy[i], 0, tilesCopy[i].length);
            }

            return tilesCopy;
        }
    }

    private static class XmlHttpLevelPack implements ILevelPack {

        private final String name, id;
        private final List<ILevel> levels;

        public XmlHttpLevelPack(XmlHttpLevelPackMeta xdef, List<ILevel> levels) {
            this.name = xdef.name;
            this.id = xdef.id;
            this.levels = levels;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public int getCount() {
            return this.levels.size();
        }

        @Override
        public ILevel getLevel(int number) {
            return this.levels.get(number);
        }

        @Override
        public Iterator<ILevel> iterator() {
            return this.levels.iterator();
        }
    }

    @XmlRootElement(name = "level-pack")
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class XmlHttpLevelPackMeta {

        @XmlAttribute(name = "id")
        public String id;

        @XmlAttribute(name = "name")
        public String name;

        @XmlElement(name = "level")
        public XmlHttpLevelMeta[] levels;
    }

    @XmlRootElement(name = "level")
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class XmlHttpLevelMeta {

        @XmlElement(name = "ordinal")
        public int ordinal;

        @XmlElement(name = "name")
        public String name;

        @XmlElement(name = "bonus-time")
        public int bonusTime;

        @XmlElement(name = "penalty-time")
        public int penaltyTime;

        @XmlElement(name = "fail-time")
        public int failTime;

        @XmlElement(name = "definition")
        public String definitionFile;
    }
}
