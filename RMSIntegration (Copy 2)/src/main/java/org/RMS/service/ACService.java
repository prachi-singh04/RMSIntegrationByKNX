package org.RMS.service;

import org.RMS.Model.AC;
import org.RMS.Model.DeviceType;
import org.RMS.Model.KnxConfig;
import org.RMS.config.XmlDeviceLoader;
import org.RMS.knx.command.KNXCommands;
import org.RMS.knx.impl.KNXGateway;
import org.RMS.knx.thread.KNXCommandWriter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ACService {

    private final KNXCommands knxCommands;
    private final KNXCommandWriter writer;
    private final List<AC> acList;

    private final Map<String, Boolean> powerStateMap = new ConcurrentHashMap<>();
    private final Map<String, Float> temperatureMap = new ConcurrentHashMap<>();
    private final Map<String, Integer> fanSpeedMap = new ConcurrentHashMap<>();


    public ACService(KNXCommands knxCommands,
                     KNXCommandWriter writer,
                     String xmlFilePath) {

        this.knxCommands = Objects.requireNonNull(knxCommands, "KNXCommands cannot be null");
        this.writer = Objects.requireNonNull(writer, "KNX Writer cannot be null");

        KnxConfig config = XmlDeviceLoader.loadConfig(xmlFilePath);
        this.acList = Optional.ofNullable(config)
                .map(KnxConfig::getAcs)
                .orElseThrow(() -> new RuntimeException("No AC devices found in XML config"));
    }

    public boolean isRelayStatusAddress(String ga) {
        return acList.stream()
                .anyMatch(ac ->
                        ac.getPower() != null &&
                                ga.equalsIgnoreCase(ac.getPower().getStatusAddress())
                );
    }

    private void ensurePowerOn(AC ac) {

        String id = ac.getId().toUpperCase();
        boolean isOn = powerStateMap.getOrDefault(id, false);

        if (!isOn) {
            String ga = ac.getPower().getGroupAddress();

            if (StringUtils.isNotBlank(ga)) {
                writer.submit(() ->
                        knxCommands.setACPower(ga, ac.getPower().getOnValue()));

                powerStateMap.put(id, true);
            }
        }
    }

    /* ================= POWER ================= */

    public String turnOn(AC ac) {
        Objects.requireNonNull(ac, "AC cannot be null");

        String ga = ac.getPower().getGroupAddress();
        if (StringUtils.isBlank(ga))
            return "Invalid group address for " + ac.getName();

        writer.submit(() ->
                knxCommands.setACPower(ga, ac.getPower().getOnValue()));

        powerStateMap.put(ac.getId().toUpperCase(), true);
        return ac.getName() + " turned ON";
    }

    public String turnOff(AC ac) {

        Objects.requireNonNull(ac, "AC cannot be null");

        String ga = ac.getPower().getGroupAddress();
        if (StringUtils.isBlank(ga))
            return "Invalid group address for " + ac.getName();

        writer.submit(() ->
                knxCommands.setACPower(ga, ac.getPower().getOffValue()));

        powerStateMap.put(ac.getId().toUpperCase(), false);
        return ac.getName() + " turned OFF";
    }

    public String toggle(AC ac) {
        boolean current = powerStateMap.getOrDefault(ac.getId().toUpperCase(), false);
        return current ? turnOff(ac) : turnOn(ac);
    }

    /* ================= TEMPERATURE ================= */

    public String setTemperatureCelcius(AC ac, float targetTempC) {

        Objects.requireNonNull(ac, "AC cannot be null");

        if (targetTempC < 18 || targetTempC > 28)
            return "Temperature range supported: 18°C to 28°C only.";

        ensurePowerOn(ac);

        String ga = ac.getTemperature().getGroupAddress();

        if (StringUtils.isBlank(ga))
            return "Invalid temperature group address for " + ac.getName();

        writer.submit(() -> knxCommands.setACTemperature(ga, targetTempC));

        temperatureMap.put(ac.getId().toUpperCase(), targetTempC);

        return ac.getName() + " temperature set to " + targetTempC + "°C";
    }


    public String setTemperatureFahrenheit(AC ac, float targetTempF) {

        Objects.requireNonNull(ac, "AC cannot be null");

        if (targetTempF < 64 || targetTempF > 82)
            return "Temperature range supported: 64°F to 82°F only.";

        float targetTempC = (targetTempF - 32) * 5 / 9;

        ensurePowerOn(ac);

        String ga = ac.getTemperature().getGroupAddress();

        if (StringUtils.isBlank(ga))
            return "Invalid temperature group address for " + ac.getName();

        writer.submit(() -> knxCommands.setACTemperature(ga, targetTempC));

        temperatureMap.put(ac.getId().toUpperCase(), targetTempC);

        return ac.getName() + " temperature set to " + targetTempF + "°F";
    }

    /* ================= FAN ================= */

    public String setFanSpeed(AC ac, String ga, int speed) {

        Objects.requireNonNull(ac, "AC cannot be null");

        if (StringUtils.isBlank(ga))
            return "Fan speed group address missing";

        if (speed < 0 || speed > 3)
            return "Fan speed must be between 0 and 3";

        ensurePowerOn(ac);

        writer.submit(() -> knxCommands.setACFanSpeed(ga, speed));

        fanSpeedMap.put(ac.getId().toUpperCase(), speed);

        return ac.getName() + " fan speed set to " + speed;
    }

    public String updateStatusFromTelegram(String statusAddress, Object telegram) {
        for (AC ac : acList) {

            String id = ac.getId().toUpperCase();

            /* ================= POWER ================= */
            if (statusAddress.equalsIgnoreCase(ac.getPower().getStatusAddress())
                    && telegram instanceof Boolean) {

                boolean newState = (Boolean) telegram;
                Boolean oldState = powerStateMap.get(id);

                if (oldState == null || oldState != newState) {
                    powerStateMap.put(id, newState);
                    return "Changes Done: "+ac.getName() + " turned " + (newState ? "ON" : "OFF");
                }

                return null; // no change
            }

            /* ================= TEMPERATURE ================= */
            if (statusAddress.equalsIgnoreCase(ac.getTemperature().getStatusAddress())
                    && telegram instanceof Number) {

                float temp = ((Number) telegram).floatValue();
                Float oldTemp = temperatureMap.get(id);

                if (oldTemp == null || Math.abs(oldTemp - temp) > 0.01f) {
                    temperatureMap.put(id, temp);
                    return "Changes Done: " + ac.getName() +
                            " temperature changed to " + temp + "°C";
                }

                return null;
            }


            /* ================= FAN SPEED ================= */
            if (statusAddress.equalsIgnoreCase(ac.getFan().getStatusAddress())
                    && telegram instanceof Number) {

                int speed = ((Number) telegram).intValue();
                Integer oldSpeed = fanSpeedMap.get(id);

                if (oldSpeed == null || !oldSpeed.equals(speed)) {
                    fanSpeedMap.put(id, speed);
                    return "Changes Done: "+ ac.getName() + " fan speed changed to " + speed;
                }

                return null; // no change
            }
        }

        return null;
    }


    /* ================= STATUS QUERY ================= */

    public void queryAllStatusForAC() {

        for (AC ac : acList) {

            writer.submit(() -> {
                try {

                    String powerGA = ac.getPower().getStatusAddress();
                    if (StringUtils.isNotBlank(powerGA)) {
                        Boolean power = KNXGateway.readBoolean(powerGA);
                        updateStatusFromTelegram(powerGA, power);
                    }

                    String tempGA = ac.getTemperature().getStatusAddress();
                    if (StringUtils.isNotBlank(tempGA)) {
                        Float temp = KNXGateway.readFloat(tempGA);
                        updateStatusFromTelegram(tempGA, temp);
                    }

                    String fanGA = ac.getFan().getStatusAddress();
                    if (StringUtils.isNotBlank(fanGA)) {
                        Integer speed = KNXGateway.readUnsignedByte(fanGA);
                        updateStatusFromTelegram(fanGA, speed);
                    }

                } catch (Exception e) {
                    System.out.println("Status query failed for " + ac.getName());
                }
            });
        }
    }

    public String getFullStatus(String acId) {

        String id = acId.toUpperCase();

        Optional<AC> optionalAC = getACById(acId);

        if (optionalAC.isEmpty()) {
            return "AC with ID " + acId + " not found";
        }

        AC ac = optionalAC.get();

        Boolean power = powerStateMap.get(id);
        Float temp = temperatureMap.get(id);
        Integer fan = fanSpeedMap.get(id);

        if (power == null && temp == null && fan == null) {
            return "No status available for " + ac.getName();
        }

        StringBuilder status = new StringBuilder();

        status.append(ac.getName()).append(" is ");

        // Power
        if (power != null) {
            status.append(power ? "ON" : "OFF");
        } else {
            status.append("Power: UNKNOWN");
        }

        // Temperature
        if (temp != null) {
            status.append(", Temperature: ").append(temp).append("°C");
        }

        // Fan Speed
        if (fan != null) {
            status.append(", Fan Speed: ").append(fan);
        }

        return status.toString();
    }

    public Optional<AC> getACById(String id) {

        if (id == null || id.isBlank())
            return Optional.empty();

        String normalizedId = id.toUpperCase();

        return acList.stream()
                .filter(ac -> ac.getId() != null &&
                        ac.getId().equalsIgnoreCase(normalizedId))
                .findFirst();
    }
}


