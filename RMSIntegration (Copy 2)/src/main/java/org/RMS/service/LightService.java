package org.RMS.service;
import org.RMS.Model.DeviceType;
import org.RMS.Model.Light;
import org.RMS.Model.KnxConfig;
import org.RMS.config.XmlDeviceLoader;
import org.RMS.knx.command.KNXCommands;
import org.RMS.knx.impl.KNXGateway;
import org.RMS.knx.thread.KNXCommandWriter;
import org.apache.commons.lang3.StringUtils;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LightService {

    private final KNXCommands knxCommands;
    private final KNXCommandWriter writer;
    private final List<Light> lights;

    private final Map<String, Boolean> lightStateMap = new ConcurrentHashMap<>();
    private final Map<String, Integer> dimLevelMap = new ConcurrentHashMap<>();


    public LightService(KNXCommands knxGateway,
                        KNXCommandWriter writer,
                        String xmlFilePath) {

        this.knxCommands = Objects.requireNonNull(knxGateway, "KNX Gateway cannot be null");
        this.writer = Objects.requireNonNull(writer, "KNX Writer cannot be null");

        KnxConfig config = XmlDeviceLoader.loadConfig(xmlFilePath);
        this.lights = Optional.ofNullable(config)
                .map(KnxConfig::getLights)
                .orElseThrow(() -> new RuntimeException("No lights found in XML config"));

        lights.forEach(l -> {
            lightStateMap.put(l.getId().toUpperCase(), false);
            dimLevelMap.put(l.getId().toUpperCase(), 0);
        });
    }

    public boolean isRelayStatusAddress(String ga) {
        return lights.stream()
                .anyMatch(l -> l.getType() == DeviceType.RELAY &&
                        ga.equalsIgnoreCase(l.getStatusAddress()));
    }


    public String turnOn(Light light) {

        Objects.requireNonNull(light, "Light cannot be null");

        if (light.getType() == DeviceType.DIMMER) {
            return setDimLevel(light, 100);
        }

        String ga = light.getGroupAddress();
        if (StringUtils.isBlank(ga)) {
            return "Invalid group address for " + light.getName();
        }

        writer.submit(() -> knxCommands.switchOn(ga));

        lightStateMap.put(light.getId().toUpperCase(), true);
        return light.getName() + " turned ON";
    }

    public String turnOff(Light light) {

        Objects.requireNonNull(light, "Light cannot be null");

        if (light.getType() == DeviceType.DIMMER) {
            return setDimLevel(light, 0);
        }

        String ga = light.getGroupAddress();
        if (StringUtils.isBlank(ga)) {
            return "Invalid group address for " + light.getName();
        }

        writer.submit(() -> knxCommands.switchOff(ga));

        lightStateMap.put(light.getId().toUpperCase(), false);
        return light.getName() + " turned OFF";
    }

    public String toggle(Light light) {

        if (light.getType() == DeviceType.DIMMER) {
            int current = dimLevelMap.getOrDefault(light.getId().toUpperCase(), 0);
            return setDimLevel(light, current > 0 ? 0 : 100);
        }

        boolean current = lightStateMap.getOrDefault(light.getId().toUpperCase(), false);

        return current ? turnOff(light) : turnOn(light);
    }

    public String setDimLevel(Light light, int level) {

        Objects.requireNonNull(light, "Light cannot be null");

        if (light.getType() != DeviceType.DIMMER) {
            return "Light " + light.getName() + " is not a dimmer.";
        }

        String ga = light.getGroupAddress();
        if (StringUtils.isBlank(ga)) {
            return "Invalid group address for " + light.getName();
        }

        if (level < 0 || level > 100) {
            return "Invalid dim level. Use 0–100.";
        }

        writer.submit(() -> knxCommands.setDimLevel(ga, level));

        dimLevelMap.put(light.getId().toUpperCase(), level);
        lightStateMap.put(light.getId().toUpperCase(), level > 0);

        return light.getName() + " dim level set to " + level + "%";
    }

    public Optional<Light> getLightById(String id) {
        return lights.stream()
                .filter(l -> l.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    public Optional<Boolean> getCurrentState(String lightId) {
        return Optional.ofNullable(lightStateMap.get(lightId.toUpperCase()));
    }

    public Optional<Integer> getCurrentDimLevel(String lightId) {
        return Optional.ofNullable(dimLevelMap.get(lightId.toUpperCase()));
    }

    public String updateStatusFromTelegram(String statusAddress, Object telegram) {

        return lights.stream()
                .filter(l -> statusAddress.equalsIgnoreCase(l.getStatusAddress()))
                .findFirst()
                .map(light -> {

                    String id = light.getId().toUpperCase();

                    if (light.getType() == DeviceType.RELAY && telegram instanceof Boolean) {

                        boolean newState = (Boolean) telegram;
                        Boolean oldState = lightStateMap.get(id);

                        if (oldState == null || oldState != newState) {
                            lightStateMap.put(id, newState);
                            return "Changes Done: "+ light.getName() + " turned " + (newState ? "ON" : "OFF");
                        }
                    }

                    if (light.getType() == DeviceType.DIMMER && telegram instanceof Integer) {

                        int raw = (Integer) telegram;

                        // Convert 0–255 to 0–100%
                        int percent = Math.round((raw / 255f) * 100);

                        // Safety clamp
                        if (percent < 0) percent = 0;
                        if (percent > 100) percent = 100;

                        Integer oldLevel = dimLevelMap.get(id);

                        if (oldLevel == null || !oldLevel.equals(percent)) {
                            dimLevelMap.put(id, percent);
                            lightStateMap.put(id, percent > 0);
                            return "Changes Done: " + light.getName() +
                                    " dim level updated to " + percent + "%";
                        }
                    }

                    return null;
                })
                .orElse(null);
    }

    // Query initial status using writer thread (non-blocking TCP)
    public void queryAllStatusForLight() {

        for (Light light : lights) {

            writer.submit(() -> {
                try {
                    if (light.getType() == DeviceType.RELAY) {
                        boolean state = KNXGateway.readBoolean(light.getStatusAddress());
                        updateStatusFromTelegram(light.getStatusAddress(), state);
                    }
                    else if (light.getType() == DeviceType.DIMMER) {
                        Integer level = KNXGateway.readDimLevel(light.getStatusAddress());
                        updateStatusFromTelegram(light.getStatusAddress(), level);
                    }
                } catch (Exception e) {
                    System.out.println("KNX read failed for " + light.getId());
                }
            });
        }
    }

}

