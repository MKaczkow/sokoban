package elkaproj.config.impl;

import com.sun.istack.NotNull;
import elkaproj.CastingIterator;
import elkaproj.Common;
import elkaproj.Dimensions;
import elkaproj.config.ILevel;
import elkaproj.config.ILevelPack;
import elkaproj.config.ILevelPackLoader;
import elkaproj.config.LevelTile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.*;
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
class HttpLevelPackLoader implements ILevelPackLoader {

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
        URL meta = new URL(this.endpointBase, this.appendPath(this.endpointBase.getPath(), "meta/" + id));

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

        List<XmlHttpLevel> levels = new ArrayList<>();
        try {
            for (XmlHttpLevelMeta leveldef : xlpm.levels) {
                levels.add((XmlHttpLevel) this.loadLevel(leveldef, id));
            }
        } catch (Exception e) {
            throw new IOException(e);
        }

        return new XmlHttpLevelPack(xlpm, levels);
    }

    private ILevel loadLevel(XmlHttpLevelMeta xdef, String packId) throws IOException {
        URL lvldata = new URL(this.endpointBase, this.appendPath(this.endpointBase.getPath(), "data/" + packId + "/" + xdef.ordinal));

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

        return new XmlHttpLevel(xdef, new Dimensions(width, height), tiles, xdef.definitionFile);
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
        private final String name, originalFile;
        private final Dimensions dimensions;
        private final LevelTile[][] tiles;

        public XmlHttpLevel(XmlHttpLevelMeta xdef, Dimensions dims, LevelTile[][] tiles, String originalFile) {
            this.ordinal = xdef.ordinal;
            this.name = xdef.name;
            this.bonusTimeThreshold = xdef.bonusTime;
            this.penaltyTimeThreshold = xdef.penaltyTime;
            this.failTimeThreshold = xdef.failTime;
            this.dimensions = dims;
            this.tiles = tiles;
            this.originalFile = originalFile;
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

        public String getOriginalFile() {
            return originalFile;
        }
    }

    private static class XmlHttpLevelPack implements ILevelPack {

        private final String name, id;
        private final List<XmlHttpLevel> levels;

        public XmlHttpLevelPack(XmlHttpLevelPackMeta xdef, List<XmlHttpLevel> levels) {
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
            return new CastingIterator<>(this.levels);
        }

        @Override
        public void serialize(OutputStream os) throws IOException, JAXBException {
            XmlHttpLevelPackMeta meta = new XmlHttpLevelPackMeta();
            meta.id = this.id;
            meta.name = this.name;
            meta.levels = this.levels.stream()
                    .map(x -> {
                        XmlHttpLevelMeta xmeta = new XmlHttpLevelMeta();
                        xmeta.ordinal = x.getOrdinal();
                        xmeta.name = x.getName();
                        xmeta.bonusTime = x.getBonusTimeThreshold();
                        xmeta.penaltyTime = x.getPenaltyTimeThreshold();
                        xmeta.failTime = x.getFailTimeThreshold();
                        xmeta.definitionFile = x.getOriginalFile();
                        return xmeta;
                    })
                    .toArray(XmlHttpLevelMeta[]::new);

            JAXBContext jaxbctx = JAXBContext.newInstance(meta.getClass());
            Marshaller jaxb = jaxbctx.createMarshaller();
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                jaxb.marshal(meta, baos);
                os.write(baos.toByteArray());
            }
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
