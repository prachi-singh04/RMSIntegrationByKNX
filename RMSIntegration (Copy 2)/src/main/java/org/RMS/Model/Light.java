package org.RMS.Model;
import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Light extends BaseEntity{

    @XmlAttribute(name = "id")
    private String id;

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "dpt")
    private String dpt;

    @XmlElement(name = "type")
    private DeviceType type;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDpt() {
        return dpt;
    }

    public DeviceType getType() {
        return type;
    }
}



