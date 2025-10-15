package bharati.binita.cache1.common.helpers;

import bharati.binita.cache1.contract.CacheService;
import bharati.binita.cache1.model.CustomerInfo;
import bharati.binita.cache1.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class CustomerBasicInfoReaderCumUpdater implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(CustomerBasicInfoReaderCumUpdater.class);

    private CacheService cacheService;
    private int startCustomerId;
    private int endCustomerId;

    private static final ThreadLocal<MemorySegment> threadLocalBuffer = ThreadLocal.withInitial(() -> {
        Arena arena = Arena.ofConfined();
        return arena.allocate(Util.CUSTOMER_INFO_JSON_STR_SIZE);
    });

    public CustomerBasicInfoReaderCumUpdater(CacheService cacheService, int startCustomerId, int endCustomerId) {
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
                    long st1 = System.nanoTime();
                    String custInfoOld = this.cacheService.getBasicCustomerInfo(i, threadLocalBuffer.get());
                    long et1 = System.nanoTime();
                    if (custInfoOld != null) {
                        CustomerInfo customerInfo = Util.OBJECT_MAPPER.readValue(custInfoOld, CustomerInfo.class);
                        String phone = Util.generateUSPhoneNumber(Util.MAX_PHONE_CHARS);
                        String email = Util.generateRandomEmail(Util.MAX_EMAIL_CHARS);

                        long st2 = System.nanoTime();
                        this.cacheService.updateBasicCustomerInfo(i, phone, email);
                        long et2 = System.nanoTime();

                        processedCount++;
                        if (processedCount > 0 && processedCount%1000000 == 0){
                            log.info("custId = {}, lookedUp info = {}",i,custInfoOld);
                            log.info("custId = {}, email {} updated to {}, phone = {} updated to {}",i, customerInfo.getHomeEmail(), email,
                                     customerInfo.getHomePhone(), phone);
                            log.info("custId = {}, lookUpTime = {}",i,(et1-st1)/1000000);
                            log.info("custId = {}, updateTime = {}",i,(et2-st2)/1000000);
                        }
                    }
                }
                Thread.sleep(1*60*1000);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

    }
}
