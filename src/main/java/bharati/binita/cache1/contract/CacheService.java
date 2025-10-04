package bharati.binita.cache1.contract;

public interface CacheService {

    public void initCache() throws Throwable;

    public void onboardCustomer(int custId, String firstName, String lastName,
                                String phone, String email, double balance) throws Throwable;

    public String getBasicCustomerInfo(int custId) throws Throwable;

    //public String getSpecificCacheEntry(int cacheKey, String specificEntryLookUpStr);
}
