package bharati.binita.cache.contract.jni;

import bharati.binita.cache.contract.common.utils.JVMPauseDetectorThread;
import bharati.binita.cache.contract.impl.jni.JNIBasedCacheImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main app to demonstrate that using native hashmap is
 * more efficient, and it will not cause GC, which in turn won't stall
 * application threads.
 */

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws InterruptedException {

        Thread appThread = new Thread(new JVMPauseDetectorThread());
        appThread.start();

        int totalKeys = Integer.parseInt(args[0]);
        long sleepMinutes = Long.parseLong(args[1]);
        String jniLibRef = args[2];
        JNIBasedCacheImpl nativeHashMap = new JNIBasedCacheImpl(jniLibRef);
        nativeHashMap.init_hash_table();
        while (true) {
            long st = System.nanoTime();
            for (int mapKey = 1; mapKey <= totalKeys ; mapKey++){
                nativeHashMap.insert_to_hash_table(mapKey, mapKey+"");
            }
            long et = System.nanoTime();
            log.info("Map loading took {} ms" ,(et-st) / 1_000_000.0);
            Thread.sleep(sleepMinutes*60*1000);
            log.info("Woke up from sleep");
        }
    }
}
