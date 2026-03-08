package org.RMS.Model;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class ACPower extends BaseEntity {

    @XmlElement
    private String type;

    @XmlElement
    private int onValue;

    @XmlElement
    private int offValue;

    public String getType() {
        return type;
    }

    public int getOnValue() {
        return onValue;
    }

    public int getOffValue() {
        return offValue;
    }
}

