package bharati.binita.cache1.common.helpers;

import bharati.binita.cache1.contract.CacheService;
import bharati.binita.cache1.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class CustomerTransactionReader implements  Runnable{

    private static final Logger log = LoggerFactory.getLogger(CustomerTransactionReader.class);

    private CacheService cacheService;
    private int startCustomerId;
    private int endCustomerId;

    private static final ThreadLocal<MemorySegment> threadLocalBuffer = ThreadLocal.withInitial(() -> {
        Arena arena = Arena.ofConfined();
        return arena.allocate(Util.CUSTOMER_TRXN_INFO_JSON_STR_SIZE);
    });

    public CustomerTransactionReader(CacheService cacheService, int startCustomerId, int endCustomerId) {
        this.cacheService = cacheService;
        this.startCustomerId = startCustomerId;
        this.endCustomerId = endCustomerId;
    }

    @Override
    public void run() {
        int readTrxnCount = 0;
        while (true) {
            try {
                for (int i = startCustomerId ; i <= endCustomerId ; i++) {
                    long st = System.nanoTime();
                    String custTrxnInfoStr = this.cacheService.getLatestTrxnsForCustomer(i, threadLocalBuffer.get());
                    long et = System.nanoTime();

                    readTrxnCount++;
                    if (readTrxnCount > 0 && readTrxnCount%1000000 == 0) {
                        log.info("custId = {}, latestTransactions = {}",i,custTrxnInfoStr);
                        log.info("custId = {}, lookupTimeNs={}, lookupTimeMs={}",i, (et-st),(et-st)/1000000);
                    }
                }
                Thread.sleep(1*60*1000);
            }
            catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

    }
}
