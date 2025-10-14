package bharati.binita.cache1.main;

import bharati.binita.cache1.common.helpers.CustomerTransactionWriter;
import bharati.binita.cache1.contract.CacheService;
import bharati.binita.cache1.impl.ffi.FFICacheServiceImpl;
import bharati.binita.cache1.impl.offheap.OffHeapCacheServiceImpl;
import bharati.binita.cache1.impl.pure.CacheServiceImpl;
import bharati.binita.cache1.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);
    //private static ExecutorService cacheBasicInfoReaderTPool ;
    //private static ExecutorService cacheBasicInfoUpdaterTPool ;
    //private static ExecutorService cacheTrxnReaderTPool ;
    private static ExecutorService cacheTrxnUpdaterTPool ;


    public static void main(String[] args) throws Throwable {
        log.info("started123");
        String implType = args[0];

        ThreadFactory namedFactory = new ThreadFactory() {
            private int counter = 1;

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "MyWorker-" + counter++);
                return t;
            }
        };

       /* cacheBasicInfoReaderTPool = Executors.newFixedThreadPool(Integer.parseInt(args[1]), namedFactory);
        cacheTrxnReaderTPool = Executors.newFixedThreadPool(Integer.parseInt(args[1]), namedFactory);

        cacheBasicInfoUpdaterTPool = Executors.newFixedThreadPool(Integer.parseInt(args[1]), namedFactory);*/
        cacheTrxnUpdaterTPool = Executors.newFixedThreadPool(Integer.parseInt(args[1]), namedFactory);

        CacheService cacheService = null;
        log.info("implyTYpe = {}", implType);

        if (implType.equalsIgnoreCase("pure")) {
            log.info("setting cacheService to CacheServiceImpl");
            cacheService = new CacheServiceImpl();
        }
        else if (implType.equalsIgnoreCase("ffi")) {
            cacheService = new FFICacheServiceImpl("_ffi_cache1");//lib_ffi_cache1
        }
        else if (implType.equalsIgnoreCase("offheap")) {
            cacheService = new OffHeapCacheServiceImpl();
        }

        //init cache
        cacheService.initCache();

        Map<Integer, Integer> startCustomerIdToEndCustomerId = Util.divideCustomersIntoBatches(Util.MAX_CACHE_ENTRIES, Integer.parseInt(args[1]));
        log.info("startCustomerIdToEndCustomerId = {}",startCustomerIdToEndCustomerId);

        //populateCache
        long st = System.nanoTime();
        log.info("started loading all customers");
        for (int i = 1 ; i <= Util.MAX_CACHE_ENTRIES ; i++) {
            cacheService.onboardCustomer(i,Util.generateRandomString(Util.MAX_NAME_CHARS),Util.generateRandomString(Util.MAX_NAME_CHARS),Util.generateUSPhoneNumber(Util.MAX_PHONE_CHARS),
                                         Util.generateRandomEmail(Util.MAX_EMAIL_CHARS),Util.NOT_THREAD_SAFE_RANDOM.nextDouble(1000, 100000000));
            /*if (i % 1000 == 0) {
                log.info("Finished loading {} customers",i);
            }*/

        }
        long et = System.nanoTime();
        log.info("Loading time in millis = {}",(et-st)/1000000);//112 secs for ffi, 78 secs for pure
        log.info("finished loading all customers");


        Iterator<Integer> startCustomerIdToEndCustomerIdItr = startCustomerIdToEndCustomerId.keySet().iterator();
        List<Future<?>> futureList = new ArrayList<>();
        while (startCustomerIdToEndCustomerIdItr.hasNext()) {
            Integer startCustomerId = startCustomerIdToEndCustomerIdItr.next();
            futureList.add(cacheTrxnUpdaterTPool.submit(new CustomerTransactionWriter(cacheService, "2000-01-01 00:00:00",startCustomerId,
                                                                                  startCustomerIdToEndCustomerId.get(startCustomerId))));
        }
        for (int i = 0 ; i < futureList.size() ; i++) {
            while (!futureList.get(i).isDone()) {
            }
        }
        log.info("Done with dumping {} trxns for each of the {} customers", Util.MAX_TRXNS_PER_CUSTOMER, Util.MAX_CACHE_ENTRIES);

        while (true){}

        /*Iterator<Integer> startCustomerIdToEndCustomerIdItr = startCustomerIdToEndCustomerId.keySet().iterator();
        while (startCustomerIdToEndCustomerIdItr.hasNext()) {
            Integer startCustomerId = startCustomerIdToEndCustomerIdItr.next();
            cacheBasicInfoReaderTPool.submit(new CustomerBasicInfoReader(cacheService, startCustomerId, startCustomerIdToEndCustomerId.get(startCustomerId)));
        }

        startCustomerIdToEndCustomerIdItr = startCustomerIdToEndCustomerId.keySet().iterator();
        while (startCustomerIdToEndCustomerIdItr.hasNext()) {
            Integer startCustomerId = startCustomerIdToEndCustomerIdItr.next();
            cacheTrxnReaderTPool.submit(new CustomerTransactionReader(cacheService, startCustomerId, startCustomerIdToEndCustomerId.get(startCustomerId)));
        }*/



        //update populated basic customer info
        /*startCustomerIdToEndCustomerIdItr = startCustomerIdToEndCustomerId.keySet().iterator();
        while (startCustomerIdToEndCustomerIdItr.hasNext()) {
            Integer startCustomerId = startCustomerIdToEndCustomerIdItr.next();
            cacheBasicInfoUpdaterTPool.submit(new CustomerBasicInfoUpdater(cacheService, startCustomerId, startCustomerIdToEndCustomerId.get(startCustomerId)));
        }

        startCustomerIdToEndCustomerIdItr = startCustomerIdToEndCustomerId.keySet().iterator();
        while (startCustomerIdToEndCustomerIdItr.hasNext()) {
            Integer startCustomerId = startCustomerIdToEndCustomerIdItr.next();
            cacheTrxnUpdaterTPool.submit(new CustomerTransactionWriter(cacheService, "2000-01-01 00:00:00", startCustomerId, startCustomerIdToEndCustomerId.get(startCustomerId)));
        }*/
    }
}
