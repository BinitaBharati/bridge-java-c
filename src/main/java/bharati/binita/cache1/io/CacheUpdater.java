package bharati.binita.cache1.io;

import bharati.binita.cache1.contract.CacheService;
import bharati.binita.cache1.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class CacheUpdater implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(CacheUpdater.class);
    private final Random random = new Random();//this is called in thread, but only 1 cache updater thread is there.
    private CacheService cacheService;

    public CacheUpdater(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public void run() {
        while (true) {
            try {
                for (int i = 0 ; i < Util.READ_UPDATE_CUST_IDS.length ; i++) {
                    int custId = Util.READ_UPDATE_CUST_IDS[i];
                    String phone = Util.generateUSPhoneNumber(Util.MAX_PHONE_CHARS);
                    String email = Util.generateRandomEmail(Util.MAX_EMAIL_CHARS);
                    log.info("CacheUpdater: custId = {}, update phone = {}, updated email = {}", custId, phone, email);
                    this.cacheService.updateBasicCustomerInfo(custId, phone, email);
                }
                Thread.sleep(1 * 1000);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }
}
