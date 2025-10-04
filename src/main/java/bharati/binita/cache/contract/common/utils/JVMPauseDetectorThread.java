package bharati.binita.cache.contract.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JVMPauseDetectorThread implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(JVMPauseDetectorThread.class);

    private long lastTime = System.nanoTime();
    @Override
    public void run() {
        while (true) {
            long now = System.nanoTime();
            double elapsedMs = now - lastTime/1_000_000.0;
            if (elapsedMs > 100) { // 100ms
                log.info("Thread pause detected! Pause in ms = {}" ,elapsedMs);
            }
            lastTime = now;
        }
    }
}
