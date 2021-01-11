package elkaproj.config.impl;

import elkaproj.config.GamePowerup;
import elkaproj.config.IConfiguration;

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
import java.util.EnumSet;
import java.util.List;

class XmlConfigImpl {

    @XmlRootElement(name = "configuration")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class XmlConfiguration implements IConfiguration {

        @XmlElement(name = "level-pack")
        private String levelPackId;

        @XmlElement(name = "max-lives")
        public int maxLives;

        @XmlElement(name = "start-lives")
        public int startLives;

        @XmlElement(name = "life-recovery-threshold")
        public int lifeRecoveryThreshold;

        @XmlElement(name = "life-recovery-count")
        public int lifeRecoveryCount;

        @XmlElement(name = "timers-active")
        private boolean timersActive;

        @XmlElement(name = "active-powerup")
        private List<String> activePowerups;

        private transient EnumSet<GamePowerup> activePowerupsES;

        private XmlConfiguration() {
        }

        @Override
        public String getLevelPackId() {
            return this.levelPackId;
        }

        @Override
        public int getMaxLives() {
            return this.maxLives;
        }

        @Override
        public int getStartingLives() {
            return this.startLives;
        }

        @Override
        public int getLifeRecoveryThreshold() {
            return this.lifeRecoveryThreshold;
        }

        @Override
        public int getLifeRecoveryCount() {
            return this.lifeRecoveryCount;
        }

        @Override
        public boolean areTimersActive() {
            return this.timersActive;
        }

        @Override
        public EnumSet<GamePowerup> getActivePowerups() {
            if (this.activePowerupsES != null)
                return this.activePowerupsES;

            EnumSet<GamePowerup> powerups = EnumSet.noneOf(GamePowerup.class);
            for (String s : this.activePowerups) {
                powerups.add(GamePowerup.valueOf(s));
            }

            return this.activePowerupsES = powerups;
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
