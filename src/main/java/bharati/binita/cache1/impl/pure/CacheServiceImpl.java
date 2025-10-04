package bharati.binita.cache1.impl.pure;

import bharati.binita.cache1.contract.CacheService;
import bharati.binita.cache1.model.CustomerInfo;
import bharati.binita.cache1.model.TransactionDetails;
import bharati.binita.cache1.util.Util;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CacheServiceImpl implements CacheService {

    private ConcurrentHashMap<Integer, CustomerInfo> custIdToCustomerInfoMap;

    @Override
    public void initCache() {
        this.custIdToCustomerInfoMap = new ConcurrentHashMap<>();
    }

    public void onboardCustomer(int custId, String firstName, String lastName,
                                String phone, String email, double balance) throws Throwable {

        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setId(custId);
        customerInfo.setName(firstName);
        customerInfo.setLastName(lastName);
        customerInfo.setHomeEmail(email);
        customerInfo.setHomePhone(phone);

        customerInfo.setBalance(balance);

        custIdToCustomerInfoMap.put(custId, customerInfo);
    }

    //@Override
    /*
    This method will add transactions for an onboarded customer.
     */
    public void updateCacheEntry(int cacheKey, int opType, String opDate, double opValue) {
        if (custIdToCustomerInfoMap.get(cacheKey) != null) {
            CustomerInfo customerInfo = custIdToCustomerInfoMap.get(cacheKey);

            TransactionDetails trxn = new TransactionDetails();
            trxn.setTrxnDate(opDate);

            if (opType == Util.CREDIT_TRXN_TYPE) {
                trxn.setCredit(opValue);
                trxn.setBalance(trxn.getCredit() + customerInfo.getBalance());
                customerInfo.setBalance(trxn.getBalance());
            }
            else if (opType == Util.DEBIT_TRXN_TYPE) {
                if (opValue > customerInfo.getBalance()) {
                    trxn.setDebit(0);
                    trxn.setBalance(customerInfo.getBalance());
                }
                else {
                    trxn.setDebit(opValue);
                    trxn.setBalance(customerInfo.getBalance() - trxn.getDebit());
                    customerInfo.setBalance(trxn.getBalance());
                }

            }
            customerInfo.addTransactionDetails(trxn);
        }
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
     /*
    This method will get transactions that occurred on a specific date for an onboarded customer.
     */
    public String getSpecificCacheEntry(int cacheKey, String specificEntryLookUpStr) {
        List<TransactionDetails> transactionDetailsList = new ArrayList<>();
        CustomerInfo customerInfo = custIdToCustomerInfoMap.get(cacheKey);
        if (customerInfo != null) {
            transactionDetailsList = customerInfo.getTransactionDetails();
            transactionDetailsList = transactionDetailsList.stream().filter(trxn -> trxn.getTrxnDate().equals(specificEntryLookUpStr))
                    .collect(Collectors.toList());
        }
        try {
            return Util.OBJECT_MAPPER.writeValueAsString(transactionDetailsList);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


}
