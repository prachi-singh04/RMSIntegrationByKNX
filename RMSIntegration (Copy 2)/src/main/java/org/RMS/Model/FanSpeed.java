package org.RMS.Model;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class FanSpeed extends BaseEntity {

    @XmlElement
    private int value;

    public int getValue() {
        return value;
    }
}
