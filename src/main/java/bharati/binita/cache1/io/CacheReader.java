package bharati.binita.cache1.io;

import bharati.binita.cache1.contract.CacheService;
import bharati.binita.cache1.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

public class CacheReader implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(CacheReader.class);

    private CacheService cacheService;
    private static ThreadLocalRandom random = ThreadLocalRandom.current();//multiple reader threads are there

    public CacheReader(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public void run() {
        while (true) {
            try {
                for (int i = 0 ; i < Util.READ_UPDATE_CUST_IDS.length ; i++) {
                    int custId = Util.READ_UPDATE_CUST_IDS[i];
                    long st = System.nanoTime();
                    String custInfo = this.cacheService.getBasicCustomerInfo(custId);
                    long et = System.nanoTime();
                    log.info("Lookup time = {}",(et-st)/1000000);
                    log.info("custId = {}, custInfo = {}",custId, custInfo);
                }

                Thread.sleep(1*1000);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
