package bharati.binita.pure.java;

import bharati.binita.java.busybee.BusyBee;
import bharati.binita.sample.http.server.SimpleHttpServer;
import bharati.binita.sample.http.server.SimpleNettyHttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static ExecutorService readerTPool = Executors.newFixedThreadPool(5);
    private static String[] precomputedValues;
    private static int minKey, maxKey, totalKeys;

    public static PureJavaHashMap init(String[] args) {

        totalKeys = Integer.parseInt(args[0]);
        minKey = Integer.parseInt(args[2]);
        maxKey = minKey + 2 * (totalKeys - 1);
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

        long sleepMinutes = Long.parseLong(args[1]);

        PureJavaHashMap pureJavaHashMap = init(args);
        SimpleHttpServer simpleHttpServer = new SimpleHttpServer(pureJavaHashMap);
        simpleHttpServer.startServer();


        /**
         * Here reader threads are reading in parallel, even before map might have got initialized.
         */
        for (int i = 0 ; i < 5 ; i++) {
            //readerTPool.submit(new ReaderThread(minKey, maxKey, pureJavaHashMap));
            readerTPool.submit(new BusyBee());
        }

        /*for (int i = 0 ; i < 5 ; i++) {
            readerTPool.submit(new ReaderThread(minKey, maxKey, pureJavaHashMap));
        }*/

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
            log.info("Map loadingggg took {} ms" ,(et-st) / 1_000_000.0);
            Thread.sleep(sleepMinutes*60*1000);
        }
    }
}
