package bharati.binita.pure.java;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

public class ReaderThread implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(ReaderThread.class);

    private int min = 2;

    /**
     * If you generate 100 million even numbers starting from 2, then the largest even number that
     * you can generate would be 200 million.
     * nth term = 2 + ( n - 1) * 2 = 200 million
     */
    private int max;

    private PureJavaHashMap pureJavaHashMap;

    public ReaderThread(int minKey, int maxKey, PureJavaHashMap pureJavaHashMap) {
        this.min = minKey;
        this.max = maxKey;
        this.pureJavaHashMap = pureJavaHashMap;
    }


    @Override
    public void run() {
        while (true) {
            try {
                int randomEven = ThreadLocalRandom.current()
                        .nextInt(min / 2, (max / 2) + 1) * 2;
                String value = pureJavaHashMap.lookUpKey(randomEven);
                log.info(Thread.currentThread().getId() + "- Lookup key = {}, value = {}", randomEven, value);
            }
            catch (Throwable t) {
                log.error("Oppsy",t);
            }
        }
    }
}
