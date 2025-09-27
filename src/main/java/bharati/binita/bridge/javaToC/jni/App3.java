package bharati.binita.bridge.javaToC.jni;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main app to that calls thread safe implementation of FFI HashMap.
 * Multiple, concurrent reader threads and a single threaded writer.
 */

public class App3 {

    private static final Logger log = LoggerFactory.getLogger(App3.class);
    private static Random r = new Random();
    private static ExecutorService readerTPool = Executors.newFixedThreadPool(5);


    public static void main(String[] args) throws InterruptedException {

        int totalKeys = Integer.parseInt(args[0]);
        long sleepMinutes = Long.parseLong(args[1]);
        int minKey = Integer.parseInt(args[2]);
        int maxKey = minKey + 2 * (totalKeys - 1);
        boolean isJeMallocEnabled = Boolean.parseBoolean(args[3]);

        NativeMemoryMonitorJNI nativeMemoryMonitorJNI = new NativeMemoryMonitorJNI("native_memory_usage_tracker_jni");//actual libname = libnative_memory_usage_tracker_jni.so
        Thread nativeMemoryUsageTrackerThread = new Thread(new NativeMemoryUsageTrackerThread(nativeMemoryMonitorJNI, isJeMallocEnabled));
        nativeMemoryUsageTrackerThread.start();


        String jniLibRef = "jni_ts2_hashmap";//actual libname = lijni_ts2_hashmap.so
        NativeHashMap nativeHashMap = new NativeHashMap(jniLibRef);
        nativeHashMap.init_hash_table();

        /**
         * Here reader threads are reading in parallel, even before map might have got initialized.
         */
        for (int i = 0 ; i < 5 ; i++){
            readerTPool.submit(new HashMapReaderThread(nativeHashMap, minKey, maxKey ));
        }

        /**
         * Writer is single threaded. Writer keeps writing while multiple reader threads keep reading.
         */
        while (true) {
        long st = System.nanoTime();
        int mapKey = 2;
        for (int i = 1; i <= totalKeys ; i++){
            nativeHashMap.insert_to_hash_table(mapKey, mapKey+"");
            mapKey = mapKey + 2;
        }
        long et = System.nanoTime();
        log.info("Map loadingggg took {} ms" ,(et-st) / 1_000_000.0);
        Thread.sleep(sleepMinutes*60*1000);
        }
    }
}
