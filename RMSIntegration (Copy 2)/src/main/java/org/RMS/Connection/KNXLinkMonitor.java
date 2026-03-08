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


