package bharati.binita.cache1.impl.offheap;

import bharati.binita.cache1.contract.CacheService;
import bharati.binita.cache1.model.CustomerInfo;
import bharati.binita.cache1.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.*;
import java.util.concurrent.ConcurrentHashMap;

public class OffHeapCacheServiceImpl implements CacheService {

    private static final Logger log = LoggerFactory.getLogger(OffHeapCacheServiceImpl.class);

    private ConcurrentHashMap<Integer, MemorySegment> cache;//MemorySegmnet is not thread safe.

    /**
     * Memory allocated from a shared arena is not automatically reclaimed.
     *
     * It will stay allocated until you explicitly call:
     *
     * arena.close();
     *
     * Once you do that:
     *
     * All MemorySegments created from that arena are invalidated.
     *
     * The corresponding native memory is released immediately.
     *
     * Further access to those segments throws IllegalStateException.
     */
    private final Arena arena = Arena.ofShared(); // shared across threads

    @Override
    public void initCache() throws Throwable {
        cache = new ConcurrentHashMap<>();
        //custIdLock = new ConcurrentHashMap<>();
        for (int i = 1; i < Util.MAX_CACHE_ENTRIES ; i++) {
            //custIdLock.put(i, new AtomicBoolean(false));
        }
    }

    @Override
    public void onboardCustomer(int custId, String firstName, String lastName, String phone, String email, double balance) throws Throwable {
        MemorySegment custInfoMemSegment = CustomerInfoFFIWriter.toOffHeap(arena, custId, firstName, lastName, phone, email, balance);
        synchronized (custInfoMemSegment) {
            cache.put(custId, custInfoMemSegment);
        }
    }

    @Override
    public String getBasicCustomerInfo(int custId) throws Throwable {
        String basicInfo = null;
        MemorySegment memorySegment = cache.get(custId);
        if (memorySegment != null) {
            synchronized (memorySegment) {
                CustomerInfo customerInfo = cache.get(custId) != null ? CustomerInfoFFIReader.fromOffHeap(memorySegment) : null;
                if (customerInfo != null) {
                    basicInfo =  Util.OBJECT_MAPPER.writeValueAsString(customerInfo);
                }
            }
        }
        return basicInfo;
    }

    @Override
    public void updateBasicCustomerInfo(int custId, String phone, String email) throws Throwable {
        MemorySegment memorySegment = cache.get(custId);
        if (memorySegment != null) {
            synchronized (memorySegment) {
                CustomerInfoFFIWriter.updateContact(memorySegment, phone, email);
            }
        }
    }

    @Override
    public void addTransactionEntry(int custId, long trxnDate, int opType, double amount) throws Throwable {

    }

    @Override
    public String getLatestTrxnsForCustomer(int custId) {
        return "";
    }
}
