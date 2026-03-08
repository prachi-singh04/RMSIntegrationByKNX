package org.RMS.Model;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class ACFan extends BaseEntity {

    @XmlElement
    private String type;

    @XmlElement
    private FanSpeed low;

    @XmlElement
    private FanSpeed medium;

    @XmlElement
    private FanSpeed high;

    @XmlElement
    private FanAuto auto;

    public String getType() {
        return type;
    }

    public FanSpeed getLow() {
        return low;
    }

    public FanSpeed getMedium() {
        return medium;
    }

    public FanSpeed getHigh() {
        return high;
    }

    public FanAuto getAuto() {
        return auto;
    }
}

