package bharati.binita.bridge.javaToC.ffi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main app to that calls thread safe implementation of FFI HashMap.
 * Multiple, concurrent reader threads and a single threaded writer.
 */
public class App2 {

    private static final Logger log = LoggerFactory.getLogger(App2.class);
    private static Random r = new Random();
    private static ExecutorService readerTPool = Executors.newFixedThreadPool(5);

    public static void main(String[] args) throws Throwable {

        int totalKeys = Integer.parseInt(args[0]);
        long sleepMinutes = Long.parseLong(args[1]);
        int minKey = Integer.parseInt(args[2]);
        boolean isJeMallocEnabled = Boolean.parseBoolean(args[3]);

        int maxKey = minKey + 2 * (totalKeys - 1);
        NativeHashMapFFI nativeHashMap = new NativeHashMapFFI("ffi_ts_hashmap");
        nativeHashMap.init();

        /**
         * Here reader threads are reading in parallel, even before map might have got initialized.
         */
        for (int i = 0 ; i < 5 ; i++){
            readerTPool.submit(new HashMapReaderThread(nativeHashMap, minKey, maxKey));
        }

        NativeMemoryMonitorFFI nativeMemoryMonitorFFI = new NativeMemoryMonitorFFI("native_memory_usage_tracker");
        Thread nativeMemoryUsageTrackerThread = new Thread(new NativeMemoryUsageTrackerThread(nativeMemoryMonitorFFI,isJeMallocEnabled));
        nativeMemoryUsageTrackerThread.start();

        /**
         * Writer is single threaded. Writer keeps writing while multiple reader threads keep reading.
         */
        while (true) {
        long st = System.nanoTime();
        int mapKey = 2;
        for (int i = 1; i <= totalKeys ; i++){
            //log.info("putting key = {} into map", mapKey);
            nativeHashMap.insert(mapKey, mapKey+"");
            //log.info("after putting key = {} into map", mapKey);
            mapKey = mapKey + 2;
        }
        long et = System.nanoTime();
        log.info("Map loadingggg took {} ms" ,(et-st) / 1_000_000.0);
        Thread.sleep(sleepMinutes*60*1000);
        }
    }
}
