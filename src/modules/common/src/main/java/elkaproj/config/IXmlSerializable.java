package elkaproj.config;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Defines that an object can be serialized to XML.
 */
public interface IXmlSerializable {

    /**
     * Serializes the object to XML.
     *
     * @param os Stream to serialize to.
     */
    void serialize(OutputStream os) throws IOException, JAXBException;
}
