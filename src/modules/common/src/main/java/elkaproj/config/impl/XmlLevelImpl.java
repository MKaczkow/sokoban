package elkaproj.config.impl;

import elkaproj.CastingIterator;
import elkaproj.Dimensions;
import elkaproj.config.ILevel;
import elkaproj.config.ILevelPack;
import elkaproj.config.LevelTile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

class XmlLevelImpl {

    public static class XmlLevel implements ILevel {

        private final int ordinal, bonusTimeThreshold, penaltyTimeThreshold, failTimeThreshold;
        private final String name, originalFile;
        private final Dimensions dimensions;
        private final LevelTile[][] tiles;

        public XmlLevel(XmlLevelMeta xdef, Dimensions dims, LevelTile[][] tiles, String originalFile) {
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

    public static class XmlLevelPack implements ILevelPack {

        private final String name, id;
        private final List<XmlLevel> levels;

        public XmlLevelPack(XmlLevelPackMeta xdef, List<XmlLevel> levels) {
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
            XmlLevelPackMeta meta = new XmlLevelPackMeta();
            meta.id = this.id;
            meta.name = this.name;
            meta.levels = this.levels.stream()
                    .map(x -> {
                        XmlLevelMeta xmeta = new XmlLevelMeta();
                        xmeta.ordinal = x.getOrdinal();
                        xmeta.name = x.getName();
                        xmeta.bonusTime = x.getBonusTimeThreshold();
                        xmeta.penaltyTime = x.getPenaltyTimeThreshold();
                        xmeta.failTime = x.getFailTimeThreshold();
                        xmeta.definitionFile = x.getOriginalFile();
                        return xmeta;
                    })
                    .toArray(XmlLevelMeta[]::new);

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
    public static class XmlLevelPackMeta {

        @XmlAttribute(name = "id")
        public String id;

        @XmlAttribute(name = "name")
        public String name;

        @XmlElement(name = "level")
        public XmlLevelMeta[] levels;
    }

    @XmlRootElement(name = "level")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class XmlLevelMeta {

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
