package bharati.binita.cache1.common.helpers;

import bharati.binita.cache1.contract.CacheService;
import bharati.binita.cache1.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class CustomerBasicInfoReader implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(CustomerBasicInfoReader.class);

    private CacheService cacheService;
    private int startCustomerId;
    private int endCustomerId;

    public CustomerBasicInfoReader(CacheService cacheService, int startCustomerId, int endCustomerId) {
        this.cacheService = cacheService;
        this.startCustomerId = startCustomerId;
        this.endCustomerId = endCustomerId;
    }

    @Override
    public void run() {

    }
}
