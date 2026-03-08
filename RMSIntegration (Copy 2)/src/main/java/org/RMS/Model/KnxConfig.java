package org.RMS.Model;

import jakarta.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "knxDevices")
@XmlAccessorType(XmlAccessType.FIELD)
public class KnxConfig {

    @XmlAttribute(name = "type")
    private String type;

    @XmlElement(name = "gateway")
    private Gateway gateway;

    @XmlElement(name = "light")
    private List<Light> lights;

    @XmlElement(name = "ac")
    private List<AC> acs;

    public Gateway getGateway() {
        return gateway;
    }

    public List<Light> getLights() {
        return lights;
    }

    public String getType() {
        return type;
    }

    public List<AC> getAcs() {
        return acs;
    }
}
