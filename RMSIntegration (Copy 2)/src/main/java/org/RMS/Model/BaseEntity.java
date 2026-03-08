package org.RMS.Model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;


@XmlAccessorType(XmlAccessType.FIELD)
public class BaseEntity {
    @XmlElement(name = "groupAddress")
    private String groupAddress;

    @XmlElement(name = "statusAddress")
    private String statusAddress;

    public String getStatusAddress() {
        return statusAddress;
    }

    public String getGroupAddress() {
        return groupAddress;
    }
}
