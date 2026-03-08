package org.RMS.Model;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class ACTemperature extends BaseEntity {

    @XmlElement
    private String type;

    @XmlElement
    private int baseSetpoint;

    public String getType() {
        return type;
    }

    public int getBaseSetpoint() {
        return baseSetpoint;
    }
}

