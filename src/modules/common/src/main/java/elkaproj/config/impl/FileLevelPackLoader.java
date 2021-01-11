package elkaproj.config.impl;

import elkaproj.Dimensions;
import elkaproj.config.ILevel;
import elkaproj.config.ILevelPack;
import elkaproj.config.ILevelPackLoader;
import elkaproj.config.LevelTile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
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

        XmlLevelImpl.XmlLevelPackMeta xlpm;
        try {
            JAXBContext jaxbctx = JAXBContext.newInstance(XmlLevelImpl.XmlLevelPackMeta.class);
            Unmarshaller jaxb = jaxbctx.createUnmarshaller();
            xlpm = (XmlLevelImpl.XmlLevelPackMeta) jaxb.unmarshal(meta);
        } catch (JAXBException e) {
            throw new IOException(e);
        }

        List<XmlLevelImpl.XmlLevel> levels = new ArrayList<>();
        try {
            for (XmlLevelImpl.XmlLevelMeta leveldef : xlpm.levels) {
                levels.add((XmlLevelImpl.XmlLevel) this.loadLevel(leveldef, pack));
            }
        } catch (Exception e) {
            throw new IOException(e);
        }

        return new XmlLevelImpl.XmlLevelPack(xlpm, levels);
    }

    private ILevel loadLevel(XmlLevelImpl.XmlLevelMeta xdef, File pack) throws IOException {
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

        return new XmlLevelImpl.XmlLevel(xdef, new Dimensions(width, height), tiles, xdef.definitionFile);
    }

    /**
     * Closes this loader.
     *
     * @throws IOException Closing failed.
     */
    @Override
    public void close() throws IOException {
    }
}
