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
            try {
                for (int j = 0 ; j < Util.MAX_TRXNS_PER_CUSTOMER ; j++) {
                    log.info("Started dumping of {} trxns for custId {} to {}",j+1,startCustomerId, endCustomerId);
                    int totalDumpedTrxnsAcrossCustomersPerIteration = 0;
                    for (int i = startCustomerId ; i <= endCustomerId ; i++) {
                        long trxnDate = Util.randomTimestampWithinOneDay(startDate);
                        double balance = cacheService.getCustomerBalance(i);
                        if (balance != -1) {//customer with id found in cache
                            boolean isCreditOp = Util.THREAD_SAFE_RANDOM.nextBoolean();
                            if (isCreditOp) {
                                cacheService.addTransactionEntry(i, trxnDate, Util.CREDIT_TRXN_TYPE, Util.THREAD_SAFE_RANDOM.nextDouble(100, 100000000));
                                totalDumpedTrxnsAcrossCustomersPerIteration++;
                            }
                            else {
                                double debitAmount = Util.THREAD_SAFE_RANDOM.nextDouble(0, balance);
                                if (debitAmount > 0) {
                                    cacheService.addTransactionEntry(i, trxnDate, Util.DEBIT_TRXN_TYPE, debitAmount);
                                    totalDumpedTrxnsAcrossCustomersPerIteration++;
                                }
                            }
                            /*if (totalDumpedTrxnsAcrossCustomersPerIteration > 0 && totalDumpedTrxnsAcrossCustomersPerIteration%1000000 == 0) {
                                log.info("Finished dumping {} trxns between custIds {} to {}",totalDumpedTrxnsAcrossCustomersPerIteration, startCustomerId, i);
                                log.info("Last processed customer has id = {}, with balance = {}",i, balance);
                            }*/
                        }
                    }
                    log.info("Completed dumping of {} trxns for custId {} to {}",j+1,startCustomerId, endCustomerId);
                }

                Thread.sleep(1*60*1000);
                startDate = startDate.plusDays(1);
            } catch (Throwable t) {
                log.error("Exception while dumping customers",t);
            }
    }
}
