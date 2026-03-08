package org.RMS.knx.thread;

import io.calimero.dptxlator.DPTXlator2ByteFloat;
import org.RMS.Connection.KNXConnectionManager;
import org.RMS.service.KNXService;
import io.calimero.DetachEvent;
import io.calimero.process.ProcessCommunicator;
import io.calimero.process.ProcessEvent;
import io.calimero.process.ProcessListener;
import org.RMS.server.TcpKnxServer;

public class KNXStatusReader implements Runnable, ProcessListener {

    private KNXService service;
    private ProcessCommunicator communicator;
    private volatile boolean running = true;
    private TcpKnxServer tcpServer;
    private volatile boolean knxConnected = true;


    public KNXStatusReader(KNXService service, TcpKnxServer tcpServer) {
        this.service = service;
        this.tcpServer = tcpServer;
        attachListener();
    }


    //to attach the listener to the KNX communicator when the application starts
    private synchronized void attachListener() {
        try {
            communicator = KNXConnectionManager.getCommunicator();
            communicator.addProcessListener(this);
            System.out.println("KNX Status Reader attached.");
        } catch (Exception e) {
            System.out.println("Failed to attach KNX listener: " + e.getMessage());
        }
    }

    //to detach the listener when the connection is lost or when the application is shutting down
    private synchronized void detachListener() {
        try {
            if (communicator != null) {
                communicator.removeProcessListener(this);
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void run() {
        Thread.currentThread().setName("KNXReader-Thread");
        while (running) {
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                System.out.println("Error caused in Reader Thread " + e.getMessage());
            }
        }
    }

    private synchronized void reconnect() {
        try {
            detachListener();
            System.out.println("Trying to reconnect...");
            KNXConnectionManager.reconnect();
            attachListener();
            knxConnected = true;
            System.out.println("Reconnected successfully");
            tcpServer.broadcastKNXStatus(
                    "{\"status\":\"success\",\"message\":\"KNX connection established. You can send commands.\"}"
            );
        } catch (Exception e) {
            System.out.println("Reconnection failed: " + e.getMessage());
        }
    }



    @Override
    //before the write is executed, the read request is sent to the device, and the response is received in this method.
    public void groupReadResponse(ProcessEvent e) {
        handleIncoming(e);
    }

    @Override
    //after the write request is sent to the device, the response is received in this method.
    public void groupWrite(ProcessEvent e) {
        handleIncoming(e);
    }

    @Override
    //after the read request is sent to the device, the response is received in this method.
    public void groupReadRequest(ProcessEvent e) {
        handleIncoming(e);
    }


    //after the write request is sent to the device, the response is received in this method.
    /*private void handleIncoming(ProcessEvent e) {
        String ga = e.getDestination().toString();//returns the group address as string
        byte[] data = e.getASDU();//returns the raw data bytes
        boolean state = data.length > 0 && (data[data.length - 1] & 0x01) == 1;//returns the last byte as boolean state
        service.updateStateByStatusAddress(ga, state);
    }*/

    private void handleIncoming(ProcessEvent e) {
        String ga = e.getDestination().toString();
        byte[] data = e.getASDU();
        Object value = null;

        /*if (data != null && data.length > 0) {
            int lastByte = data[data.length - 1] & 0xFF;//ise sirf last byte ko unsigned int me convert karega taki 0-255 range me value mile
            // 1-bit values for power, auto
            if (data.length == 1) {
                value = (lastByte & 0x01) == 1; //ise sirf last value ka 1st bit check karega jo power ya auto ke liye hoga
            }
            // 1-byte values for fan speed, temperature delta
            else {
                value = lastByte;
            }
        }*/

        if (data != null && data.length > 0) {

            // 1-bit (ON/OFF)
            /*if (data.length == 1) {
                int unsigned = data[0] & 0xFF;
                value = (unsigned & 0x01) == 1;
            }*/

            if (data.length == 1) {

                int unsigned = data[0] & 0xFF;

                // First check if this GA belongs to a relay
                if (service.isRelayAddress(ga)) {
                    value = (unsigned & 0x01) == 1;   // Boolean
                }
                else {
                    value = unsigned;                // Dimmer 0–255
                }
            }


            // 2-byte float (Temperature - DPT 9.xxx)
            /*else if (data.length == 2) {
                try {
                    int msb = data[0] & 0xFF;
                    int lsb = data[1] & 0xFF;

                    int raw = (msb << 8) | lsb;

                    int sign = (raw & 0x8000) >> 15;
                    int exponent = (raw & 0x7800) >> 11;
                    int mantissa = raw & 0x07FF;

                    if (sign == 1) {
                        mantissa = -(~(mantissa - 1) & 0x07FF);
                    }

                    float temperature = (float) (0.01 * mantissa * Math.pow(2, exponent));
                    value = temperature;

                } catch (Exception ex) {
                    System.out.println("Temperature decode failed");
                }
            }*/

            else if (data.length == 2) {
                try {
                    DPTXlator2ByteFloat translator =
                            new DPTXlator2ByteFloat(
                                    DPTXlator2ByteFloat.DPT_TEMPERATURE
                            );

                    translator.setData(data);
                    value = translator.getValue();

                } catch (Exception ex) {
                    System.out.println("Temperature decode failed: " + ex.getMessage());
                }
            }


            // 1-byte unsigned (fan speed, dim level)
            else {
                value = data[data.length - 1] & 0xFF;
            }
        }

        /*boolean handled = service.updateStatusFromTelegram(ga, value);

        if (handled) {
            System.out.println("Physical change detected at GA " + ga + " → Value: " + value);
        }*/

        //System.out.println("GA: " + ga + " Value: " + value + " Type: " + value.getClass());

        // Update internal state and get message to broadcast
        String message = service.updateStatusFromTelegram(ga, value);

        if (message != null) {

            System.out.println(message);
            //Broadcast to all connected TCP clients
            tcpServer.broadcast(message);
        }

    }

    /*@Override
    //Informs about the detaching of an object from a KNX link and then take current source
    public void detached(DetachEvent e) {
        System.out.println("KNX link detached. Will reconnect");

        tcpServer.broadcast(
                "{\"status\":\"error\",\"message\":\"KNX connection lost. Reconnecting...\"}"
        );
        reconnect();
    }*/

    @Override
    public synchronized void detached(DetachEvent e) {

        if (!knxConnected) return;   // avoid duplicate trigger

        knxConnected = false;

        System.out.println("KNX link detached. Will reconnect");

        tcpServer.broadcastKNXStatus(
                "{\"status\":\"error\",\"message\":\"KNX connection lost. Reconnecting...\"}"
        );
        reconnect();
    }

    //to stop the thread and detach the listener when the application is shutting down
    public void stop() {
        running = false;
        detachListener();
    }

}

