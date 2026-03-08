package org.RMS.server;

import org.RMS.Connection.KNXConnectionManager;

public class KNXConnectionMonitor implements Runnable {

    private final TcpKnxServer tcpServer;
    private boolean lastStatus;

    public KNXConnectionMonitor(TcpKnxServer tcpServer) {
        this.tcpServer = tcpServer;
        this.lastStatus = KNXConnectionManager.isConnected();
        //real initial status
    }

    @Override
    public void run() {

        while (!Thread.currentThread().isInterrupted()) {

            boolean connected = KNXConnectionManager.isConnected();

            //Only act when state changes
            if (connected != lastStatus) {

                if (!connected) {
                    System.out.println("KNX disconnected!");

                    tcpServer.broadcast(
                            "{\"status\":\"error\",\"message\":\"KNX link detached. Retrying...\"}"
                    );
                } else {
                    System.out.println("KNX reconnected!");

                    tcpServer.broadcast(
                            "{\"status\":\"success\",\"message\":\"KNX link attached. You can send commands.\"}"
                    );
                }

                lastStatus = connected;
            }

            //ry reconnect only if disconnected
            if (!connected) {
                try {
                    KNXConnectionManager.reconnect();

                    // Re-check immediately after reconnect attempt
                    connected = KNXConnectionManager.isConnected();

                    if (connected && !lastStatus) {
                        System.out.println("KNX reconnected after retry!");

                        tcpServer.broadcast(
                                "{\"status\":\"success\",\"message\":\"KNX reconnected successfully.\"}"
                        );

                        lastStatus = true;
                    }

                } catch (Exception e) {
                    System.out.println("Reconnect failed: " + e.getMessage());
                }
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
