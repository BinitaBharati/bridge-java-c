package bharati.binita.cache1.contract;

public interface CacheService {

    public void initCache() throws Throwable;

    public void onboardCustomer(int custId, String firstName, String lastName,
                                String phone, String email, double balance) throws Throwable;

    public String getBasicCustomerInfo(int custId) throws Throwable;

    public void updateBasicCustomerInfo(int custId, String phone, String email) throws Throwable;

    public void addTransactionEntry(int custId, long trxnDate, int opType, double amount) throws Throwable;

    public String getLatestTrxnsForCustomer(int custId) throws Throwable;
}
