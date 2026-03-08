package org.RMS.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.RMS.Model.Light;
import org.RMS.service.LightService;

import java.util.HashMap;
import java.util.Map;

public class LightController {

    private final LightService service;
    private final ObjectMapper mapper;

    public LightController(LightService service) {
        this.service = service;
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty Print
    }

    public String handleJsonCommand(String json) {

        try {
            json = json.replaceAll("(?i)\\bNull\\b", "null");

            Map<String, Object> command = mapper.readValue(json, Map.class);

            String id = (String) command.get("id");
            String action = (String) command.get("command");
            Integer level = command.containsKey("level")
                    ? (Integer) command.get("level")
                    : null;

            if (id == null || action == null) {
                return buildError("Missing id or command");
            }

            if ("set".equalsIgnoreCase(action) && level == null) {
                return buildError("Missing level for set command");
            }

            return service.getLightById(id)
                    .map(light -> switch (light.getType()) {

                        case RELAY -> handleRelay(light, action);

                        case DIMMER -> handleDimmer(light, action, level);

                        default -> buildError("Unsupported device type");
                    })
                    .orElse(buildError("Light with ID " + id + " not found"));

        } catch (Exception e) {
            return buildError("Invalid JSON format");
        }
    }

    // ================= RELAY =================
    private String handleRelay(Light light, String action) {

        try {

            return switch (action.toLowerCase()) {

                case "on" -> {
                    String result = service.turnOn(light);
                    yield validateAndBuild(light.getId(), result);
                }

                case "off" -> {
                    String result = service.turnOff(light);
                    yield validateAndBuild(light.getId(), result);
                }

                case "toggle" -> {
                    String result = service.toggle(light);
                    yield validateAndBuild(light.getId(), result);
                }

                case "status" -> service.getCurrentState(light.getId())
                        .map(state -> buildSuccess(light.getId(),
                                "Light is " + (state ? "ON" : "OFF")))
                        .orElse(buildError("No state available"));

                default -> buildError("Invalid relay command");
            };

        } catch (Exception e) {
            return buildError("Error handling relay");
        }
    }

    // ================= DIMMER =================
    private String handleDimmer(Light light, String action, Integer level) {

        try {

            return switch (action.toLowerCase()) {

                case "on" -> {
                    String result = service.turnOn(light);
                    yield validateAndBuild(light.getId(), result);
                }

                case "off" -> {
                    String result = service.turnOff(light);
                    yield validateAndBuild(light.getId(), result);
                }

                case "toggle" -> {
                    String result = service.toggle(light);
                    yield validateAndBuild(light.getId(), result);
                }

                case "set" -> {
                    if(level==null){
                        yield buildError("Level can't be null. Use 0-100.");
                    }
                    String result = service.setDimLevel(light, level);
                    yield validateAndBuild(light.getId(), result);
                }

                case "status" -> service.getCurrentDimLevel(light.getId())
                        .map(lvl -> buildSuccess(light.getId(),
                                "Dim level is " + lvl + "%"))
                        .orElse(buildError("No dim level available"));

                default -> buildError("Invalid dimmer command");
            };

        } catch (Exception e) {
            return buildError("Error handling dimmer");
        }
    }

    // ================= COMMON VALIDATION =================
    private String validateAndBuild(String id, String result) {

        if (result == null)
            return buildError("Operation failed");

        String lower = result.toLowerCase();

        if (lower.contains("invalid") ||
                lower.contains("missing") ||
                lower.contains("not")) {

            return buildError(result);
        }

        return buildSuccess(id, result);
    }

    // ================= JSON BUILDERS =================
    private String buildSuccess(String id, String message) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("id", id);
            response.put("message", message);
            return mapper.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"status\":\"error\",\"message\":\"JSON build failed\"}";
        }
    }

    private String buildError(String message) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", message);
            return mapper.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"status\":\"error\",\"message\":\"JSON build failed\"}";
        }
    }
}