package bharati.binita.cache1.main;

import bharati.binita.cache1.contract.CacheService;
import bharati.binita.cache1.impl.ffi.FFICacheServiceImpl;
import bharati.binita.cache1.impl.offheap.OffHeapCacheServiceImpl;
import bharati.binita.cache1.impl.pure.CacheServiceImpl;
import bharati.binita.cache1.io.CacheReader;
import bharati.binita.cache1.io.CacheUpdater;
import bharati.binita.cache1.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static ExecutorService readerTPool = Executors.newFixedThreadPool(5);
    private static ExecutorService updaterTPool = Executors.newFixedThreadPool(5);


    public static void main(String[] args) throws Throwable {
        log.info("started123");
        String implType = args[0];
        CacheService cacheService = null;
        log.info("implyTYpe = {}", implType);

        if (implType.equalsIgnoreCase("pure")) {
            log.info("setting cacheService to CacheServiceImpl");
            cacheService = new CacheServiceImpl();
        }
        else if (implType.equalsIgnoreCase("ffi")) {
            cacheService = new FFICacheServiceImpl("_ffi_cache");//lib_ffi_cache
        }
        else if (implType.equalsIgnoreCase("offheap")) {
            cacheService = new OffHeapCacheServiceImpl();
        }

        //init cache
        cacheService.initCache();

        for (int i = 0 ; i < 5 ; i++) {
            readerTPool.submit(new CacheReader(cacheService));
        }

        //populateCache
        log.info("started loading all customers");
        for (int i = 1 ; i <= Util.MAX_CACHE_ENTRIES ; i++) {
            cacheService.onboardCustomer(i,Util.generateRandomString(Util.MAX_NAME_CHARS),Util.generateRandomString(Util.MAX_NAME_CHARS),Util.generateUSPhoneNumber(Util.MAX_PHONE_CHARS),
                                         Util.generateRandomEmail(Util.MAX_EMAIL_CHARS),Util.NOT_THREAD_SAFE_RANDOM.nextDouble(1000, 100000000));
            if (i % 1000 == 0) {
                log.info("Finished loading {} customers",i);
            }
        }
        log.info("finished loading all customers");
        //update populated basic customer info
        for (int i = 0 ; i < 5 ; i++) {
            updaterTPool.submit(new CacheUpdater(cacheService));
        }
    }
}
