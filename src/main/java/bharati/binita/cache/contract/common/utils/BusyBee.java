package bharati.binita.cache.contract.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

public class BusyBee
implements Runnable
{
    private static final Logger log = LoggerFactory.getLogger(BusyBee.class);
    private String randomStringResult;

    @Override
    public void run() {
        try {
            while (true) {
                long startTime = System.nanoTime();
                long subsequentTime = System.nanoTime();
                long diff = subsequentTime - startTime;
                double elapsedMs = diff / 1_000_000.0; // convert to milliseconds
                if (elapsedMs > 100) { // 100ms
                    log.info("BusyBee Thread pause detected! Pause in ms = {}", elapsedMs);
                }
                int iterationCount = ThreadLocalRandom.current().nextInt(100, 100000);
                for (int i = 0; i < iterationCount; i++) {
                }
                long endTime = System.nanoTime();
                long diff1 = endTime - startTime;
                double elapsedMs1 = diff1 / 1_000_000.0; // convert to milliseconds
                if (elapsedMs1 > 100) { // 100ms
                    log.info("BusyBee Thread pause detected! Pause in ms = {}", elapsedMs1);
                }
                randomStringResult = Integer.toString(iterationCount);
            }
        }
        catch (Throwable t) {
            log.error("BusyBee failed",t);
        }
    }


    public String getRandomStringResult() {
        return randomStringResult;
    }

    public void setRandomStringResult(String randomStringResult) {
        this.randomStringResult = randomStringResult;
    }
}
