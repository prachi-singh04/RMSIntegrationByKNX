package org.RMS.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.RMS.Model.AC;
import org.RMS.service.ACService;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ACController {

    private final ACService service;
    private final ObjectMapper mapper;

    public ACController(ACService service) {
        this.service = service;
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty Print Enabled
    }

    public String handleJsonCommand(String json) {

        try {
            json = json.replaceAll("(?i)\\bNull\\b", "null");

            Map<String, Object> command = mapper.readValue(json, Map.class);

            String id = (String) command.get("id");
            String action = (String) command.get("command");

            if (id == null || action == null) {
                return buildError("Missing id or command");
            }

            return service.getACById(id)
                    .map(ac -> switch (action) {

                        case "on" -> buildSuccess(id, service.turnOn(ac));

                        case "off" -> buildSuccess(id, service.turnOff(ac));

                        case "toggle" -> buildSuccess(id, service.toggle(ac));

                        case "setTempToC" -> {
                            Number temp = (Number) command.get("temperature");
                            if (temp == null)
                                yield buildError("Temperature can't be null. Use valid range in Celsius.");

                            String result = service.setTemperatureCelcius(ac, temp.floatValue());
                            yield result.contains("range")
                                    ? buildError(result)
                                    : buildSuccess(id, result);
                        }

                        case "setTempToF" -> {
                            Number tempF = (Number) command.get("temperature");
                            if (tempF == null)
                                yield buildError("Temperature can't be null. Use valid range in Fahrenheit.");

                            String result = service.setTemperatureFahrenheit(ac, tempF.floatValue());
                            yield result.contains("range")
                                    ? buildError(result)
                                    : buildSuccess(id, result);
                        }

                        case "setFanSpeed" -> {
                            String speed = (String) command.get("speed");
                            if (speed == null || speed.isBlank())
                                yield buildError("Speed can't be null. Use auto, low, medium, or high.");

                            yield setFanSpeed(ac, speed, id);
                        }

                        case "status" -> buildSuccess(id, service.getFullStatus(ac.getId()));

                        default -> buildError("Invalid AC command");
                    })
                    .orElse(buildError("AC with ID " + id + " not found"));

        } catch (Exception e) {
            return buildError("Invalid JSON format");
        }
    }

    private String setFanSpeed(AC ac, String speed, String id) {

        Objects.requireNonNull(ac, "AC cannot be null");

        String ga;
        int value;

        switch (speed.toLowerCase()) {
            case "auto" -> {
                ga = ac.getFan().getAuto().getGroupAddress();
                value = 0;
            }
            case "low" -> {
                ga = ac.getFan().getGroupAddress();
                value = 1;
            }
            case "medium" -> {
                ga = ac.getFan().getGroupAddress();
                value = 2;
            }
            case "high" -> {
                ga = ac.getFan().getGroupAddress();
                value = 3;
            }
            default -> {
                return buildError("Unsupported fan speed. Supported: auto, low, medium, high");
            }
        }

        if (StringUtils.isBlank(ga))
            return buildError("Fan speed group address missing");

        String result = service.setFanSpeed(ac, ga, value);

        return result.contains("missing") || result.contains("must")
                ? buildError(result)
                : buildSuccess(id, result);
    }

    // ✅ JSON Builders (Pretty Printed Automatically)

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