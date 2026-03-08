package org.RMS.server;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.*;

public class TcpKnxClient {

    private final String host;
    private final int port;

    private volatile boolean running = true;

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private final BufferedReader consoleReader =
            new BufferedReader(new InputStreamReader(System.in));

    public TcpKnxClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {

        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleWithFixedDelay(() -> {

            if (socket == null) {
                try {

                    socket = new Socket();
                    socket.connect(new InetSocketAddress(host, port), 3000);

                    writer = new PrintWriter(socket.getOutputStream(), true);
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    System.out.println("Connected to server: " + host + ":" + port);

                    startReaderThread();

                } catch (Exception e) {
                    System.out.println("Server not available. Retrying in 5 seconds...");
                    socket = null;
                }
            }

        }, 0, 5, TimeUnit.SECONDS);


        while (running) {
            try {

                if (writer != null) {

                    String input = consoleReader.readLine();

                    if (input == null || "exit".equalsIgnoreCase(input)) {
                        running = false;
                        closeConnection();
                        break;
                    }

                    writer.println(input);

                } else {
                    Thread.sleep(1000);
                }

            } catch (Exception e) {
                closeConnection();
            }
        }

        scheduler.shutdown();
    }

    private void startReaderThread() {

        Thread readerThread = new Thread(() -> {

            try {

                String line;

                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }

                System.out.println("Server connection lost.");
                closeConnection();

            } catch (IOException e) {
                System.out.println("Disconnected from server.");
                closeConnection();
            }

        });

        readerThread.setDaemon(true);
        readerThread.start();
    }

    private synchronized void closeConnection() {

        try {
            if (socket != null)
                socket.close();
        } catch (IOException ignored) {}

        socket = null;
        writer = null;
        reader = null;
    }

    public static void main(String[] args) {

        String host = args.length > 0 ? args[0] : "10.75.0.118";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5000;

        new TcpKnxClient(host, port).start();
    }
}