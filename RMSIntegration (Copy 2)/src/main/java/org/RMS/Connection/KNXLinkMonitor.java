package org.RMS.Connection;
public class KNXLinkMonitor implements Runnable {
    private volatile boolean running = true;

    @Override
    public void run() {
        while (running) {
            try {
                if (!KNXConnectionManager.isConnected()) {
                    System.out.println("KNX disconnected. Trying to reconnect...");
                    KNXConnectionManager.reconnect();
                }
                Thread.sleep(1000); // check every 1 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stop() {
        running = false;
    }

}



/*
package org.RMS.Connection;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.process.ProcessCommunicator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KNXLinkMonitor {

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private volatile boolean reconnecting = false;

    public void start() {

        scheduler.scheduleAtFixedRate(() -> {

            try {
                if (!KNXConnectionManager.isConnected()) {
                    triggerReconnect("KNX not connected");
                    return;
                }

                // ACTIVE TEST READ (Fast Failure Detection)
                ProcessCommunicator communicator =
                        KNXConnectionManager.getCommunicator();

                // Use any stable status GA from your config
                System.out.println("####################################");
                communicator.readBool(new GroupAddress("1/0/2"));
                System.out.println("####################################");

            } catch (Exception e) {
                System.out.println("exception: ####################################");
                triggerReconnect("KNX link lost (read failed)");

            }

        }, 5, 5, TimeUnit.SECONDS); // check every 5 sec
    }

    private void triggerReconnect(String reason) {

        if (reconnecting) return;

        reconnecting = true;

        System.out.println(reason + ". Reconnecting immediately...");

        try {
            KNXConnectionManager.reconnect();
        } catch (Exception ex) {
            System.out.println("Reconnect failed: " + ex.getMessage());
        } finally {
            reconnecting = false;
        }
    }

    public void stop() {
        scheduler.shutdownNow();
    }
}
*/



