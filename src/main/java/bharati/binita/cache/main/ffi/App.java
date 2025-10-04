package bharati.binita.cache.main.ffi;

import bharati.binita.cache.contract.common.utils.JVMPauseDetectorThread;
import bharati.binita.cache.helpers.ffi.FFINativeMemoryMonitor;
import bharati.binita.cache.helpers.ffi.FFINativeMemoryUsageTrackerThread;
import bharati.binita.cache.contract.impl.ffi.FFIBasedCacheImpl;
import bharati.binita.sample.http.server.SimpleHttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main app to that calls thread safe implementation of FFI HashMap.
 * A single threaded writer.
 * Multiple, concurrent reader threads invoked by external clients through REST API.
 * External clients have knowledge of the mao key ranges.
 * Multiple busy bee threads that track jvm pause beyond a thresold.
 *
 */
public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static ExecutorService jvmPauseDetectorTPool = Executors.newFixedThreadPool(5);

    public static void main(String[] args) throws Throwable {

        int totalKeys = Integer.parseInt(args[0]);
        long sleepMinutes = Long.parseLong(args[1]);
        boolean isJeMallocEnabled = Boolean.parseBoolean(args[2]);
        String jniLibRef = args[3];//if libname is libjni_hashmap.so, then jniLibRef should be jni_hashmap
        String jniMemMonitorLibRef = args[4];//if libname is libjni_hashmap.so, then jniLibRef should be jni_hashmap
        int httpServerPort = Integer.parseInt(args[5]);

        //start thread that will track C code malloc/free calls memory usage.
        FFINativeMemoryMonitor nativeMemoryMonitorFFI = new FFINativeMemoryMonitor(jniMemMonitorLibRef);
        Thread nativeMemoryUsageTrackerThread = new Thread(new FFINativeMemoryUsageTrackerThread(nativeMemoryMonitorFFI, isJeMallocEnabled));
        nativeMemoryUsageTrackerThread.start();

        //Keep CPU busy with JVMPauseDetectorThread.
        for (int i = 0 ; i < 5 ; i++){
            jvmPauseDetectorTPool.submit(new JVMPauseDetectorThread());
        }

        FFIBasedCacheImpl ffiBasedCache = new FFIBasedCacheImpl(jniLibRef);
        ffiBasedCache.init();

        //start http server that will handle fetch key requests for lookup from this native FFI based hashmap.
        SimpleHttpServer simpleHttpServer = new SimpleHttpServer(ffiBasedCache);
        simpleHttpServer.startServer(httpServerPort);

        /**
         * Writer is single threaded. Writer keeps writing while multiple reader threads keep reading.
         */
        while (true) {
        long st = System.nanoTime();
        int mapKey = 2;
        for (int i = 1; i <= totalKeys ; i++){
            ffiBasedCache.updateCache(mapKey, mapKey+"");
            mapKey = mapKey + 2;
        }
        long et = System.nanoTime();
        log.info("Map loading took {} ms" ,(et-st) / 1_000_000.0);
        Thread.sleep(sleepMinutes*60*1000);
        }
    }
}
