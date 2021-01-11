package elkaproj.config.impl;

import elkaproj.Common;
import elkaproj.Dimensions;
import elkaproj.config.ILevel;
import elkaproj.config.ILevelPack;
import elkaproj.config.ILevelPackLoader;
import elkaproj.config.LevelTile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
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
        URL meta = new URL(this.endpointBase, Common.appendPath(this.endpointBase.getPath(), "meta/" + id));

        XmlLevelImpl.XmlLevelPackMeta xlpm;
        try {
            JAXBContext jaxbctx = JAXBContext.newInstance(XmlLevelImpl.XmlLevelPackMeta.class);
            Unmarshaller jaxb = jaxbctx.createUnmarshaller();

            URLConnection con = meta.openConnection();
            con.setRequestProperty("User-Agent", Common.USER_AGENT);
            try (InputStream is = con.getInputStream()) {
                xlpm = (XmlLevelImpl.XmlLevelPackMeta) jaxb.unmarshal(is);
            }
        } catch (JAXBException e) {
            throw new IOException(e);
        }

        List<XmlLevelImpl.XmlLevel> levels = new ArrayList<>();
        try {
            for (XmlLevelImpl.XmlLevelMeta leveldef : xlpm.levels) {
                levels.add((XmlLevelImpl.XmlLevel) this.loadLevel(leveldef, id));
            }
        } catch (Exception e) {
            throw new IOException(e);
        }

        return new XmlLevelImpl.XmlLevelPack(xlpm, levels);
    }

    private ILevel loadLevel(XmlLevelImpl.XmlLevelMeta xdef, String packId) throws IOException {
        URL lvldata = new URL(this.endpointBase, Common.appendPath(this.endpointBase.getPath(), "data/" + packId + "/" + xdef.ordinal));

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
