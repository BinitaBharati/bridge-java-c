package bharati.binita.java.busybee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusyBee implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(BusyBee.class);

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        while (true) {
            long now = System.nanoTime();
            long diff = now - lastTime;
            if (diff > 100_000_000) { // 100ms
                log.info("Thread pause detected! Pause in ms = {}" ,diff / 1_000_000.0);
            }
            lastTime = now;
        }
    }
}
