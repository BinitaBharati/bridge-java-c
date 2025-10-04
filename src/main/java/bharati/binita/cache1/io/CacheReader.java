package bharati.binita.cache1.io;

import bharati.binita.cache1.contract.CacheService;
import bharati.binita.cache1.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

public class CacheReader implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(CacheReader.class);

    private CacheService cacheService;
    private static ThreadLocalRandom random = ThreadLocalRandom.current();

    public CacheReader(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public void run() {
        while (true) {
            try {
                int custId = random.nextInt(1, Util.MAX_CACHE_ENTRIES);
                String custInfo = this.cacheService.getBasicCustomerInfo(custId);
                log.info("custId = {}, custInfo = {}",custId, custInfo);
                Thread.sleep(1*1000);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
