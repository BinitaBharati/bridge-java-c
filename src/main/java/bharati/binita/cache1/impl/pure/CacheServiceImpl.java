package bharati.binita.cache1.impl.pure;

import bharati.binita.cache1.contract.CacheService;
import bharati.binita.cache1.model.CustomerInfo;
import bharati.binita.cache1.model.TransactionDetails;
import bharati.binita.cache1.util.Util;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CacheServiceImpl implements CacheService {

    private ConcurrentHashMap<Integer, CustomerInfo> custIdToCustomerInfoMap;

    @Override
    public void initCache() {
        this.custIdToCustomerInfoMap = new ConcurrentHashMap<>(Util.MAX_CACHE_ENTRIES);
    }

    @Override
    public void onboardCustomer(int custId, String firstName, String lastName,
                                String phone, String email, double balance) throws Throwable {

        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setId(custId);
        customerInfo.setName(firstName);
        customerInfo.setLastName(lastName);
        customerInfo.setHomeEmail(email);
        customerInfo.setHomePhone(phone);

        customerInfo.setBalance(balance);

        custIdToCustomerInfoMap.put(custId, customerInfo);//this first time onboarding, done in single thread.
    }

    @Override
    public void updateBasicCustomerInfo(int custId, String phone, String email) throws Throwable {

        custIdToCustomerInfoMap.computeIfPresent(custId, (id, customerInfo) -> {//thread safe update.
            customerInfo.setHomePhone(phone);
            customerInfo.setHomeEmail(email);// safe update
            return customerInfo;
        });
    }

    @Override
    /*
    This method will get basic info for a onboarded customer.
     */
    public String getBasicCustomerInfo(int cacheKey) throws Throwable {
        CustomerInfo customerInfo = custIdToCustomerInfoMap != null ? custIdToCustomerInfoMap.get(cacheKey) : null;
        if (customerInfo != null) {
            try {
                return Util.OBJECT_MAPPER.writeValueAsString(customerInfo);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    //@Override
    public void addTransactionEntry(int custId, long opDate, int opType, double amount) throws Throwable {
        if (custIdToCustomerInfoMap.get(custId) != null) {
            CustomerInfo customerInfo = custIdToCustomerInfoMap.get(custId);

            TransactionDetails trxn = new TransactionDetails();
            trxn.setTrxnDate(opDate);

            if (opType == Util.CREDIT_TRXN_TYPE) {
                trxn.setCredit(amount);
                trxn.setBalance(trxn.getCredit() + customerInfo.getBalance());
                customerInfo.setBalance(trxn.getBalance());
            }
            else if (opType == Util.DEBIT_TRXN_TYPE) {
                if (amount > customerInfo.getBalance()) {
                    trxn.setDebit(0);
                    trxn.setBalance(customerInfo.getBalance());
                }
                else {
                    trxn.setDebit(amount);
                    trxn.setBalance(customerInfo.getBalance() - trxn.getDebit());
                    customerInfo.setBalance(trxn.getBalance());
                }

            }
            customerInfo.addTransactionDetails(trxn);
        }
    }

    //@Override
    public String getLatestTrxnsForCustomer(int custId, MemorySegment buffer) {
        if (custIdToCustomerInfoMap.get(custId) != null) {
            CustomerInfo customerInfo = custIdToCustomerInfoMap.get(custId);
        }
        return null;
    }


}
