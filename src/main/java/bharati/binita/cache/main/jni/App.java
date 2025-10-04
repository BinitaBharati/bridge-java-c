package bharati.binita.cache.main.jni;

import bharati.binita.cache.contract.common.utils.JVMPauseDetectorThread;
import bharati.binita.cache.contract.impl.jni.JNIBasedCacheImpl;
import bharati.binita.cache.contract.jni.App3;
import bharati.binita.cache.contract.jni.HashMapReaderThread;
import bharati.binita.cache.helpers.jni.NativeMemoryMonitorJNI;
import bharati.binita.cache.helpers.jni.NativeMemoryUsageTrackerThread;
import bharati.binita.sample.http.server.SimpleHttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App3.class);
    private static ExecutorService jvmPauseDetectorTpool = Executors.newFixedThreadPool(5);


    public static void main(String[] args) throws Exception {

        int totalKeys = Integer.parseInt(args[0]);
        long sleepMinutes = Long.parseLong(args[1]);
        boolean isJeMallocEnabled = Boolean.parseBoolean(args[2]);
        String jniLibRef = args[3];//if libname is libjni_hashmap.so, then jniLibRef should be jni_hashmap

        NativeMemoryMonitorJNI nativeMemoryMonitorJNI = new NativeMemoryMonitorJNI("native_memory_usage_tracker_jni");//actual libname = libnative_memory_usage_tracker_jni.so
        Thread nativeMemoryUsageTrackerThread = new Thread(new NativeMemoryUsageTrackerThread(nativeMemoryMonitorJNI, isJeMallocEnabled));
        nativeMemoryUsageTrackerThread.start();

        JNIBasedCacheImpl jniBasedCache = new JNIBasedCacheImpl(jniLibRef);
        jniBasedCache.init_hash_table();

        SimpleHttpServer simpleHttpServer = new SimpleHttpServer(jniBasedCache);
        int httpServerPort = Integer.parseInt(args[4]);
        simpleHttpServer.startServer(httpServerPort);


        /**
         * Here reader threads are reading in parallel, even before map might have got initialized.
         */
        for (int i = 0 ; i < 5 ; i++){
            jvmPauseDetectorTpool.submit(new JVMPauseDetectorThread());
        }

        /**
         * Writer is single threaded. Writer keeps writing while multiple reader threads keep reading.
         */
        while (true) {
            long st = System.nanoTime();
            int mapKey = 2;
            for (int i = 1; i <= totalKeys ; i++){
                jniBasedCache.insert_to_hash_table(mapKey, mapKey+"");
                mapKey = mapKey + 2;
            }
            long et = System.nanoTime();
            log.info("Map loading took {} ms" ,(et-st) / 1_000_000.0);
            Thread.sleep(sleepMinutes*60*1000);
        }
    }
}
