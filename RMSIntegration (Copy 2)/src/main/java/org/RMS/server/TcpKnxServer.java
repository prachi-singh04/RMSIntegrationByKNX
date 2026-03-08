package org.RMS.server;

import org.RMS.Controller.KNXController;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.*;

public class TcpKnxServer {

    private final int port;
    private final KNXController controller;
    private final ExecutorService clientPool;

    private static final int MAX_CLIENTS = 15;

    // No username — just client writers
    private final Set<PrintWriter> clients = ConcurrentHashMap.newKeySet();

    private volatile boolean running = true;

    public TcpKnxServer(int port, KNXController controller) {
        this.port = port;
        this.controller = controller;
        this.clientPool = Executors.newFixedThreadPool(MAX_CLIENTS);
    }

    public void start() {

        System.out.println("TCP Server starting on port " + port);

        Thread monitorThread = new Thread(new KNXConnectionMonitor(this));
        monitorThread.setDaemon(true);
        monitorThread.start();

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            while (running) {

                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getRemoteSocketAddress());

                clientPool.submit(new ClientHandler(clientSocket, this, controller));
            }

        } catch (Exception e) {
            System.out.println("TCP Server error: " + e.getMessage());
        }
    }

    public void addClient(PrintWriter writer) {
        clients.add(writer);
    }

    public void removeClient(PrintWriter writer) {
        clients.remove(writer);
    }

    public void broadcastExcept(String message, PrintWriter sender) {

        clients.removeIf(writer -> {
            try {
                if (writer != sender) {
                    writer.println(message);
                    writer.flush();
                }
                return false;
            } catch (Exception e) {
                return true;
            }
        });
    }

    public void broadcast(String message) {

        clients.removeIf(writer -> {
            try {
                writer.println(message);
                writer.flush();
                return false;
            } catch (Exception e) {
                return true; // remove broken client
            }
        });
    }

    public void broadcastKNXStatus(String jsonMessage) {
        broadcast(jsonMessage);
    }
}