package bharati.binita.bridge.javaToC.jni;

import bharati.binita.bridge.javaToC.common.JVMPauseDetectorThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Main app to that calls non thread safe implementation of JNI HashMap.
 */

public class App2 {

    private static final Logger log = LoggerFactory.getLogger(App2.class);
    private static Random r = new Random();

    public static void main(String[] args) throws InterruptedException {

        Thread appThread = new Thread(new JVMPauseDetectorThread());
        appThread.start();

        int totalKeys = Integer.parseInt(args[0]);
        long sleepMinutes = Long.parseLong(args[1]);
        String jniLibRef = "_jni_hashmap";//actual libname = lib_jni_hashmap.so

        NativeHashMap nativeHashMap = new NativeHashMap(jniLibRef);
        nativeHashMap.init_hash_table();
        /**
         * Notice below that there is no concurrent access to the native JNI HashMap.
         * Every operation is sequential (writes and reads)
         */
        while (true) {
            long st = System.nanoTime();
            int generatedKeys = 0;
            int mapKey = 2;
            while (generatedKeys < totalKeys){
                nativeHashMap.insert_to_hash_table(mapKey, mapKey+"");
                mapKey = mapKey + 2;
                generatedKeys++;
            }
            long et = System.nanoTime();
            log.info("Map loading took {} ms" ,(et-st) / 1_000_000.0);
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
            String value = nativeHashMap.hash_table_look_up(evenNumber);
            log.info("Lookup key {}, fetched value = {}" ,evenNumber, value);
            Thread.sleep(sleepMinutes*60*1000);
            }
    }
}
