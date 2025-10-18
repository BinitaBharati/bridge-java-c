package bharati.binita.cache1.common.helpers;

import bharati.binita.cache1.contract.CacheService;
import bharati.binita.cache1.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class CustomerTransactionWriter implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(CustomerTransactionWriter.class);

    private CacheService cacheService;
    private LocalDateTime startDate;
    private int startCustomerId;
    private int endCustomerId;

    public CustomerTransactionWriter(CacheService cacheService, String startDateStr, int startCustomerId, int endCustomerId) {
        this.cacheService = cacheService;
        this.startDate = LocalDateTime.parse(startDateStr, Util.FORMATTER);
        this.startCustomerId = startCustomerId;
        this.endCustomerId = endCustomerId;
    }

    @Override
    public void run() {
        try {
            startDumpingCustomerTrxns();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void startDumpingCustomerTrxns() throws InterruptedException {
        int totalDumpedTrxnsAcrossCustomersTillNow = 0;
        while (true) {
            try {
                    for (int i = startCustomerId ; i <= endCustomerId ; i++) {
                        long st = -1, et = -1;
                        long trxnDate = Util.randomTimestampWithinOneDay(startDate);
                        double balance = cacheService.getCustomerBalance(i);
                        if (balance != -1) {//customer with id found in cache
                            boolean isCreditOp = Util.THREAD_SAFE_RANDOM.nextBoolean();
                            if (isCreditOp) {
                                st = System.nanoTime();
                                cacheService.addTransactionEntry(i, trxnDate, Util.CREDIT_TRXN_TYPE, Util.THREAD_SAFE_RANDOM.nextDouble(100, 100000000));
                                et = System.nanoTime();
                                totalDumpedTrxnsAcrossCustomersTillNow++;
                                if(totalDumpedTrxnsAcrossCustomersTillNow%1000000 == 0) {
                                    log.info("custId = {}, trxnUpdateTime in ns = {}, trxnUpdateTime in ms = {} ",i,(et-st),(et-st)/1000000);
                                }
                            }
                            else {
                                double debitAmount = Util.THREAD_SAFE_RANDOM.nextDouble(0, balance);
                                if (debitAmount > 0) {
                                    st = System.nanoTime();
                                    cacheService.addTransactionEntry(i, trxnDate, Util.DEBIT_TRXN_TYPE, debitAmount);
                                    et = System.nanoTime();
                                    totalDumpedTrxnsAcrossCustomersTillNow++;
                                    if(totalDumpedTrxnsAcrossCustomersTillNow%1000000 == 0) {
                                        log.info("custId = {}, trxnUpdateTimeNs={}, trxnUpdateTimeMs={} ",i,(et-st),(et-st)/1000000);
                                    }
                                }
                            }
                        }
                    }
                log.info("Completed dumping {} trxns for custId {} to {}", totalDumpedTrxnsAcrossCustomersTillNow, startCustomerId, endCustomerId);
                Thread.sleep(1*60*1000);
                    startDate = startDate.plusDays(1);
            } catch (Throwable t) {
                log.error("Exception while dumping customers",t);
            }
        }
    }
}
