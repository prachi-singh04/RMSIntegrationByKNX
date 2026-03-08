package org.RMS.Model;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Gateway {

    @XmlElement(name = "ip")
    private String ip;

    @XmlElement(name = "port")
    private int port;

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
