package org.RMS.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.RMS.Controller.KNXController;
import org.RMS.Connection.KNXConnectionManager;

import java.io.*;
import java.net.Socket;
import java.util.Map;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final TcpKnxServer server;
    private final KNXController controller;
    private final ObjectMapper mapper = new ObjectMapper();

    private PrintWriter writer;

    public ClientHandler(Socket socket, TcpKnxServer server, KNXController controller) {
        this.socket = socket;
        this.server = server;
        this.controller = controller;
    }

    @Override
    public void run() {

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(
                        socket.getOutputStream(), true)
        ) {

            this.writer = writer;
            server.addClient(writer);

            writer.println("Connected to KNX TCP Server");
            writer.println("Enter JSON commands (type 'exit' to quit):\n");

            writer.println("Example for relay : {\"deviceType\":\"light\",\"id\":\"A\",\"command\":\"on\"}");
            writer.println("Example for dimmer : {\"deviceType\":\"light\",\"id\":\"D1\",\"command\":\"set\",\"level\":50}");
            writer.println("Example for AC : {\"deviceType\":\"ac\",\"id\":\"AC1\",\"command\":\"setTempToC\",\"temperature\":24}");

            String line;

            while ((line = reader.readLine()) != null) {

                line = line.trim();

                if (line.isEmpty())
                    continue;

                if ("exit".equalsIgnoreCase(line)) {
                    writer.println("Connection closed.");
                    break;
                }

                if (!line.startsWith("{") || !line.endsWith("}")) {
                    continue;
                }

                System.out.println("Received: " + line);

                if (!KNXConnectionManager.isConnected()) {
                    writer.println(buildError("KNX server disconnected. Please wait..."));
                    continue;
                }

                try {
                    String response = controller.handleJsonCommand(line);

                    // Always send to sender
                    writer.println(response);

                    // Broadcast only valid state changes
                    if (shouldBroadcast(line, response)) {
                        server.broadcastExcept(response, writer);
                    }

                } catch (Exception e) {
                    writer.println(buildError("Invalid JSON"));
                }
            }

        } catch (IOException e) {
            System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());
        } finally {

            if (writer != null) {
                server.removeClient(writer);
            }

            try { socket.close(); } catch (IOException ignored) {}

            System.out.println("Client connection closed: " + socket.getRemoteSocketAddress());
        }
    }

    private boolean shouldBroadcast(String requestJson, String responseJson) {

        if (responseJson == null)
            return false;

        // Do not broadcast errors
        if (responseJson.contains("\"status\"") && responseJson.contains("error"))
            return false;

        try {
            Map<String, Object> request = mapper.readValue(requestJson, Map.class);
            String command = (String) request.get("command");

            if (command == null)
                return false;

            command = command.toLowerCase();

            return switch (command) {
                case "on",
                     "off",
                     "toggle",
                     "set",
                     "settemptoc",
                     "settemptof",
                     "setfanspeed" -> true;
                default -> false;
            };

        } catch (Exception e) {
            return false;
        }
    }

    private String buildError(String message) {
        return "{ \"status\" : \"error\", \"message\" : \"" + message + "\" }";
    }
}