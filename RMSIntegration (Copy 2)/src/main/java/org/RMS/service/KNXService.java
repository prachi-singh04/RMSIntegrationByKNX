package org.RMS.service;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KNXService {

    private LightService lightService;
    private ACService acService;

    // Dedicated executor for status sync
    private final ExecutorService statusExecutor =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "KNX-Status-Sync");
                t.setDaemon(true);
                return t;
            });

    public KNXService(LightService lightService, ACService acService) {
        this.lightService = lightService;
        this.acService = acService;
    }

    public boolean isRelayAddress(String ga) {
        return lightService != null && lightService.isRelayStatusAddress(ga) || acService != null && acService.isRelayStatusAddress(ga);
    }


    public String updateStatusFromTelegram(String statusAddress, Object telegram) {

        if (lightService != null) {
            String msg = lightService.updateStatusFromTelegram(statusAddress, telegram);
            if (msg != null) return msg;
        }

        if (acService != null) {
            String msg = acService.updateStatusFromTelegram(statusAddress, telegram);
            if (msg != null) return msg;
        }

        return null;
    }

    public void queryAllStatus() {

        statusExecutor.submit(() -> {
            try {

                if (lightService != null) {
                    lightService.queryAllStatusForLight();
                }

                if (acService != null) {
                    acService.queryAllStatusForAC();
                }

            } catch (Exception e) {
                System.out.println("KNX status sync failed: " + e.getMessage());
            }
        });
    }

    // Only shutdown what this class owns
    public void shutdown() {
        statusExecutor.shutdownNow();
    }
}

