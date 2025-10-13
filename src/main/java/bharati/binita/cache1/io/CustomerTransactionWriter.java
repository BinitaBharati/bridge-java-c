package bharati.binita.cache1.io;

import bharati.binita.cache1.contract.CacheService;
import bharati.binita.cache1.model.CustomerInfo;
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

    public CustomerTransactionWriter(CacheService cacheService, String startDatStr, int startCustomerId, int endCustomerId) {
        this.cacheService = cacheService;
        this.startDate = LocalDateTime.parse(startDatStr, Util.FORMATTER);
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
        while (true) {
            try {
                int processedCount = 0;
                for (int i = startCustomerId ; i <= endCustomerId ; i++) {
                    long trxnDate = Util.randomTimestampWithinOneDay(startDate);
                    String customerInfoJson = cacheService.getBasicCustomerInfo(i);
                    if (customerInfoJson != null) {
                        boolean isCreditOp = Util.THREAD_SAFE_RANDOM.nextBoolean();
                        if (isCreditOp) {
                            cacheService.addTransactionEntry(i, trxnDate, Util.CREDIT_TRXN_TYPE, Util.THREAD_SAFE_RANDOM.nextDouble(100, 100000000));
                            processedCount++;

                        }
                        else {
                            CustomerInfo customerInfo = Util.OBJECT_MAPPER.readValue(customerInfoJson, CustomerInfo.class);
                            double debitAmount = Util.THREAD_SAFE_RANDOM.nextDouble(0, customerInfo.getBalance());
                            if (debitAmount > 0) {
                                cacheService.addTransactionEntry(i, trxnDate, Util.DEBIT_TRXN_TYPE, debitAmount);
                                processedCount++;
                            }
                        }
                        if (processedCount > 0 && processedCount%1000000 == 0) {
                            log.info("startCustomerIdx = {}, endCustomerId = {}. Finished dumping trxns for {} customers",startCustomerId, endCustomerId, processedCount);
                            log.info("Last processed customer = {}",customerInfoJson);
                        }
                    }
                }
                Thread.sleep(1*60*1000);
                startDate = startDate.plusDays(1);
            } catch (Throwable t) {
                log.error("Exception while dumping customers",t);
            }
        }
    }
}
