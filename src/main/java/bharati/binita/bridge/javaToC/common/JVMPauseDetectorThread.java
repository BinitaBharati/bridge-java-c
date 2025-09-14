package bharati.binita.bridge.javaToC.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JVMPauseDetectorThread implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(JVMPauseDetectorThread.class);

    private long lastTime = System.nanoTime();
    @Override
    public void run() {
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
