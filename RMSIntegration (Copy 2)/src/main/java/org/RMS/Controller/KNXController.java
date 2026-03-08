package org.RMS.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.HashMap;
import java.util.Map;

public class KNXController {

    private final LightController lightController;
    private final ACController acController;
    private final ObjectMapper mapper;

    public KNXController(LightController lightController, ACController acController) {
        this.lightController = lightController;
        this.acController = acController;
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty Print
    }

    public String handleJsonCommand(String json) {

        try {
            json = json.replaceAll("(?i)\\bNull\\b", "null");

            Map<String, Object> command = mapper.readValue(json, Map.class);

            String deviceType = (String) command.get("deviceType");

            if (deviceType == null) {
                return buildError("Missing deviceType");
            }

            return switch (deviceType.toUpperCase()) {

                case "LIGHT" -> lightController.handleJsonCommand(json);

                case "AC" -> acController.handleJsonCommand(json);

                default -> buildError("Unsupported deviceType");
            };

        } catch (Exception e) {
            return buildError("Invalid JSON");
        }
    }

    // ================= JSON BUILDERS =================

    private String buildSuccess(String message) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
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