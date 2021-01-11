package elkaproj.config.impl;

import elkaproj.CastingIterator;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Loads levels from XML file data.
 */
class FileLevelPackLoader implements ILevelPackLoader {

    private final File baseDir;

    /**
     * Creates a new load from given directory.
     *
     * @param baseDir Directory to load from.
     */
    public FileLevelPackLoader(File baseDir) {
        this.baseDir = baseDir;
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
        File pack = new File(this.baseDir, id);
        File meta = new File(pack, "meta.xml");
        if (!pack.exists() || !pack.isDirectory() || !meta.exists()) {
            throw new IOException("Specified pack does not exist.");
        }

        XmlFileLevelPackMeta xlpm;
        try {
            JAXBContext jaxbctx = JAXBContext.newInstance(XmlFileLevelPackMeta.class);
            Unmarshaller jaxb = jaxbctx.createUnmarshaller();
            xlpm = (XmlFileLevelPackMeta) jaxb.unmarshal(meta);
        } catch (JAXBException e) {
            throw new IOException(e);
        }

        List<XmlFileLevel> levels = new ArrayList<>();
        try {
            for (XmlFileLevelMeta leveldef : xlpm.levels) {
                levels.add((XmlFileLevel) this.loadLevel(leveldef, pack));
            }
        } catch (Exception e) {
            throw new IOException(e);
        }

        return new XmlFileLevelPack(xlpm, levels);
    }

    private ILevel loadLevel(XmlFileLevelMeta xdef, File pack) throws IOException {
        File lvldata = new File(pack, xdef.definitionFile);
        List<String> lines = Files.readAllLines(lvldata.toPath())
                .stream()
                .filter(x -> x.length() > 0)
                .collect(Collectors.toList());

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

        return new XmlFileLevel(xdef, new Dimensions(width, height), tiles, xdef.definitionFile);
    }

    /**
     * Closes this loader.
     *
     * @throws IOException Closing failed.
     */
    @Override
    public void close() throws IOException {
    }

    private static class XmlFileLevel implements ILevel {

        private final int ordinal, bonusTimeThreshold, penaltyTimeThreshold, failTimeThreshold;
        private final String name, originalFile;
        private final Dimensions dimensions;
        private final LevelTile[][] tiles;

        public XmlFileLevel(XmlFileLevelMeta xdef, Dimensions dims, LevelTile[][] tiles, String originalFile) {
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

    private static class XmlFileLevelPack implements ILevelPack {

        private final String name, id;
        private final List<XmlFileLevel> levels;

        public XmlFileLevelPack(XmlFileLevelPackMeta xdef, List<XmlFileLevel> levels) {
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
            XmlFileLevelPackMeta meta = new XmlFileLevelPackMeta();
            meta.id = this.id;
            meta.name = this.name;
            meta.levels = this.levels.stream()
                    .map(x -> {
                        XmlFileLevelMeta xmeta = new XmlFileLevelMeta();
                        xmeta.ordinal = x.getOrdinal();
                        xmeta.name = x.getName();
                        xmeta.bonusTime = x.getBonusTimeThreshold();
                        xmeta.penaltyTime = x.getPenaltyTimeThreshold();
                        xmeta.failTime = x.getFailTimeThreshold();
                        xmeta.definitionFile = x.getOriginalFile();
                        return xmeta;
                    })
                    .toArray(XmlFileLevelMeta[]::new);

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
    private static class XmlFileLevelPackMeta {

        @XmlAttribute(name = "id")
        public String id;

        @XmlAttribute(name = "name")
        public String name;

        @XmlElement(name = "level")
        public XmlFileLevelMeta[] levels;
    }

    @XmlRootElement(name = "level")
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class XmlFileLevelMeta {

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
