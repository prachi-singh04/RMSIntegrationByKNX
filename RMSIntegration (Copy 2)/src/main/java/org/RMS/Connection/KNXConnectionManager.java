package org.RMS.Connection;
import io.calimero.link.KNXNetworkLink;
import io.calimero.link.KNXNetworkLinkIP;
import io.calimero.process.ProcessCommunicator;
import io.calimero.process.ProcessCommunicatorImpl;
import io.calimero.link.medium.TPSettings;

import java.net.InetSocketAddress;

public class KNXConnectionManager {

    private static KNXNetworkLink link;
    private static ProcessCommunicator pc;

    private static String gatewayIp;
    private static int gatewayPort;

    private static boolean reconnecting = false;

    public static void init(String ip, int port) {
        gatewayIp = ip;
        gatewayPort = port;
    }

    public static synchronized void connect() {
        try {
            if (link != null && link.isOpen()) {
                return;
            }

            disconnect();   // ensure clean state

            System.out.println("Connecting to KNX/IP Gateway...");

            InetSocketAddress local = new InetSocketAddress(0);
            InetSocketAddress remote = new InetSocketAddress(gatewayIp, gatewayPort);

            link = KNXNetworkLinkIP.newTunnelingLink(
                    local,
                    remote,
                    false,
                    new TPSettings()
            );

            pc = new ProcessCommunicatorImpl(link);

            System.out.println("Connected to KNX/IP Gateway");

        } catch (Exception e) {
            System.out.println("Failed to connect to KNX: " + e.getMessage());
        }
    }

    public static synchronized void reconnect() {
        if (reconnecting) {
            return;
        }
        reconnecting = true;

        try {
            System.out.println("Reconnecting to KNX/IP Gateway...");
            disconnect();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}

            connect();

        } finally {
            reconnecting = false;
        }
    }

    public static ProcessCommunicator getCommunicator() {
        if (link == null || !link.isOpen() || pc == null) {
            System.out.println("KNX connection not established. Trying to connect...");
            connect();
        }
        return pc;
    }

    public static boolean isConnected() {
        return link != null && link.isOpen() && pc != null;
    }

    public static synchronized void disconnect() {
        try {
            if (pc != null) {
                pc.detach();
                pc.close();
            }

            if (link != null && link.isOpen()) {
                link.close();
                System.out.println("Disconnected from KNX/IP Gateway");
            }

        } catch (Exception e) {
            System.out.println("Error during KNX disconnect: " + e.getMessage());
        } finally {
            pc = null;
            link = null;
        }
    }
}
