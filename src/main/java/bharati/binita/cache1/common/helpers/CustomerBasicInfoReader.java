package bharati.binita.cache1.common.helpers;

import bharati.binita.cache1.contract.CacheService;
import bharati.binita.cache1.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class CustomerBasicInfoReader implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(CustomerBasicInfoReader.class);

    private CacheService cacheService;
    private int startCustomerId;
    private int endCustomerId;

    /**
     * ThreadLocal maintains a separate value for each thread that accesses it.
     *
     * The withInitial(...) lambda is invoked once per thread, the first time that thread calls threadLocalBuffer.get().
     *
     * The value returned by the lambda (here, the MemorySegment) is stored internally for that thread only.
     *
     * So:
     *
     * Thread A → lambda runs → creates arena A → allocates MemorySegment A → stores in ThreadLocal for Thread A.
     *
     * Thread B → lambda runs → creates arena B → allocates MemorySegment B → stores in ThreadLocal for Thread B.
     *
     * Thread A again → lambda does not run → returns MemorySegment A.
     */
    private static final ThreadLocal<MemorySegment> threadLocalBuffer = ThreadLocal.withInitial(() -> {
        Arena arena = Arena.ofConfined();
        return arena.allocate(Util.CUSTOMER_INFO_JSON_STR_SIZE);
    });

    public CustomerBasicInfoReader(CacheService cacheService, int startCustomerId, int endCustomerId) {
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
                    //if (processedCount > 0 && processedCount%1000000 == 0) {
                    String custInfo = this.cacheService.getBasicCustomerInfo(i, threadLocalBuffer.get());
                    long et = System.nanoTime();
                    processedCount++;
                    if (processedCount > 0 && processedCount%1000000 == 0){
                        log.info("custId = {}, custInfo = {}",i, custInfo);
                        log.info("custId = {}, lookup time ms = {}",i, (et-st)/1000000);
                    }
                }
                Thread.sleep(1*60*1000);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
