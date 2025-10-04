package bharati.binita.cache.contract.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GarbageGenerator implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(GarbageGenerator.class);

    private List<byte[]> memoryHog = new ArrayList<>();

    @Override
    public void run() {
        try {
            while (true) {
                // Allocate 1MB chunks
                byte[] chunk = new byte[1024 * 1024];
                memoryHog.add(chunk);

                // Small sleep to make GC logs easier to read
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        catch (Throwable t) {
            log.error("GarbageGenerator failed",t);
        }
    }
}
