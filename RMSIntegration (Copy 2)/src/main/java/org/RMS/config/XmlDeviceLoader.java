package org.RMS.config;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import org.RMS.Model.KnxConfig;
import java.io.File;

public class XmlDeviceLoader {
    public static KnxConfig loadConfig(String filePath) {
        try {
            JAXBContext context = JAXBContext.newInstance(KnxConfig.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (KnxConfig) unmarshaller.unmarshal(new File(filePath));
        } catch (Exception e) {
            System.out.println("Failed to load KNX config XML" + e.getMessage());
        }
        return null;
    }
}
