package bharati.binita.cache1.model;

import bharati.binita.cache1.util.Util;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerInfo {

    private int id;
    private String name;
    private String lastName;
    @JsonProperty("phone")
    private String homePhone;
    @JsonProperty("email")
    private String homeEmail;
    private double balance;

    public CustomerInfo(){
        this.transactionDetails = new TransactionDetails[Util.MAX_TRXNS_PER_CUSTOMER];
    }

    @JsonProperty("trxns")
    private TransactionDetails[] transactionDetails;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getHomePhone() {
        return homePhone;
    }

    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }

    public String getHomeEmail() {
        return homeEmail;
    }

    public void setHomeEmail(String homeEmail) {
        this.homeEmail = homeEmail;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public TransactionDetails[] getTransactionDetails() {
        return transactionDetails;
    }

    public void addTransactionDetails(TransactionDetails input) {
        //find empty index
        for (int i = 0 ; i < Util.MAX_TRXNS_PER_CUSTOMER ; i++) {
            if (transactionDetails[i] == null) {
                transactionDetails[i] = input;
                return;
            }
        }
        //no empty trxn index found
        for (int i = 0 ; i < Util.MAX_TRXNS_PER_CUSTOMER - 1 ; i++) {
            transactionDetails[i] = transactionDetails[i+1];
        }
        transactionDetails[Util.MAX_TRXNS_PER_CUSTOMER - 1] = input;
    }
}
