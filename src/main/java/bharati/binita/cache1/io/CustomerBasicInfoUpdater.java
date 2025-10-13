package bharati.binita.cache1.io;

import bharati.binita.cache1.contract.CacheService;
import bharati.binita.cache1.model.CustomerInfo;
import bharati.binita.cache1.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class CustomerBasicInfoUpdater implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(CustomerBasicInfoUpdater.class);
    private final Random random = new Random();//this is called in thread, but only 1 cache updater thread is there.
    private CacheService cacheService;
    private int startCustomerId;
    private int endCustomerId;


    public CustomerBasicInfoUpdater(CacheService cacheService,
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

                    String customerInfoJson = cacheService.getBasicCustomerInfo(i);
                    CustomerInfo customerInfo = Util.OBJECT_MAPPER.readValue(customerInfoJson, CustomerInfo.class);

                    String phone = Util.generateUSPhoneNumber(Util.MAX_PHONE_CHARS);
                    String email = Util.generateRandomEmail(Util.MAX_EMAIL_CHARS);

                    long st = System.nanoTime();
                    this.cacheService.updateBasicCustomerInfo(i, phone, email);
                    long et = System.nanoTime();

                    processedCount++;
                    if (processedCount > 0 && processedCount%1000000 == 0) {
                        log.info("custId = {}, old phone = {}, old email = {}", i, customerInfo.getHomePhone(), customerInfo.getHomeEmail());
                        log.info("custId = {}, updated phone = {}, updated email = {}", i, phone, email);
                        log.info("Basic cache info update time = {}",(et-st)/1000000);
                    }
                }
                Thread.sleep(1 * 60 * 1000);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }
}
