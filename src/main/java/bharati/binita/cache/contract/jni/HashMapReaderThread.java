package bharati.binita.cache.contract.jni;

import bharati.binita.cache.contract.impl.jni.JNIBasedCacheImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

public class HashMapReaderThread implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(HashMapReaderThread.class);

    private int min;
    /**
     * If you generate 100 million even numbers starting from 2, then the largest even number that
     * you can generate would be 200 million.
     * nth term = 2 + ( n - 1) * 2 = 200 million
     */
    private int max;

    private JNIBasedCacheImpl nativeHashMap;

    public HashMapReaderThread(JNIBasedCacheImpl nativeHashMap, int minKey, int maxKey) {
        this.nativeHashMap = nativeHashMap;
        this.min = minKey;
        this.max = maxKey;
    }


    @Override
    public void run() {
        while (true) {
            int randomEven = ThreadLocalRandom.current()
                    .nextInt(min / 2, (max / 2) + 1) * 2;
            String value = nativeHashMap.hash_table_look_up(randomEven);
            log.info(Thread.currentThread().getId() + "- Lookup key = {}, value = {}", randomEven, value);
        }
    }
}
