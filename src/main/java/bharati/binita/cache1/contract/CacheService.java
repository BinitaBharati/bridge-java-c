package bharati.binita.cache1.contract;

import java.lang.foreign.MemorySegment;

public interface CacheService {

    public void initCache() throws Throwable;

    public void onboardCustomer(int custId, String firstName, String lastName,
                                String phone, String email, double balance) throws Throwable;

    public String getBasicCustomerInfo(int custId, MemorySegment buffer) throws Throwable;

    //public byte[] getBasicCustomerInfo2(int custId, MemorySegment buffer) throws Throwable;

    public void updateBasicCustomerInfo(int custId, String phone, String email) throws Throwable;

    public void addTransactionEntry(int custId, long trxnDate, int opType, double amount) throws Throwable;

    public String getLatestTrxnsForCustomer(int custId) throws Throwable;

    public double getCustomerBalance(int custId) throws Throwable;
}