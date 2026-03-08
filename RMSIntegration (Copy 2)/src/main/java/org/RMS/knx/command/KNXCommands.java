package org.RMS.knx.command;

public interface KNXCommands {
    boolean switchOn(String groupAddress);
    boolean switchOff(String groupAddress);
    boolean setDimLevel(String groupAddress, int level);//0-100

    boolean setACPower(String groupAddress, int value);     // 1 = ON, 4 = OFF
    boolean setACFanSpeed(String groupAddress, int speed);  // 0=Auto,1=Low,2=Med,3=High
    boolean setACTemperature(String groupAddress, float temperature);// e.g. 18–30
}

