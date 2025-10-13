package bharati.binita.cache1.io;

import bharati.binita.cache1.contract.CacheService;
import bharati.binita.cache1.model.CustomerInfo;
import bharati.binita.cache1.model.TransactionDetails;
import bharati.binita.cache1.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class CustomerTransactionReader implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(CustomerTransactionReader.class);

    private CacheService cacheService;
    private int startCustomerId;
    private int endCustomerId;

    public CustomerTransactionReader(CacheService cacheService, int startCustomerId, int endCustomerId) {
        this.cacheService = cacheService;
        this.startCustomerId = startCustomerId;
        this.endCustomerId = endCustomerId;
    }


    @Override
    public void run() {
        while (true){
            try {
                int processedCount = 0;
                for (int i = startCustomerId ; i <= endCustomerId ; i++) {
                    String customerTrxns = cacheService.getLatestTrxnsForCustomer(i);
                    log.info("customerTrxns = {}", customerTrxns);
                    if (customerTrxns != null) {
                        processedCount++;
                        if (processedCount > 0 && processedCount%1000000 == 0) {
                            CustomerInfo custInfo = Util.OBJECT_MAPPER.readValue(customerTrxns, CustomerInfo.class);
                            List<TransactionDetails> trxns = custInfo.getTransactionDetails();
                            log.info("startCustomerIdx = {}, endCustomerId = {}. Finished reading latest trxns for {} customers",startCustomerId, endCustomerId, processedCount);
                            log.info("Last processed customer id = {}. Trxn entries = {}",i,trxns);
                        }
                    }
                }
                Thread.sleep(1*60*1000);
            } catch (Throwable t) {
                log.error("Exception while reading customer trxns",t);
            }
        }

    }
}
