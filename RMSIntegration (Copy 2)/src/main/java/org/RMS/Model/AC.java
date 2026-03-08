package org.RMS.Model;
import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class AC extends BaseEntity{

    @XmlAttribute
    private String id;

    @XmlElement
    private String name;

    @XmlElement
    private ACPower power;

    @XmlElement
    private ACFan fan;

    @XmlElement
    private ACTemperature temperature;

    @XmlElement
    private ACUnit unit;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ACPower getPower() {
        return power;
    }

    public ACFan getFan() {
        return fan;
    }

    public ACTemperature getTemperature() {
        return temperature;
    }

    public ACUnit getUnit() {
        return unit;
    }
}

