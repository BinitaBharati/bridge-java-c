package bharati.binita.cache.main.pure;

import bharati.binita.cache.contract.common.utils.BusyBee;
import bharati.binita.cache.contract.common.utils.GarbageGenerator;
import bharati.binita.cache.contract.common.utils.JVMPauseDetectorThread;
import bharati.binita.cache.contract.impl.pure.PureJavaHashMap;
import bharati.binita.sample.http.server.SimpleHttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static ExecutorService jvmPauseDetectorTPool = Executors.newFixedThreadPool(5);
    private static String[] precomputedValues;
    private static int minKey,  totalKeys;

    public static PureJavaHashMap init(String[] args) {

        totalKeys = Integer.parseInt(args[0]);
        minKey = Integer.parseInt(args[1]);
        int maxKey = minKey + 2 * (totalKeys - 1);
        precomputedValues = new String[totalKeys];

        int mapKey = 2;
        for (int i = 0 ; i < totalKeys; i++){
            precomputedValues[i] = Integer.toString(mapKey);
            mapKey = mapKey + 2;
        }

        PureJavaHashMap pureJavaHashMap = new PureJavaHashMap();
        return pureJavaHashMap;

    }

    public static void main(String[] args) throws Exception {
        long sleepMinutes = Long.parseLong(args[2]);
        int port = Integer.parseInt(args[3]);
        boolean startMajorGcTriggerThread = Boolean.parseBoolean(args[4]);



        /**
         * Here reader threads are reading in parallel, even before map might have got initialized.
         */
        /*for (int i = 0 ; i < 5 ; i++) {
            jvmPauseDetectorTPool.submit(new JVMPauseDetectorThread());
        }*/
        BusyBee busyBee = new BusyBee();
        jvmPauseDetectorTPool.submit(busyBee);
        log.info("started busybee");

        if (startMajorGcTriggerThread) {
            GarbageGenerator garbageGenerator = new GarbageGenerator();
            jvmPauseDetectorTPool.submit(garbageGenerator);
        }


        PureJavaHashMap pureJavaHashMap = init(args);
        SimpleHttpServer simpleHttpServer = new SimpleHttpServer(pureJavaHashMap);
        simpleHttpServer.setBusyBee(busyBee);
        simpleHttpServer.startServer(port);


        /**
         * Writer is single threaded. Writer keeps writing while multiple reader threads keep reading.
         */
        while (true) {
            long st = System.nanoTime();
            int mapKey = 2;
            for (int i = 0; i < totalKeys ; i++){
                pureJavaHashMap.updateCache(mapKey, precomputedValues[i]);
                mapKey = mapKey + 2;
            }
            long et = System.nanoTime();
            log.info("Map loading took {} ms" ,(et-st) / 1_000_000.0);
            Thread.sleep(sleepMinutes*60*1000);
        }
    }
}
