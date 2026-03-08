package org.RMS.knx.thread;
import org.RMS.knx.command.KNXCommands;
import java.util.concurrent.*;

public class KNXCommandWriter implements Runnable {

    private final KNXCommands knxCommands;
    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(500);
    private volatile boolean running = true;

    public KNXCommandWriter(KNXCommands knxCommands) {
        this.knxCommands = knxCommands;
    }

    public void submit(Runnable task) {
        if (running) {
            if (!queue.offer(task)) {
                System.out.println("KNX queue full. Dropping command.");
            }
        }
    }

    @Override
    public void run() {
        Thread.currentThread().setName("KNXWriter-Thread");

        while (running || !queue.isEmpty()) {
            try {
                Runnable task = queue.poll(1, TimeUnit.SECONDS);
                if (task != null) {
                    try {
                        task.run();
                    } catch (Throwable t) {   // <- catch Throwable, not only Exception
                        System.out.println("KNX task crashed: " + t.getMessage());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println("KNX Writer stopped.");
    }

    public void stop() {
        running = false;
    }
}
