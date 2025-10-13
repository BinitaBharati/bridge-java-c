package bharati.binita.cache1.io;

import bharati.binita.cache1.contract.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomerBasicInfoReader implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(CustomerBasicInfoReader.class);

    private CacheService cacheService;
    private int startCustomerId;
    private int endCustomerId;

    public CustomerBasicInfoReader(CacheService cacheService,
                                   int startCustomerId,
                                   int endCustomerId) {
        this.cacheService = cacheService;
        this.startCustomerId = startCustomerId;
        this.endCustomerId = endCustomerId;
    }

    @Override
    public void run() {
        while (true) {
            try {
                int processedCount = 0;
                for (int i = startCustomerId ; i <= endCustomerId ; i++) {
                    long st = System.nanoTime();
                    String custInfo = this.cacheService.getBasicCustomerInfo(i);
                    long et = System.nanoTime();
                    processedCount++;
                    if (processedCount > 0 && processedCount%1000000 == 0) {
                        log.info("Lookup time = {}",(et-st)/1000000);
                        log.info("custId = {}, custInfo = {}",i, custInfo);
                    }

                }
                Thread.sleep(1*60*1000);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
