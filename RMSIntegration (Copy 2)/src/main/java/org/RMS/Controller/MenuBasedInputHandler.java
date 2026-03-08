/*package org.RMS.Controller;
import java.util.Scanner;

public class MenuBasedInputHandler {

    private final Scanner scanner;

    public MenuBasedInputHandler(Scanner scanner) {
        this.scanner = scanner;
    }

    public String getJsonFromMenu() {

        System.out.println("\nSelect Device:");
        System.out.println("1. LIGHT");
        System.out.println("2. AC");
        System.out.println("Type 'exit' to quit");
        System.out.print("Enter choice: ");

        String deviceChoice = scanner.nextLine().trim();

        if ("exit".equalsIgnoreCase(deviceChoice)) {
            return "exit";
        }

        switch (deviceChoice) {
            case "1":
                return buildLightJson();
            case "2":
                return buildACJson();
            default:
                System.out.println("Invalid choice.");
                return null;
        }
    }

    private String buildLightJson() {
        System.out.print("Enter Light ID: ");
        String id = scanner.nextLine().trim().toUpperCase();

        System.out.println("Commands:");
        System.out.println("1. ON");
        System.out.println("2. OFF");
        System.out.println("3. TOGGLE");
        System.out.println("4. STATUS");
        System.out.println("5. SET DIM LEVEL");
        System.out.print("Enter choice: ");

        String cmd = scanner.nextLine().trim();

        switch (cmd) {
            case "1":
                return "{\"deviceType\":\"LIGHT\",\"id\":\"" + id + "\",\"command\":\"on\"}";
            case "2":
                return "{\"deviceType\":\"LIGHT\",\"id\":\"" + id + "\",\"command\":\"off\"}";
            case "3":
                return "{\"deviceType\":\"LIGHT\",\"id\":\"" + id + "\",\"command\":\"toggle\"}";
            case "4":
                return "{\"deviceType\":\"LIGHT\",\"id\":\"" + id + "\",\"command\":\"status\"}";
            case "5":
                System.out.print("Enter level (0-100): ");
                String level = scanner.nextLine().trim();
                return "{\"deviceType\":\"LIGHT\",\"id\":\"" + id + "\",\"command\":\"set\",\"level\":" + level + "}";
            default:
                System.out.println("Invalid command.");
                return null;
        }
    }

    private String buildACJson() {
        System.out.print("Enter AC ID: ");
        String id = scanner.nextLine().trim();

        System.out.println("Commands:");
        System.out.println("1. ON");
        System.out.println("2. OFF");
        System.out.println("3. TOGGLE");
        System.out.println("4. STATUS");
        System.out.println("5. SET TEMP (°C)");
        System.out.println("6. SET TEMP (°F)");
        System.out.println("7. SET FAN SPEED (auto/low/medium/high)");
        System.out.print("Enter choice: ");

        String cmd = scanner.nextLine().trim();

        switch (cmd) {
            case "1":
                return "{\"deviceType\":\"AC\",\"id\":\"" + id + "\",\"command\":\"on\"}";
            case "2":
                return "{\"deviceType\":\"AC\",\"id\":\"" + id + "\",\"command\":\"off\"}";
            case "3":
                return "{\"deviceType\":\"AC\",\"id\":\"" + id + "\",\"command\":\"toggle\"}";
            case "4":
                return "{\"deviceType\":\"AC\",\"id\":\"" + id + "\",\"command\":\"status\"}";
            case "5":
                System.out.print("Enter temperature in °C: ");
                String tempC = scanner.nextLine().trim();
                return "{\"deviceType\":\"AC\",\"id\":\"" + id + "\",\"command\":\"setTempToC\",\"temperature\":" + tempC + "}";
            case "6":
                System.out.print("Enter temperature in °F: ");
                String tempF = scanner.nextLine().trim();
                return "{\"deviceType\":\"AC\",\"id\":\"" + id + "\",\"command\":\"setTempToF\",\"temperature\":" + tempF + "}";
            case "7":
                System.out.print("Enter speed (auto/low/medium/high): ");
                String speed = scanner.nextLine().trim();
                return "{\"deviceType\":\"AC\",\"id\":\"" + id + "\",\"command\":\"setFanSpeed\",\"speed\":\"" + speed + "\"}";
            default:
                System.out.println("Invalid command.");
                return null;
        }
    }
}*/
