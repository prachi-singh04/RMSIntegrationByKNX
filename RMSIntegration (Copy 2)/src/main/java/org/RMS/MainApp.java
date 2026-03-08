package org.RMS;
import org.RMS.Connection.KNXConnectionManager;
import org.RMS.Connection.KNXLinkMonitor;
import org.RMS.Controller.ACController;
import org.RMS.Controller.KNXController;
import org.RMS.Controller.LightController;
import org.RMS.Model.KnxConfig;
import org.RMS.config.XmlDeviceLoader;
import org.RMS.knx.command.KNXCommands;
import org.RMS.knx.impl.KNXGateway;
import org.RMS.knx.thread.KNXCommandWriter;
import org.RMS.knx.thread.KNXStatusReader;
import org.RMS.server.KNXConnectionMonitor;
import org.RMS.server.TcpKnxServer;
import org.RMS.service.ACService;
import org.RMS.service.KNXService;
import org.RMS.service.LightService;

public class MainApp {

    public static void main(String[] args) throws Exception {

        // ================= LOAD CONFIG =================
        //String xmlPath = "src/main/java/org/RMS/config/knx-light-relay.xml";
        String xmlPath = args.length > 0 ? args[0] : "knx-light-relay.xml";
        KnxConfig config = XmlDeviceLoader.loadConfig(xmlPath);

        // ================= CONNECT KNX =================

        KNXConnectionManager.init(
                config.getGateway().getIp(),
                config.getGateway().getPort()
        );
        KNXConnectionManager.connect();

        // ================= START KNX LINK MONITOR =================
        KNXLinkMonitor monitor = new KNXLinkMonitor();
        Thread monitorThread = new Thread(monitor, "KNX-Link-Monitor-Thread");
        //monitorThread.setDaemon(true); // optional: allows JVM to exit if main ends
        monitorThread.start();

        // ================= INIT GATEWAY =================

        KNXCommands gateway = new KNXGateway();

        // ================= INIT WRITER =================

        KNXCommandWriter writer = new KNXCommandWriter(gateway);
        Thread writerThread = new Thread(writer, "KNX-Writer-Thread");
        writerThread.start();

        // ================= INIT SERVICES =================

        LightService lightService = new LightService(gateway, writer, xmlPath);

        ACService acService = new ACService(gateway, writer, xmlPath);

        KNXService service = new KNXService(lightService, acService);

        // ================= INIT CONTROLLERS =================

        LightController lightController = new LightController(lightService);

        ACController acController = new ACController(acService);

        KNXController controller = new KNXController(lightController, acController);

        // ================= START TCP SERVER =================
        int tcpPort = 5000;
        TcpKnxServer tcpServer = new TcpKnxServer(tcpPort, controller);

        Thread serverThread = new Thread(tcpServer::start, "TCP-KNX-Server-Thread");
        serverThread.start();

        // ================= START KNX READER =================
        KNXStatusReader reader = new KNXStatusReader(service, tcpServer);

        Thread readerThread = new Thread(reader, "KNX-Reader-Thread");
        readerThread.start();

        // ================= INITIAL STATUS SYNC (ASYNC) =================
        Thread syncThread = new Thread(() -> {
            try {
                System.out.println("Syncing device status...");
                lightService.queryAllStatusForLight();
                acService.queryAllStatusForAC();
                System.out.println("Sync complete.");
            } catch (Exception e) {
                System.out.println("Initial sync failed: " + e.getMessage());
            }
        }, "KNX-Initial-Sync-Thread");

        syncThread.start();

        System.out.println("--------------------------------------");
        System.out.println(" KNX TCP Server Started");
        System.out.println(" Listening on port: " + tcpPort);
        System.out.println("--------------------------------------");
    }
}
