package bharati.binita.bridge.javaToC.ffi;

import bharati.binita.bridge.javaToC.common.JVMPauseDetectorThread;
import bharati.binita.bridge.javaToC.jni.NativeHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Main app to that calls non thread safe implementation of FFI HashMap.
 */

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    private static Random r = new Random();

    public static void main(String[] args) throws Throwable {

        Thread appThread = new Thread(new JVMPauseDetectorThread());
        appThread.start();

        int totalKeys = Integer.parseInt(args[0]);
        long sleepMinutes = Long.parseLong(args[1]);
        NativeHashMapFFI nativeHashMap = new NativeHashMapFFI("_ffi_hashmap");
        nativeHashMap.init();
        while (true) {
            long st = System.nanoTime();
            int generatedEntriesCount = 0;
            int mapKey = 2;
            while (generatedEntriesCount < totalKeys){
                    nativeHashMap.insert(mapKey, mapKey+"");
                    mapKey = mapKey + 2;
                    generatedEntriesCount++;
            }
            long et = System.nanoTime();
            log.info("Map loadingggg took {} ms" ,(et-st) / 1_000_000.0);
            //read any random key.
            /**
             * If you generate 100 million even numbers starting from 2, then the largest even number that
             * you can generate would be 200 million.
             * nth term = 2 + ( n - 1) * 2 = 200 million
             */

            // Generate random number between 1 and 100,000,000 (inclusive)
            int n = r.nextInt(totalKeys) + 1;
            // Multiply by 2 to make it even, range becomes 2 to 200,000,000
            int evenNumber = n * 2;
            String value = nativeHashMap.lookup(evenNumber);
            log.info("Lookup key {}, fetched value = {}" ,evenNumber, value);
            Thread.sleep(sleepMinutes*60*1000);
            log.info("Woke up from sleep");
        }
    }
}
