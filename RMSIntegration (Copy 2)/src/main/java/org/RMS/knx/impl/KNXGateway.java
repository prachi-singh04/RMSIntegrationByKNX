package org.RMS.knx.impl;
import io.calimero.dptxlator.DPTXlator2ByteFloat;
import io.calimero.dptxlator.DPTXlator8BitUnsigned;
import org.RMS.Connection.KNXConnectionManager;
import org.RMS.knx.command.KNXCommands;
import io.calimero.GroupAddress;
import io.calimero.KNXException;
import io.calimero.process.ProcessCommunicator;

public class KNXGateway implements KNXCommands {

    private static ProcessCommunicator communicator;

    public KNXGateway() {
        refreshCommunicator();
    }

    private static void refreshCommunicator() {
        communicator = KNXConnectionManager.getCommunicator();
    }

    @Override
    public boolean switchOn(String groupAddress) {
        return writeBoolean(groupAddress, true);
    }

    @Override
    public boolean switchOff(String groupAddress) {
        return writeBoolean(groupAddress, false);
    }


    //Write dim level (0–100) using DPT 5.001 (1 byte)
    @Override
    public boolean setDimLevel(String groupAddress, int level) {
        try {
            refreshCommunicator();

            if (level < 0 || level > 100) {
                System.out.println("Dim level must be between 0–100");
                return false;
            }

            DPTXlator8BitUnsigned translator =
                    new DPTXlator8BitUnsigned(DPTXlator8BitUnsigned.DPT_SCALING);

            translator.setValue(level);   // 0–100 directly

            communicator.write(new GroupAddress(groupAddress), translator);

            return true;

        } catch (Exception e) {
            System.out.println("KNX dim write failed at " + groupAddress + ": " + e.getMessage());
            return false;
        }
    }


    //for ac power control-dp1 1.001 is boolean
    @Override
    public boolean setACPower(String groupAddress, int value) {
        boolean isOn; // KNX 1 = ON, 4 = OFF but treating any non-1 value as OFF for simplicity
        if (value == 1) {
            isOn = true;
        } else {
            isOn = false;
        }
        return writeBoolean(groupAddress, isOn); //convert the int value to boolean before writing
    }

    @Override
    public boolean setACTemperature(String groupAddress, float temperature) {
        try {
            refreshCommunicator();

            DPTXlator2ByteFloat translator =
                    new DPTXlator2ByteFloat(DPTXlator2ByteFloat.DPT_TEMPERATURE);

            translator.setValue(temperature);

            communicator.write(new GroupAddress(groupAddress), translator);

            return true;

        } catch (Exception e) {
            System.out.println("KNX temperature write failed at " + groupAddress + ": " + e.getMessage());
            return false;
        }
    }


    @Override
    public boolean setACFanSpeed(String groupAddress, int speed) {
        return writeUnsignedByte(groupAddress, speed);
    }

    private static boolean writeUnsignedByte(String ga, int value) {
        try {
            refreshCommunicator();
            communicator.write(new GroupAddress(ga), value, "5.004"); // unsigned 1-byte
            return true;
        } catch (Exception e) {
            System.out.println("KNX write failed at " + ga + ": " + e.getMessage());
            return false;
        }

    }

    // Write boolean value
    public static boolean writeBoolean(String ga, boolean value) {
        try {
            refreshCommunicator();
            communicator.write(new GroupAddress(ga), value);
            return true;
        } catch (Exception e) {
            System.out.println("KNX write failed at " + ga + ": " + e.getMessage());
            return false;
        }

    }

    // Read current state of relay light
    public static boolean readBoolean(String ga){
        try {
            refreshCommunicator();
            return communicator.readBool(new GroupAddress(ga));
        } catch (Exception e) {
            System.out.println("KNX read failed at " + ga);
            return false;
        }
    }

    // read current dim level (0–100) of dimmer light using DPT 5.001 (1 byte)
    public static Integer readDimLevel(String ga) {
        try {
            refreshCommunicator();

            short raw = (short) communicator.readUnsigned(new GroupAddress(ga), "5.004");

            int percent = Math.round((raw / 255f) * 100);

            return percent;

        } catch (Exception e) {
            System.out.println("KNX dim read failed at " + ga + ": " + e.getMessage());
            return null;
        }
    }

    public static Float readFloat(String ga) {
        try {
            refreshCommunicator();

            double value = communicator.readFloat(new GroupAddress(ga));

            return (float) value;

        } catch (Exception e) {
            System.out.println("KNX float read failed at " + ga + ": " + e.getMessage());
            return null;
        }
    }

    // Read unsigned 1-byte (DPT 5.004) – used for fan speed
    public static Integer readUnsignedByte(String ga) {
        try {
            refreshCommunicator();
            return communicator.readUnsigned(new GroupAddress(ga), "5.004");
        } catch (Exception e) {
            System.out.println("KNX read failed at " + ga + ": " + e.getMessage());
            return null;
        }
    }
}

